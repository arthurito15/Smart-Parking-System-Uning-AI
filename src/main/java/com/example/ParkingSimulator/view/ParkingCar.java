package com.example.ParkingSimulator.view;

import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class ParkingCar extends ImageView {

  public ParkingCar(String imageName) {
    super(new Image(ParkingCar.class.getResource("/" + imageName).toExternalForm()));
    setFitWidth(80);
    setPreserveRatio(true);
  }

  public void switchToParkedImage() {
    setImage(new Image(ParkingCar.class.getResource("/car.png").toExternalForm()));
  }

  public void driveTo(double x1, double y1, double x2, double y2, Runnable onArrive) {
    TranslateTransition t1 = new TranslateTransition(Duration.seconds(1.5), this);
    t1.setToX(x1);
    t1.setToY(y1);

    TranslateTransition t2 = new TranslateTransition(Duration.seconds(1.5), this);
    t2.setToX(x2);
    t2.setToY(y2);

    t1.setOnFinished(e -> {
      // 🔁 Changer l’image juste avant d’entrer dans la place
      switchToParkedImage();
      t2.play();
    });

    t2.setOnFinished(e -> onArrive.run());

    t1.play(); // on démarre par le mouvement horizontal
  }

}
