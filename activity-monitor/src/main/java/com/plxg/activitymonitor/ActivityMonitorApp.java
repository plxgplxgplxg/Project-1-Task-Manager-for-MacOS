package com.plxg.activitymonitor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ActivityMonitorApp extends Application
{
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ui/Main.fxml")
        );

        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("Activity Monitor");
        stage.show();
    }

    public static void main(String[] args )
    {
        launch(args);
    }
}
