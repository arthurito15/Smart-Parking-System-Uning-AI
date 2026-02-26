package com.example.ParkingSimulator.model;

import com.example.ParkingSimulator.strategy.IStrategy;
import com.example.ParkingSimulator.Main;
import com.example.ParkingSimulator.view.AnimatedCar;
import javafx.application.Platform;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;   // <-- nécessaire pour le créneau horaire

public class Parking {

	private static final Logger LOGGER = Logger.getLogger(Parking.class.getName());
	private final Semaphore placesSemaphore;
	private final Lock placesLock = new ReentrantLock();
	private final List<Long> tempPlace = new ArrayList<>();
	private final List<Boolean> placeOccupe = new ArrayList<>();
	private final IStrategy strategy;
	private final int nbrPlaces;
	private final Main gui;
	private final int syncStrategy;
	private final String nom;

	public Parking(int nbrPlaces, IStrategy strategy, Main main, int syncStrategy, String nom) {
		this.nom = nom;

		try {
			FileHandler fileHandler = new FileHandler("../app.log", false);
			fileHandler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fileHandler);
			LOGGER.info("Fichier journalisation configuré avec succès");
		} catch (Exception e) {
			LOGGER.severe("Échec configuration log : " + e.getMessage());
			System.exit(1);
		}

		this.placesSemaphore = new Semaphore(nbrPlaces, true);
		this.syncStrategy = syncStrategy;

		for (int i = 0; i < nbrPlaces; i++) {
			tempPlace.add((i % 5 + 1) * 1000L);
			placeOccupe.add(false);
		}

		this.strategy = strategy;
		this.nbrPlaces = nbrPlaces;
		this.gui = main;
	}

	//****************get park name
	public String getNom() {
		return nom;
	}

	// ------------------------------------------------
	//   CRENEAU HORAIRE (NOUVEAU)
	// ------------------------------------------------
	private enum Creneau {
		POINTE,
		NUIT,
		WEEKEND,
		NORMAL
	}

	private Creneau getCreneauHoraire() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int day = cal.get(Calendar.DAY_OF_WEEK);

		// Week-end
		if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
			return Creneau.WEEKEND;
		}

		// Nuit
		if (hour >= 22 || hour < 6) {
			return Creneau.NUIT;
		}

		// Heures de pointe
		if ((hour >= 8 && hour < 10) || (hour >= 17 && hour < 19)) {
			return Creneau.POINTE;
		}

		return Creneau.NORMAL;
	}

	// -----------------------------
	//   TARIFICATION DYNAMIQUE
	// -----------------------------
	public double calculerPrix(long dureePrevueMs, ProfilUsager profil) {

		double base = 2.0;
		double dureeMinutes = dureePrevueMs / 60000.0;
		double variable = 0.5 * Math.ceil(dureeMinutes / 10.0);

		double occupation = getTauxOccupation();
		double surcharge = occupation * 1.5;

		double prix = base + variable + surcharge;

		// *********************  Réductions selon le profil
		switch (profil) {
			case ETUDIANT:
				prix *= 0.7; // -30%
				break;
			case RESIDENT:
				prix *= 0.85; // -15%
				break;
			case VIP:
				prix *= 0.5; // -50%
				break;
			case ABONNE:
				prix = 1.0; // prix fixe
				break;
			case TOURISTE:
			default:
				// pas de réduction
				break;
		}

		// ------------------------------------------------
		//   AJOUT : TARIFICATION PAR CRÉNEAU HORAIRE
		// ------------------------------------------------
		Creneau c = getCreneauHoraire();

		switch (c) {
			case POINTE:
				prix *= 1.3; // +30%
				break;
			case NUIT:
				prix *= 0.6; // -40%
				break;
			case WEEKEND:
				prix *= 0.8; // -20%
				break;
			default:
				break;
		}

		return prix;
	}

	public double getTauxOccupation() {
		long occupees = placeOccupe.stream().filter(b -> b).count();
		return (double) occupees / nbrPlaces;
	}

	// -----------------------------
	//   DEMANDE DE PRIX
	// -----------------------------
	public double demanderPrix(Voiture voiture) {

		long duree = voiture.getDureePrevueMs();
		ProfilUsager profil = voiture.getProfil();

		// Log du créneau horaire (NOUVEAU)
		LOGGER.info("[" + nom + "] Créneau horaire : " + getCreneauHoraire());

		LOGGER.info("[" + nom + "] La voiture " + voiture.getNom() +
				" (profil " + voiture.getProfil() + ") demande un prix pour " + duree + " ms");

		double prix = calculerPrix(duree, profil);

		LOGGER.info("[" + nom + "] Prix proposé à " + voiture.getNom() + " : " + prix + " euros");
		double penalite = voiture.getPenalite();
		prix += penalite; // increase price for penalized cars


		return prix;
	}

	// ------------------------------------------------
	//   Contester le prix superieur au budget
	// ----------------------------------------------
	public double contreProposition(Voiture voiture, double propositionVoiture) {

		LOGGER.info("[" + nom + "] La voiture " + voiture.getNom() + " propose " + propositionVoiture + " euros");

		double prixMin = calculerPrix(voiture.getDureePrevueMs(), voiture.getProfil()) * 0.7;

		if (propositionVoiture >= prixMin) {
			LOGGER.info("[" + nom + "] Le parking accepte la contre-proposition : " + propositionVoiture + " euros");
			return propositionVoiture;
		}

		double contreOffre = (propositionVoiture + prixMin) / 2;
		LOGGER.info("[" + nom + "] Le parking propose une contre-offre : " + contreOffre + " euros");

		return contreOffre;
	}

	public void log(String message) {
		LOGGER.info(message);
	}

	// -----------------------------
	//   STATIONNER
	// -----------------------------
	public void stationner(Voiture voiture) throws Exception {

		long startAttemptTime = System.currentTimeMillis();
		LOGGER.info("[" + nom + "] La voiture " + voiture.getNom() + " essaie de stationner");

		if (syncStrategy == 1) {
			placesSemaphore.acquire();
		} else {
			placesLock.lock();
		}

		try {
			int place = strategy.trouverPlace(tempPlace, placeOccupe, nbrPlaces);
			placeOccupe.set(place, true);

			int numColumns = (int) Math.ceil(Math.sqrt(nbrPlaces));
			int col = place % numColumns;
			int row = place / numColumns;

			// --- NOUVEAU : afficher la voiture ---
			// gui.showCarOnSlot(this, col, row, voiture.getNom());

			// --- ANIMATION : voiture réelle (AnimatedCar) roule vers la place ---
			AnimatedCar car = voiture.getAnimatedCar();
			if (car != null) {
				Platform.runLater(() -> {
					gui.getParkingView().sendCarToParking(car, this, place);
				});
			} else {
				// fallback : si jamais aucune AnimatedCar n'est associée, on ne fait que logger
				LOGGER.warning("[" + nom + "] Aucun AnimatedCar associé à " + voiture.getNom() + " (pas d'animation de parking).");
			}

			voiture.setPlace(place);

			LOGGER.info("[" + nom + "] La voiture " + voiture.getNom() + " a stationné !");
		} finally {
			if (syncStrategy == 2) {
				placesLock.unlock();
			}
		}

		long endAttemptTime = System.currentTimeMillis();
		long parkingWaitTime = endAttemptTime - startAttemptTime;

		LOGGER.info("[" + nom + "] Temps d'attente pour la voiture " + voiture.getNom() + " : " + parkingWaitTime + " millisecondes");
	}

	// -----------------------------
	//   SORTIR
	// -----------------------------
	public void sortir(Voiture voiture) {

		LOGGER.info("[" + nom + "] La voiture " + voiture.getNom() + " essaie de sortir");

		if (syncStrategy == 2) {
			placesLock.lock();
		}

		try {
			Thread.sleep(tempPlace.get(voiture.getPlace()));
			placeOccupe.set(voiture.getPlace(), false);

			int numColumns = (int) Math.ceil(Math.sqrt(nbrPlaces));
			int col = voiture.getPlace() % numColumns;
			int row = voiture.getPlace() / numColumns;

			// --- NOUVEAU : cacher la voiture ---
			// gui.hideCarOnSlot(this, col, row);

			// L'icône dans la case est désormais gérée par ParkingView.removeParkedCar
			// via startParkingTimer() déclenché à l'arrivée dans la place.

			Thread.sleep(7000 + new Random().nextInt(5000));

			LOGGER.info("La voiture " + voiture.getNom() + " est sorti !");
		} catch (Exception e) {
			LOGGER.info("[" + nom + "] La voiture " + voiture.getNom() + " est sortie !");
		} finally {
			if (syncStrategy == 1) {
				placesSemaphore.release();
			} else {
				placesLock.unlock();
			}
		}
	}
}
