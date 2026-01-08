package com.plxg.activitymonitor.controller;

import com.plxg.activitymonitor.model.DiskProcessInfo;
import com.plxg.activitymonitor.service.DiskService;
import com.plxg.activitymonitor.util.FormatUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.util.Objects;

public class DiskController {

    @FXML
    private TableView<DiskProcessInfo> diskTable;

    @FXML
    private TableColumn<DiskProcessInfo, String> colProcessName;

    @FXML
    private TableColumn<DiskProcessInfo, String> colBytesWritten;

    @FXML
    private TableColumn<DiskProcessInfo, String> colBytesRead;

    @FXML
    private TableColumn<DiskProcessInfo, Integer> colPid;

    @FXML
    private TableColumn<DiskProcessInfo, String> colUser;

    @FXML
    private Label lblReadsIn;

    @FXML
    private Label lblWritesOut;

    @FXML
    private Label lblReadsInSec;

    @FXML
    private Label lblDataRead;

    @FXML
    private Label lblDataWritten;

    @FXML
    private Label lblDataReadSec;

    @FXML
    private StackedAreaChart<Number, Number> diskChart;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private void initialize() {
        setupTableColumns();

        NumberAxis xAxis = (NumberAxis) diskChart.getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(30);
        xAxis.setTickUnit(5);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(10);

        setupChartSeries();
        loadChartStylesheet();
        startDiskRealtimeUpdate(xAxis);
    }

    private final ObservableList<DiskProcessInfo> processData =
            FXCollections.observableArrayList();

    private final DiskService diskService = new DiskService();

    private XYChart.Series<Number, Number> readSeries;
    private XYChart.Series<Number, Number> writeSeries;
    private int time = 0;

    private void setupChartSeries() {
        diskChart.getData().clear();

        readSeries = new XYChart.Series<>();
        readSeries.setName("Reads");

        writeSeries = new XYChart.Series<>();
        writeSeries.setName("Writes");

        diskChart.getData().addAll(readSeries, writeSeries);
    }

    private void loadChartStylesheet() {
        try {
            String css = Objects.requireNonNull(getClass().getResource("/css/chart-style.css")).toExternalForm();
            diskChart.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.err.println("CSS file not found: /css/chart-style.css");
            e.printStackTrace();
        }
    }

    private void startDiskRealtimeUpdate(NumberAxis xAxis) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(2000), event -> {
                    updateDiskData(xAxis);
                    updateProcessTable();
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateDiskData(NumberAxis xAxis) {
        DiskService.DiskStats stats = diskService.getDiskStats();

        lblReadsIn.setText(String.valueOf(stats.totalReads()));
        lblWritesOut.setText(String.valueOf(stats.totalWrites()));
        lblReadsInSec.setText("0");
        lblDataRead.setText(FormatUtils.formatBytes(stats.totalReads()));
        lblDataWritten.setText(FormatUtils.formatBytes(stats.totalWrites()));
        lblDataReadSec.setText("0");

        double readPercent = Math.min(50, (stats.totalReads() % 100));
        double writePercent = Math.min(50, (stats.totalWrites() % 100));

        readSeries.getData().add(new XYChart.Data<>(time, readPercent));
        writeSeries.getData().add(new XYChart.Data<>(time, writePercent));
        time++;

        if (readSeries.getData().size() > 30) {
            readSeries.getData().remove(0);
            writeSeries.getData().remove(0);
        }

        if (time > 30) {
            xAxis.setLowerBound(time - 30);
            xAxis.setUpperBound(time);
        }
    }

    private void updateProcessTable() {
        var list = diskService.getProcessesByDiskIO();
        processData.clear();
        processData.addAll(list);
    }

    private void setupTableColumns() {
        colProcessName.setCellValueFactory(
                new PropertyValueFactory<>("processName")
        );

        colBytesWritten.setCellValueFactory(cellData -> {
            long bytes = cellData.getValue().getBytesWritten();
            return new SimpleStringProperty(FormatUtils.formatBytes(bytes));
        });

        colBytesRead.setCellValueFactory(cellData -> {
            long bytes = cellData.getValue().getBytesRead();
            return new SimpleStringProperty(FormatUtils.formatBytes(bytes));
        });

        colPid.setCellValueFactory(
                new PropertyValueFactory<>("pid")
        );

        colUser.setCellValueFactory(
                new PropertyValueFactory<>("user")
        );

        diskTable.setItems(processData);
    }
}
