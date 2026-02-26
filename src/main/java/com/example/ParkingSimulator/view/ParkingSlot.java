package com.example.ParkingSimulator.view;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ParkingSlot extends StackPane {

  private final Rectangle asphalt;
  private final Line line1;
  private final Line line2;
  private final ImageView carIcon;
  private final Text label;

  public ParkingSlot() {

    // Asphalt background
    asphalt = new Rectangle(70, 120);
    asphalt.setFill(Color.web("#2b2b2b")); // dark asphalt
    asphalt.setStroke(Color.WHITE);
    asphalt.setStrokeWidth(3);

    // Parking white lines
    line1 = new Line(0, 0, 70, 0);
    line1.setStroke(Color.WHITE);
    line1.setStrokeWidth(3);
    line1.setTranslateY(-50);

    line2 = new Line(0, 0, 70, 0);
    line2.setStroke(Color.WHITE);
    line2.setStrokeWidth(3);
    line2.setTranslateY(50);

    // Car icon (hidden by default)
    carIcon = new ImageView(new Image(getClass().getResource("/car.png").toExternalForm()));
    carIcon.setFitWidth(50);
    carIcon.setPreserveRatio(true);
    carIcon.setVisible(false);

    // Optional text label
    label = new Text("");
    label.setFill(Color.WHITE);                  // red text
    label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");


    getChildren().addAll(asphalt, line1, line2, carIcon, label);
  }

  public void showCar(String name) {
    asphalt.setFill(Color.DARKGRAY); // optional: darker background when occupied
    carIcon.setFitWidth(90);
    carIcon.setPreserveRatio(true);
    carIcon.setVisible(true);
    label.setText(name);
  }


  public void hideCar() {
    carIcon.setVisible(false);
    label.setText("");
    asphalt.setFill(Color.GREEN); // <-- free slot stays green
  }

  public void setColor(Color color) {
    asphalt.setFill(color);
  }

}
