package com.example.ParkingSimulator.view;

import com.example.ParkingSimulator.model.Voiture;
import javafx.animation.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class AnimatedCar extends ImageView {

  public enum State {
    APPROACHING_HORIZONTAL,
    ALIGNING_VERTICAL,
    ENTERING_SLOT,
    PARKED
  }

  private State state = State.APPROACHING_HORIZONTAL;
  private final Voiture voiture;

  public AnimatedCar(Voiture voiture) {
    this.voiture = voiture;

    setImage(new Image(getClass().getResource("/carGoingRL.png").toExternalForm()));
    setFitWidth(90);
    setPreserveRatio(true);
  }

  public Voiture getVoiture() {
    return voiture;
  }

  // ----------------------------------------------------
  // 1) APPROCHE HORIZONTALE (route → devant parking)
  // ----------------------------------------------------
  public void startHorizontalApproach(double startX, double startY,
      double approachX, double approachY,
      Runnable onArrive) {

    state = State.APPROACHING_HORIZONTAL;

    setTranslateX(startX);
    setTranslateY(startY);

    TranslateTransition t = new TranslateTransition(Duration.seconds(2.0), this);
    t.setToX(approachX);
    t.setToY(approachY);

    t.setOnFinished(e -> onArrive.run());
    t.play();
  }

  // ----------------------------------------------------
  // 2) ALIGNEMENT VERTICAL (devant parking → hauteur du slot)
  // ----------------------------------------------------
  public void startVerticalAlignment(double alignX, double alignY, Runnable onArrive) {

    state = State.ALIGNING_VERTICAL;

    // Changer l’image avant d’entrer dans la zone du parking
    setImage(new Image(getClass().getResource("/car.png").toExternalForm()));

    TranslateTransition t = new TranslateTransition(Duration.seconds(1.2), this);
    t.setToX(alignX);
    t.setToY(alignY);

    t.setOnFinished(e -> onArrive.run());
    t.play();
  }

  // ----------------------------------------------------
  // 3) ENTRÉE DANS LA PLACE (aligné → slot)
  // ----------------------------------------------------
  public void goParkFinalStep(double slotX, double slotY, Runnable onArrive) {

    state = State.ENTERING_SLOT;

    TranslateTransition t = new TranslateTransition(Duration.seconds(1.2), this);
    t.setToX(slotX);
    t.setToY(slotY);

    t.setOnFinished(e -> {
      state = State.PARKED;
      onArrive.run();
    });

    t.play();
  }
}
