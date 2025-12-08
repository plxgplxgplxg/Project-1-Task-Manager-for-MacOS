package com.plxg.activitymonitor.controller;

import com.plxg.activitymonitor.model.CpuProcessInfo;
import com.plxg.activitymonitor.service.CpuService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Label;
import javafx.util.Duration;


import java.awt.*;

public class CpuController {
    @FXML
    private TableView<CpuProcessInfo> cpuTable;

    @FXML
    private TableColumn<CpuProcessInfo, String> colProcessName;

    @FXML
    private TableColumn<CpuProcessInfo, Double> colCpuPercent;

    @FXML
    private TableColumn<CpuProcessInfo, String> colCpuTime;

    @FXML
    private  TableColumn<CpuProcessInfo, Integer> colThreads;

    @FXML
    private TableColumn<CpuProcessInfo, Integer> colIdleWakeUps;

    @FXML
    private TableColumn<CpuProcessInfo, String> colKind;

    @FXML
    private TableColumn<CpuProcessInfo, Double> colGpuPercent;

    @FXML
    private TableColumn<CpuProcessInfo, String> colGpuTime;

    @FXML
    private TableColumn<CpuProcessInfo, Integer> colPid;

    @FXML
    private TableColumn<CpuProcessInfo, String> colUser;

    @FXML
    private Label lblSystem;

    @FXML
    private Label lblUser;

    @FXML
    private Label lblIdle;

    @FXML
    private Label lblThreads;

    @FXML
    private Label lblProcesses;

    @FXML
    private AreaChart<Number, Number> cpuLoadChart;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private NumberAxis yAxis;




    private final ObservableList<CpuProcessInfo> processData =
            FXCollections.observableArrayList();

    //goi service cap nhat summary cpu o cuoi
    private  final CpuService cpuService = new CpuService();

    private  XYChart.Series<Number, Number> userSeries;
    private XYChart.Series<Number, Number> systemSeries;
    private int time = 0;



    @FXML
    private void initialize() {
        setupTableColumns();
        loadDummyData();
//        updateSummary(); mock data test ui
//        drawDummyChart(); mock data test ui

        NumberAxis xAxis = (NumberAxis) cpuLoadChart.getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(30);
        xAxis.setTickUnit(5);

        yAxis.setAutoRanging(true);

        setupChartSeries();



        startCpuRealtimeUpdate(xAxis);
    }


    private  void setupChartSeries() {
        cpuLoadChart.getData().clear();

        userSeries = new XYChart.Series<>();
        userSeries.setName("User");

        systemSeries = new XYChart.Series<>();
        systemSeries.setName("System");

        cpuLoadChart.getData().addAll(userSeries, systemSeries);
    }

    //css cho chart cho dep:
    private  void loadChartStylesheet() {
        try {
            String css = getClass().getResource("/css/chart-style.css").toExternalForm();
            cpuLoadChart.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.err.println("CSS file not found: /css/chart-style.css");
            e.printStackTrace();
        }
    }

    //cap nhat bieu do moi 500ms:
    private void startCpuRealtimeUpdate(NumberAxis xAxis) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(1000), event -> updateCpuData(xAxis) )
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateCpuData(NumberAxis xAxis) {
        CpuService.CpuStats stats = cpuService.getCpuStats();

        // summary panel
        lblUser.setText(String.format("%.1f%%", stats.user()));
        lblSystem.setText(String.format("%.1f%%", stats.system()));
        lblIdle.setText(String.format("%.1f%%", stats.idle()));

        //line chart cpu load:
        userSeries.getData().add(new XYChart.Data<>(time, stats.user()));
        systemSeries.getData().add(new XYChart.Data<>(time, stats.system()));
        time++;

        if (userSeries.getData().size() > 30) {
            userSeries.getData().remove(0);
            systemSeries.getData().remove(0 );

        }

        //di chuyen truc hoanh
        if (time > 30) {
            xAxis.setLowerBound(time - 30);
            xAxis.setUpperBound(time);
        }
        updateSummaryTableInfo();
    }

    private void updateSummaryTableInfo() {
        int totalThreads = processData.stream()
                .mapToInt(CpuProcessInfo::getThreads)
                .sum();

        int processes = processData.size();

        lblThreads.setText(String.valueOf(totalThreads));
        lblProcesses.setText(String.valueOf(processes));
    }

    private void setupTableColumns() {
        colProcessName.setCellValueFactory(
                new PropertyValueFactory<>("processName")
        );

        colCpuPercent.setCellValueFactory(
                new PropertyValueFactory<>("cpuPercent")
        );

        colCpuTime.setCellValueFactory(
                new PropertyValueFactory<>("cpuTime")
        );

        colThreads.setCellValueFactory(
                new PropertyValueFactory<>("threads")
        );

        colIdleWakeUps.setCellValueFactory(
                new PropertyValueFactory<>("idleWakeUps")
        );
        colKind.setCellValueFactory(
                new PropertyValueFactory<>("kind")
        );

        colGpuPercent.setCellValueFactory(
                new PropertyValueFactory<>("gpuPercent")
        );
        colGpuTime.setCellValueFactory(
                new PropertyValueFactory<>("gpuTime")
        );
        colPid.setCellValueFactory(
                new PropertyValueFactory<>("pid")
        );
        colUser.setCellValueFactory(
                new PropertyValueFactory<>("user")
        );
        cpuTable.setItems(processData);
    }

    //mockupdata:
    private void loadDummyData() {
        processData.clear();

        processData.addAll(
                new CpuProcessInfo("IntelliJ IDEA", 12.5, "01:23:45",
                        80, 10, "Apple", 0.0, "00:12:34", 463, "plxg"),
                new CpuProcessInfo("Google Chrome", 25.3, "10:01:59",
                        120, 50, "Apple", 5.0, "01:05:12", 1486, "plxg"),
                new CpuProcessInfo("kernel_task", 8.1, "02:10:33",
                        500, 200, "Apple", 0.0, "00:00:00", 0, "root"),
                new CpuProcessInfo("Activity Monitor", 3.2, "00:10:05",
                        30, 5, "Apple", 0.0, "00:00:00", 2762, "plxg")
        );
    }

//    private void updateSummary() {
//        //mockup data
//        double system = 12.0;
//        double user = 25.0;
//        double idle = 65.0;
//
//        lblSystem.setText(String.format("%.1f%%", system));
//        lblUser.setText(String.format("%.1f%%", user));
//        lblIdle.setText(String.format("%.1f%%", idle));
//
//        int totalThreads = processData.stream()
//                .mapToInt(CpuProcessInfo::getThreads)
//                .sum();
//
//        int processes = processData.size();
//
//        lblThreads.setText(String.valueOf(totalThreads));
//        lblProcesses.setText(String.valueOf(processes));
//    }

    //linechart: cpu load

//    private void drawDummyChart() {
//        //xoa data cu neu co
//        cpuLoadChart.getData().clear();
//
//        XYChart.Series<Number, Number> userSeries = new XYChart.Series<>();
//        userSeries.setName("User");
//
//        XYChart.Series<Number, Number> systemSeries = new XYChart.Series<>();
//        systemSeries.setName("System");
//
//        //mock data:
//        for (int t = 0; t <= 30; t++) {
//            userSeries.getData().add(
//                    new XYChart.Data<>(t, 20 + Math.sin(t / 5.0) * 5)
//            );
//            systemSeries.getData().add(
//                    new XYChart.Data<>(t, 5 + Math.sin(t / 6.0) *2)
//            );
//        }
//        cpuLoadChart.getData().addAll(userSeries, systemSeries);
//    }

}
