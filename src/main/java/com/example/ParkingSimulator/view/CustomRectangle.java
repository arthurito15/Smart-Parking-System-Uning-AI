package com.example.ParkingSimulator.view;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class CustomRectangle extends StackPane {
    private final Rectangle rect;
    private final Text label;

    public CustomRectangle() {
        rect = new Rectangle(60, 60);
        rect.setArcWidth(15);
        rect.setArcHeight(15);
        rect.setStroke(Color.DARKGRAY);
        rect.setStrokeWidth(2);
        rect.setFill(Color.LIGHTGRAY);
        rect.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0.5, 0, 1);");

        label = new Text("");
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        getChildren().addAll(rect, label);
        setStyle("-fx-cursor: hand;");
    }

    public void setColor(Color color) {
        rect.setFill(color);
    }

    public void setText(String text) {
        label.setText(text);
    }
}
