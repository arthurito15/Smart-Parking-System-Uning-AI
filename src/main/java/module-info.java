module com.example.ParkingSimulator {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.logging;

    opens com.example.ParkingSimulator to javafx.fxml;
    exports com.example.ParkingSimulator;
  exports com.example.ParkingSimulator.model;
  opens com.example.ParkingSimulator.model to javafx.fxml;
  exports com.example.ParkingSimulator.strategy;
  opens com.example.ParkingSimulator.strategy to javafx.fxml;
  exports com.example.ParkingSimulator.view;
  opens com.example.ParkingSimulator.view to javafx.fxml;
}
