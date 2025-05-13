module Tavern {
    requires ice4j;
    requires io.github.eduramiba.webcamcapture;
    requires java.sdp.nist.bridge;
    requires java.sql;
    requires org.slf4j;
    requires sdp.api;
    requires webcam.capture;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    opens com.tavern.client.gui.screens to javafx.fxml;
    
    exports com.tavern.client;
    exports com.tavern.client.components;
    exports com.tavern.client.gui.screens;
    exports com.tavern.client.views;
    opens com.tavern.client.views to javafx.fxml;
    opens com.tavern.client.components to javafx.fxml;

}