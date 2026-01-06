package com.plxg.activitymonitor.controller;

import com.plxg.activitymonitor.model.MemoryProcessInfo;
import com.plxg.activitymonitor.service.MemoryService;
import com.plxg.activitymonitor.util.FormatUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.util.Objects;


public class MemoryController {

    @FXML
    private TableView<MemoryProcessInfo> memoryTable;

    @FXML
    private TableColumn<MemoryProcessInfo, String> colProcessName;

    @FXML
    private TableColumn<MemoryProcessInfo, String> colMemory;

    @FXML
    private TableColumn<MemoryProcessInfo, Integer> colThreads;

    @FXML
    private TableColumn<MemoryProcessInfo, Integer> colPorts;

    @FXML
    private TableColumn<MemoryProcessInfo, Integer> colPid;

    @FXML
    private TableColumn<MemoryProcessInfo, String> colUser;

    @FXML
    private AreaChart<Number, Number> memoryPressureChart;

    @FXML
    private Label lblPhysicalMemory;

    @FXML
    private Label lblMemoryUsed;

    @FXML
    private Label lblCachedFiles;

    @FXML
    private Label lblAppMemory;

    @FXML
    private Label lblWiredMemory;

    @FXML
    private Label lblCompressed;

    @FXML
    private Label lblSwapUsed;

    private final ObservableList<MemoryProcessInfo> processData = FXCollections.observableArrayList();
    private final MemoryService memoryService = new MemoryService();
    private XYChart.Series<Number, Number> memorySeries;
    private int time = 0;

    @FXML
    private void initialize() {
        setUpTableColumns();
        setUpChart();
        loadChartStylesheet();
        startRealtimeUpdate();
    }

    //setup column trong tableview:
    private void setUpTableColumns() {
        colProcessName.setCellValueFactory(new PropertyValueFactory<>("processName"));

        //format memory sang don vi phu hop
        colMemory.setCellValueFactory(cellData -> {
            long bytes = cellData.getValue().getMemoryBytes();
            return new SimpleStringProperty(FormatUtils.formatBytes(bytes));
        });

        colThreads.setCellValueFactory(new PropertyValueFactory<>("threads"));
        colPorts.setCellValueFactory(new PropertyValueFactory<>("ports"));
        colPid.setCellValueFactory(new PropertyValueFactory<>("pid"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("user"));

        memoryTable.setItems(processData);
    }

    private void setUpChart() {
        memoryPressureChart.getData().clear();

        // Setup trục X
        NumberAxis xAxis = (NumberAxis) memoryPressureChart.getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(30);
        xAxis.setTickUnit(5);

        // Setup trục Y
        NumberAxis yAxis = (NumberAxis) memoryPressureChart.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(10);

        memorySeries = new XYChart.Series<>();
        memorySeries.setName("Memory Pressure");
        memoryPressureChart.getData().add(memorySeries);
    }

    private void loadChartStylesheet() {
        try {
            String css = Objects.requireNonNull(getClass().getResource("/css/chart-style.css")).toExternalForm();
            memoryPressureChart.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.err.println("CSS file not found: /css/chart-style.css");
        }
    }

    //update realtime moi 2s:
    private void startRealtimeUpdate() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(2000), event -> {
                    updateMemoryData();
                    updateProcessTable();
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        //update lan dau:
        updateMemoryData();
        updateProcessTable();

    }

    //cap nhat ttin memory:
    private void updateMemoryData() {
        MemoryService.MemoryStats stats = memoryService.getMemoryStats();

        //label:
        lblPhysicalMemory.setText(FormatUtils.formatBytes(stats.totalMemory()));
        lblMemoryUsed.setText(FormatUtils.formatBytes(stats.usedMemory()));
        lblCachedFiles.setText(FormatUtils.formatBytes(stats.cachedMemory()));
        lblSwapUsed.setText(FormatUtils.formatBytes(stats.swapUsed()));

        //MACOS ko cung cap api cho app/wired/compressed memory
        lblAppMemory.setText("N/A");
        lblWiredMemory.setText("N/A");
        lblCompressed.setText("N/A");

        //update chart
        double pressurePercent = (double) stats.usedMemory() / stats.totalMemory() * 100;
        memorySeries.getData().add(new XYChart.Data<>(time, pressurePercent));
        time++;

        if (memorySeries.getData().size() > 30) {
            memorySeries.getData().remove(0);
        }

        // di chuyen truc x khi vuot qua 30 diem
        NumberAxis xAxis = (NumberAxis) memoryPressureChart.getXAxis();
        if (time > 30) {
            xAxis.setLowerBound(time - 30);
            xAxis.setUpperBound(time);
        }
    }

    //update table
    private void updateProcessTable() {
        var processes = memoryService.getProcessesByMemory();
        processData.clear();
        processData.addAll(processes);
    }
}
