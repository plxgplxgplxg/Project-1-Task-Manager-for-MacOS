package com.example;

import javafx.util.Duration;
import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class Main extends javafx.application.Application{

    private Random random = new Random();

    @Override
    public void start(Stage stage) throws Exception {
        
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time (s)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Random Linechart");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Random data");

        lineChart.getData().add(series);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {

                int y = random.nextInt(101);

                for (XYChart.Data<Number, Number> d : series.getData()) {
                    d.setXValue(d.getXValue().intValue() -1);
                }

                series.getData().add(new XYChart.Data<>(30, y));

                series.getData().removeIf(d -> d.getXValue().intValue() < 0);
            })
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Scene scene = new Scene(lineChart, 600, 400);
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

   
}
