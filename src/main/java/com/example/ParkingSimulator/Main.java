package com.example.ParkingSimulator;

import com.example.ParkingSimulator.model.Parking;
import com.example.ParkingSimulator.model.Voiture;
import com.example.ParkingSimulator.model.ProfilUsager;
import com.example.ParkingSimulator.strategy.DefaultStrategy;
import com.example.ParkingSimulator.strategy.IStrategy;
import com.example.ParkingSimulator.view.AnimatedCar;
import com.example.ParkingSimulator.view.ParkingSlot;
import com.example.ParkingSimulator.view.ParkingView;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main extends Application {

	private int syncStrategy;
	private int numCars;
	private int nbrPlaces;

	// -----------------------------
	//   LOG ENTRY MODEL
	// -----------------------------
	public static class LogEntry {
		private final String timestamp;
		private final String voiture;
		private final String action;
		private final String message;
		private final String attente;
		private final String prix;
		private final String duree;

		public LogEntry(String timestamp, String voiture, String action,
				String message, String attente, String prix, String duree) {
			this.timestamp = timestamp;
			this.voiture = voiture;
			this.action = action;
			this.message = message;
			this.attente = attente;
			this.prix = prix;
			this.duree = duree;
		}

		public String getTimestamp() { return timestamp; }
		public String getVoiture() { return voiture; }
		public String getAction() { return action; }
		public String getMessage() { return message; }
		public String getAttente() { return attente; }
		public String getPrix() { return prix; }
		public String getDuree() { return duree; }
	}

	// ParkingView multi‑parking
	private ParkingView parkingView;

	@Override
	public void start(Stage primaryStage) {

		// -----------------------------
		//   USER INPUT
		// -----------------------------
		syncStrategy = getSyncStrategyFromUser();
		numCars = getNumFromUser("Nombre de voitures", "Entrez le nombre de voitures:");
		//nbrPlaces = getNumFromUser("Nombre de places de parking", "Entrez le nombre de places de parking:");
		nbrPlaces = 10;

		int numColumns = (int) Math.ceil(Math.sqrt(nbrPlaces));
		int numRows = (int) Math.ceil((double) nbrPlaces / numColumns);

		// -----------------------------
		//   START SIMULATION
		// -----------------------------
		IStrategy defaultStrategy = new DefaultStrategy();

		//******************list de 3 Parking
		List<Parking> parkings = new ArrayList<>();
		parkings.add(new Parking(nbrPlaces, defaultStrategy, this, syncStrategy, "P1"));
		parkings.add(new Parking(nbrPlaces, defaultStrategy, this, syncStrategy, "P2"));
		parkings.add(new Parking(nbrPlaces, defaultStrategy, this, syncStrategy, "P3"));

		// -----------------------------
		//   VIEW INITIALIZATION (multi‑parking)
		// -----------------------------
		ParkingView view = new ParkingView(parkings, numColumns, numRows, nbrPlaces, this);
		this.parkingView = view; // nécessaire pour setRectangleColor()

		StackPane root = view.getRoot();

		Scene scene = new Scene(root, 1200, 900);

		primaryStage.setTitle("Smart Parking System");
		primaryStage.setScene(scene);
		primaryStage.show();

		Random random = new Random();

		int arrivalDelay = 0;
		for (int t = 0; t < numCars; t++) {
			final int finalT = t;
			arrivalDelay += 3500 + random.nextInt(1000);
			// each car waits 1.5–3.5 seconds more than the previous one
			PauseTransition pause = new PauseTransition(Duration.millis(arrivalDelay));
			pause.setOnFinished(event -> {

				long dureePrevue = 10_000 + random.nextInt(20_000); // 10–30 sec

				ProfilUsager profil = ProfilUsager.values()[random.nextInt(ProfilUsager.values().length)];

				//****************** BUDGET RAND
				double budget = 2 + random.nextDouble() * 5; // entre 2€ et 7€
				budget = Math.round(budget * 10.0) / 10.0; // arrondi à 1 décimale

				Voiture v = new Voiture("Vec " + finalT, parkings, dureePrevue, profil, budget);

				// ----------------------------------------------------
				//   NOUVEAU : créer la AnimatedCar réelle sur la route
				// ----------------------------------------------------
				AnimatedCar car = parkingView.spawnRoadCar(v);
				v.setAnimatedCar(car);

				// ----------------------------------------------------
				//   Lancer le thread logique de la voiture
				// ----------------------------------------------------
				v.start();
			});
			pause.play();
		}
	}

	// -----------------------------
	//   LOG PARSER
	// -----------------------------
	public List<LogEntry> parseLogEntries(String filePath) {
		List<LogEntry> entries = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			String timestamp = null;

			while ((line = br.readLine()) != null) {

				// Ligne timestamp
				if (line.matches(".*\\d{4}.*Parking.*")) {
					timestamp = line.split("com")[0].trim();
					continue;
				}

				// Ligne INFO
				if (line.startsWith("INFO:")) {
					String msg = line.substring(6).trim();

					// --- VOITURE ---
					String voiture = "—";
					if (msg.contains("Vec")) {
						String[] parts = msg.split("Vec ");
						if (parts.length > 1) {
							voiture = parts[1].split(" ")[0];
						}
					}

					// --- ACTION ---
					String action = "—";
					if (msg.contains("essaie de sortir")) action = "Essaie de sortir";
					else if (msg.contains("est sortie")) action = "Sortie";
					else if (msg.contains("essaie de stationner")) action = "Essaie d'entrer";
					else if (msg.contains("a stationné")) action = "Entrée";
					else if (msg.contains("demande un prix")) action = "Demande de prix";
					else if (msg.contains("Prix proposé")) action = "Prix proposé";
					else if (msg.contains("refuse le prix")) action = "Refus";
					else if (msg.contains("contre-offre")) action = "Contre-offre";
					else if (msg.contains("accepte la contre-offre")) action = "Acceptation";
					else if (msg.contains("abandonne")) action = "Abandon";

					// --- TEMPS D'ATTENTE ---
					String attente = "—";
					if (msg.contains("Temps d'attente")) {
						String[] parts = msg.split(":");
						if (parts.length > 1) {
							attente = parts[1].replace("millisecondes", "").trim();
						}
					}

					// --- PRIX PROPOSÉ ---
					String prix = "—";
					if (msg.contains("Prix proposé")) {
						String[] parts = msg.split(":");
						if (parts.length > 1) {
							prix = parts[1].replace("euros", "").trim();
						}
					}

					// --- DURÉE DEMANDÉE ---
					String duree = "—";
					if (msg.contains("demande un prix pour")) {
						String[] parts = msg.split("demande un prix pour");
						if (parts.length > 1) {
							duree = parts[1].replace("ms", "").replace("millisecondes", "").trim();
						}
					}

					entries.add(new LogEntry(timestamp, voiture, action, msg, attente, prix, duree));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return entries;
	}

	// -----------------------------
	//   UI HELPERS (MULTI‑PARKING)
	// -----------------------------
	public void showCarOnSlot(Parking p, int col, int row, String name) {
		ParkingSlot[][] matrix = parkingView.getMatrix(p);
		matrix[col][row].showCar(name);
	}

	public void hideCarOnSlot(Parking p, int col, int row) {
		ParkingSlot[][] matrix = parkingView.getMatrix(p);
		matrix[col][row].hideCar();
	}

	private int getSyncStrategyFromUser() {
		TextInputDialog dialog = new TextInputDialog("1");
		dialog.setTitle("Choix de la stratégie");
		dialog.setHeaderText("Sélectionnez la stratégie de synchronisation");
		dialog.setContentText("1 = Sémaphore, 2 = Mutex");
		return dialog.showAndWait().map(Integer::parseInt).orElse(1);
	}

	private int getNumFromUser(String title, String content) {
		TextInputDialog dialog = new TextInputDialog("10");
		dialog.setTitle(title);
		dialog.setHeaderText(null);
		dialog.setContentText(content);
		return dialog.showAndWait().map(Integer::parseInt).orElse(10);
	}

	public ParkingView getParkingView() {
		return parkingView;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
