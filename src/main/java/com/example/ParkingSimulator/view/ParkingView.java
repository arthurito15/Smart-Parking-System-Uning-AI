package com.example.ParkingSimulator.view;

import com.example.ParkingSimulator.Main;
import com.example.ParkingSimulator.model.Parking;
import com.example.ParkingSimulator.model.Voiture;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ParkingView {

  private final Map<Parking, ParkingSlot[][]> matrices = new HashMap<>();

  private final StackPane root = new StackPane();     // main container
  private final VBox parkingLayout = new VBox(20);    // holds parking grids
  private final Pane trafficLayer = new Pane();       // floating layer for moving cars
  private final Pane roadLayer = new Pane();          // background roads

  private final Random random = new Random();

  public ParkingView(List<Parking> parkings, int numColumns, int numRows, int nbrPlaces, Main controller) {

    parkingLayout.setPadding(new Insets(10));
    parkingLayout.setAlignment(Pos.CENTER);

    // -----------------------------
    //   UNE GRILLE PAR PARKING
    // -----------------------------
    HBox firstLine = new HBox(40);
    firstLine.setAlignment(Pos.CENTER);

    HBox secondLine = new HBox(40);
    secondLine.setAlignment(Pos.CENTER);

    for (int i = 0; i < parkings.size(); i++) {
      Parking p = parkings.get(i);

      Label title = new Label("Parking " + p.getNom());
      title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

      GridPane grid = new GridPane();
      grid.setPadding(new Insets(10));
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setAlignment(Pos.CENTER);

      ParkingSlot[][] matrix = new ParkingSlot[numColumns][numRows];
      matrices.put(p, matrix);

      for (int row = 0; row < numRows; row++) {
        for (int col = 0; col < numColumns; col++) {
          ParkingSlot spot = new ParkingSlot();

          spot.setOnMouseEntered(e -> {
            spot.setScaleX(1.05);
            spot.setScaleY(1.05);
          });
          spot.setOnMouseExited(e -> {
            spot.setScaleX(1.0);
            spot.setScaleY(1.0);
          });

          matrix[col][row] = spot;
          grid.add(spot, col, row);

          if (row * numColumns + col < nbrPlaces) {
            matrix[col][row].hideCar(); // place libre = fond vert
          } else {
            matrix[col][row].setColor(Color.GREY); // hors grille
          }
        }
      }

      VBox parkingBox = new VBox(10, title, grid);
      parkingBox.setAlignment(Pos.CENTER);

      if (i < 2) {
        firstLine.getChildren().add(parkingBox);
      } else {
        secondLine.getChildren().add(parkingBox);
      }
    }

    parkingLayout.getChildren().addAll(firstLine, secondLine);

    // -----------------------------
    //   ROAD BACKGROUND (LEFT + RIGHT)
    // -----------------------------
    Image roadImg = new Image(getClass().getResource("/road.jpg").toExternalForm());

    ImageView roadLeft = new ImageView(roadImg);
    roadLeft.setFitWidth(120);
    roadLeft.setPreserveRatio(false);
    roadLeft.setFitHeight(1000);
    roadLeft.setTranslateX(0);

    ImageView roadRight = new ImageView(roadImg);
    roadRight.setFitWidth(120);
    roadRight.setPreserveRatio(false);
    roadRight.setFitHeight(1000);

    root.sceneProperty().addListener((obs, oldScene, newScene) -> {
      if (newScene != null) {

        newScene.heightProperty().addListener((o, oldH, newH) -> {
          double h = newH.doubleValue();
          roadLeft.setFitHeight(h);
          roadRight.setFitHeight(h);
        });

        newScene.widthProperty().addListener((o, oldW, newW) -> {
          roadRight.setTranslateX(newW.doubleValue() - 120);
        });
      }
    });

    roadLayer.getChildren().addAll(roadLeft, roadRight);

    root.getChildren().addAll(parkingLayout, roadLayer, trafficLayer);

    // -----------------------------
    //   TRAFFIC SIMULATION (LEFT + RIGHT SIDES)
    // -----------------------------
    root.sceneProperty().addListener((obs, oldScene, newScene) -> {
      if (newScene != null) {

        newScene.heightProperty().addListener((o, oldH, newH) -> {

          double sceneHeight = newH.doubleValue();

          // LEFT SIDE: cars move TOP → BOTTOM
          for (int i = 0; i < 3; i++) {

            PauseTransition delay = new PauseTransition(Duration.seconds(i * 2));

            delay.setOnFinished(ev -> {
              TrafficCar carLeft = new TrafficCar(true);
              trafficLayer.getChildren().add(carLeft);
              carLeft.startMoving(sceneHeight, 20);
            });

            delay.play();
          }

          // RIGHT SIDE: cars move BOTTOM → TOP
          for (int i = 0; i < 3; i++) {

            PauseTransition delay = new PauseTransition(Duration.seconds(i * 2));

            delay.setOnFinished(ev -> {
              TrafficCar carRight = new TrafficCar(false);
              trafficLayer.getChildren().add(carRight);
              carRight.startMoving(sceneHeight, newScene.getWidth() - 100);
            });

            delay.play();
          }
        });
      }
    });

    // -----------------------------
    //   LOG WINDOW (NOUVEAU)
    // -----------------------------
    Stage logStage = new Stage();
    logStage.setTitle("Logs du Parking");

    VBox logBox = new VBox(10);
    logBox.setPadding(new Insets(10));
    logBox.setAlignment(Pos.CENTER);

    for (Parking p : parkings) {
      TitledPane pane = createLogPaneForParking(p.getNom(), controller);
      logBox.getChildren().add(pane);
    }

    Scene logScene = new Scene(logBox, 1000, 550);
    logStage.setScene(logScene);
    logStage.show();
  }

  // -----------------------------
  //   LOG PANE PAR PARKING
  // -----------------------------
  private TitledPane createLogPaneForParking(String parkingName, Main controller) {

    TableView<Main.LogEntry> table = new TableView<>();
    table.setPrefHeight(300);

    TableColumn<Main.LogEntry, String> timeCol = new TableColumn<>("Horodatage");
    timeCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
    timeCol.setPrefWidth(150);

    TableColumn<Main.LogEntry, String> voitureCol = new TableColumn<>("Voiture");
    voitureCol.setCellValueFactory(new PropertyValueFactory<>("voiture"));
    voitureCol.setPrefWidth(100);

    TableColumn<Main.LogEntry, String> actionCol = new TableColumn<>("Action");
    actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
    actionCol.setPrefWidth(150);

    TableColumn<Main.LogEntry, String> msgCol = new TableColumn<>("Message");
    msgCol.setCellValueFactory(new PropertyValueFactory<>("message"));
    msgCol.setPrefWidth(300);

    TableColumn<Main.LogEntry, String> attenteCol = new TableColumn<>("Attente (ms)");
    attenteCol.setCellValueFactory(new PropertyValueFactory<>("attente"));
    attenteCol.setPrefWidth(120);

    TableColumn<Main.LogEntry, String> prixCol = new TableColumn<>("Prix (€)");
    prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
    prixCol.setPrefWidth(100);

    TableColumn<Main.LogEntry, String> dureeCol = new TableColumn<>("Durée prévue (ms)");
    dureeCol.setCellValueFactory(new PropertyValueFactory<>("duree"));
    dureeCol.setPrefWidth(150);

    table.getColumns().addAll(
        timeCol, voitureCol, actionCol, msgCol,
        attenteCol, prixCol, dureeCol
    );

    Timeline refresher = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
      List<Main.LogEntry> logs = controller.parseLogEntries("../app.log");
      List<Main.LogEntry> filtered = logs.stream()
          .filter(entry -> entry.getMessage().contains("[" + parkingName + "]"))
          .toList();
      table.getItems().setAll(filtered);
    }));
    refresher.setCycleCount(Timeline.INDEFINITE);
    refresher.play();

    VBox content = new VBox(table);
    content.setPadding(new Insets(10));
    content.setAlignment(Pos.CENTER);

    TitledPane pane = new TitledPane("Parking " + parkingName, content);
    pane.setExpanded(false);

    return pane;
  }

  public StackPane getRoot() {
    return root;
  }

  public ParkingSlot[][] getMatrix(Parking p) {
    return matrices.get(p);
  }

  // -----------------------------
  //   CREATE REAL CAR ON ROAD
  // -----------------------------
  public AnimatedCar spawnRoadCar(Voiture v) {

    AnimatedCar car = new AnimatedCar(v);
    trafficLayer.getChildren().add(car);

    boolean appearLeft = random.nextBoolean();
    double startX = appearLeft ? 20 : root.getWidth() - 100;

    // Y3 : voie juste devant les parkings
    double parkingY = parkingLayout.localToScene(parkingLayout.getBoundsInLocal()).getMinY();
    double approachY = parkingY + 20;

    double startY = approachY;

    double approachX = root.getWidth() / 2;

    car.startHorizontalApproach(startX, startY, approachX, approachY, () -> {});

    return car;
  }

  // -----------------------------
  //   SEND REAL CAR TO PARKING
  // -----------------------------
  public void sendCarToParking(AnimatedCar car, Parking parking, int slotIndex) {

    ParkingSlot[][] matrix = matrices.get(parking);
    int numCols = matrix.length;

    int row = slotIndex / numCols;
    int col = slotIndex % numCols;

    ParkingSlot target = matrix[col][row];

    double[] coords = computeSlotCoordinates(target);
    double slotX = coords[0];
    double slotY = coords[1];

    // Y3 : voie juste devant les parkings
    double parkingY = parkingLayout.localToScene(parkingLayout.getBoundsInLocal()).getMinY();
    double approachY = parkingY + 20;

    // H2 + D3 : arrêt 120 px avant la place
    double approachX = slotX - 120;

    car.startHorizontalApproach(
        car.getTranslateX(),
        car.getTranslateY(),
        approachX,
        approachY,
        () -> {

          car.startVerticalAlignment(
              approachX,
              slotY,
              () -> {

                car.goParkFinalStep(slotX, slotY, () -> {
                  target.showCar(car.getVoiture().getNom());
                  trafficLayer.getChildren().remove(car);
                  startParkingTimer(parking, slotIndex);
                });
              }
          );
        }
    );
  }


  // -----------------------------
  //   COMPUTE SLOT COORDINATES
  // -----------------------------
  public double[] computeSlotCoordinates(ParkingSlot target) {
    double x = target.localToScene(target.getBoundsInLocal()).getMinX();
    double y = target.localToScene(target.getBoundsInLocal()).getMinY();
    return new double[]{x, y};
  }

  private void startParkingTimer(Parking parking, int slotIndex) {
    PauseTransition timer = new PauseTransition(Duration.seconds(5));
    timer.setOnFinished(e -> removeParkedCar(parking, slotIndex));
    timer.play();
  }

  public void removeParkedCar(Parking parking, int slotIndex) {
    ParkingSlot[][] matrix = matrices.get(parking);
    int numCols = matrix.length;

    int row = slotIndex / numCols;
    int col = slotIndex % numCols;

    ParkingSlot slot = matrix[col][row];
    slot.hideCar();
  }
}
