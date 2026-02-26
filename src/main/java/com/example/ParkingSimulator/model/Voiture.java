package com.example.ParkingSimulator.model;

import java.util.*;
import java.util.concurrent.*;
import com.example.ParkingSimulator.model.Parking;
import com.example.ParkingSimulator.view.AnimatedCar;

public class Voiture extends Thread {

	private final String nom;
	private final List<Parking> parkings;
	private Parking parkingChoisi;
	private int place;
	private final long dureePrevueMs;
	private final ProfilUsager profil;
	private final double budgetMax;

	// Penalty accumulated by the car
	private double penalite = 0.0;

	private AnimatedCar animatedCar;

	public Voiture(String nom, List<Parking> parkings, long dureePrevueMs,
			ProfilUsager profil, double budgetMax) {
		this.nom = nom;
		this.parkings = parkings;
		this.dureePrevueMs = dureePrevueMs;
		this.profil = profil;
		this.budgetMax = budgetMax;
	}

	public String getNom() { return this.nom; }
	public long getDureePrevueMs() { return dureePrevueMs; }
	public void setPlace(int place) { this.place = place; }
	public int getPlace() { return this.place; }
	public ProfilUsager getProfil() { return profil; }
	public double getBudgetMax() { return budgetMax; }

	public double getPenalite() { return penalite; }
	public void ajouterPenalite(double montant) { penalite += montant; }

	public void setAnimatedCar(AnimatedCar car) {
		this.animatedCar = car;
	}

	public AnimatedCar getAnimatedCar() {
		return this.animatedCar;
	}

	@Override
	public void run() {
		try {

			// 1. CFP
			for (Parking p : parkings) {
				p.log("CFP : La voiture " + nom +
						" publie un appel d'offres (durée " + dureePrevueMs +
						" ms, profil " + profil + ", budget " + budgetMax +
						"€, pénalité " + penalite + "€)");
			}

			// 2. Propositions en parallèle
			Map<Parking, Double> offres = new ConcurrentHashMap<>();

			ExecutorService executor = Executors.newFixedThreadPool(parkings.size());
			List<Future<?>> futures = new ArrayList<>();

			for (Parking p : parkings) {
				futures.add(executor.submit(() -> {
					double prix = p.demanderPrix(this) + this.getPenalite();
					offres.put(p, prix);
					p.log("PROPOSITION : " + nom + " reçoit une offre de " +
							p.getNom() + " = " + prix + "€ (pénalité incluse)");
				}));
			}

			for (Future<?> f : futures) f.get();
			executor.shutdown();

			// 3. Sélection du meilleur parking
			parkingChoisi = offres.entrySet()
					.stream()
					.min(Comparator.comparingDouble(Map.Entry::getValue))
					.get()
					.getKey();

			double prixChoisi = offres.get(parkingChoisi);

			// 4. Négociation
			double prixFinal = prixChoisi;

			if (prixFinal > budgetMax) {

				double nouveauPrix = parkingChoisi.contreProposition(this, budgetMax);

				if (nouveauPrix > budgetMax) {
					// APPLY PENALTY
					double penaltyAmount = 1.0;
					this.ajouterPenalite(penaltyAmount);

					parkingChoisi.log("PÉNALITÉ : " + nom +
							" reçoit " + penaltyAmount +
							"€. Total = " + this.getPenalite() + "€");

					return; // STOP HERE — car does NOT retry
				}

				prixFinal = nouveauPrix;
			}

			// 5. Stationner
			parkingChoisi.stationner(this);

			// 6. Durée réelle de stationnement
			Thread.sleep(dureePrevueMs * 4);

			// 7. Sortie
			parkingChoisi.sortir(this);

			// DONE — car disappears forever

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
