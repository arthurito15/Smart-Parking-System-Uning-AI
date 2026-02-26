package com.example.ParkingSimulator.view;

import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import java.util.Random;

public class TrafficCar extends ImageView {

  private final Random random = new Random();
  private final boolean moveDown; // true = top→bottom, false = bottom→top

  public TrafficCar(boolean moveDown) {
    super();

    this.moveDown = moveDown;

    // Choose the correct image depending on direction
    String img = moveDown ? "/carTB.png" : "/car.png";
    setImage(new Image(TrafficCar.class.getResource(img).toExternalForm()));

    setFitWidth(90);          // same size as parked cars
    setPreserveRatio(true);
  }


  public void startMoving(double sceneHeight, double xPosition) {

    double startY = moveDown ? -120 : sceneHeight + 120;
    double endY   = moveDown ? sceneHeight + 120 : -120;

    setTranslateX(xPosition);
    setTranslateY(startY);

    TranslateTransition tt = new TranslateTransition();
    tt.setNode(this);
    tt.setDuration(Duration.seconds(10)); // 3 sec
    tt.setFromY(startY);
    tt.setToY(endY);

    tt.setOnFinished(e -> startMoving(sceneHeight, xPosition)); // loop forever
    tt.play();
  }
}
