package com.plxg.activitymonitor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ActivityMonitorApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/ui/main_layout.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 600);

        scene.getStylesheets().add(
            getClass().getResource("/styles.css").toExternalForm()
        );

        stage.setTitle("Activity Monitor");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);  
    }
}
