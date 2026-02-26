# Smart-Parking-System-Uning-AI
Ce projet simule un système de parkings intelligents où plusieurs voitures négocient en parallèle avec plusieurs parkings pour obtenir une place au meilleur prix.
La simulation combine :

Agents logiques (voitures, parkings, stratégies)

Animations JavaFX (voitures sur la route, entrée dans les parkings)

Négociation Contract‑Net (CFP, propositions, contre‑offres)

Pénalités pour les voitures qui refusent des offres

Logs en temps réel affichés dans une fenêtre dédiée

#🧩 Fonctionnalités principales
#🚗 Voitures (agents)
Générées progressivement avec un budget, un profil et une durée de stationnement.

Négocient avec tous les parkings en parallèle.

Sélectionnent l’offre la moins chère.

Acceptent ou refusent selon leur budget.

Reçoivent une pénalité si elles refusent une offre.

Se garent, attendent, puis quittent définitivement (pas de retour).

#🅿️ Parkings
Plusieurs parkings indépendants (P1, P2, P3…).

Chaque parking propose un prix basé sur une stratégie.

Gèrent leurs places via une grille visuelle.

Affichent les voitures garées et libèrent les places automatiquement.

#🎞️ Animation JavaFX
Voitures animées sur la route (gauche → droite ou droite → gauche).

Animation d’approche, alignement, entrée dans la place.

Disparition après stationnement.

#📜 Logs en temps réel
Fenêtre dédiée affichant :

Timestamp

Voiture

Action

Message

Prix proposé

Durée demandée

Temps d’attente

Mise à jour automatique toutes les secondes.

#🏗️ Architecture du projet
src/
 ├── model/
 │    ├── Voiture.java
 │    ├── Parking.java
 │    ├── ProfilUsager.java
 │    └── strategy/
 │         ├── IStrategy.java
 │         └── DefaultStrategy.java
 │
 ├── view/
 │    ├── ParkingView.java
 │    ├── AnimatedCar.java
 │    ├── ParkingSlot.java
 │    └── TrafficCar.java
 │
 └── Main.java
 
#⚙️ Lancement de la simulation
Prérequis
Java 17+

JavaFX 17+

Maven ou Gradle (optionnel)

Exécution
Depuis un IDE :
Run Main.java
