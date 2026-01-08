package com.plxg.activitymonitor.controller;

import com.plxg.activitymonitor.model.NetworkProcessInfo;
import com.plxg.activitymonitor.service.NetworkService;
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

public class NetworkController {

    @FXML
    private TableView<NetworkProcessInfo> networkTable;

    @FXML
    private TableColumn<NetworkProcessInfo, String> colProcessName;

    @FXML
    private TableColumn<NetworkProcessInfo, String> colSentBytes;

    @FXML
    private TableColumn<NetworkProcessInfo, String> colRcvdBytes;

    @FXML
    private TableColumn<NetworkProcessInfo, Long> colSentPackets;

    @FXML
    private TableColumn<NetworkProcessInfo, Long> colRcvdPackets;

    @FXML
    private TableColumn<NetworkProcessInfo, Integer> colPid;

    @FXML
    private TableColumn<NetworkProcessInfo, String> colUser;

    @FXML
    private Label lblPacketsIn;

    @FXML
    private Label lblPacketsOut;

    @FXML
    private Label lblPacketsInSec;

    @FXML
    private Label lblDataReceived;

    @FXML
    private Label lblDataSent;

    @FXML
    private Label lblDataReceivedSec;

    @FXML
    private StackedAreaChart<Number, Number> networkChart;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private void initialize() {
        setupTableColumns();

        NumberAxis xAxis = (NumberAxis) networkChart.getXAxis();
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
        startNetworkRealtimeUpdate(xAxis);
    }

    private final ObservableList<NetworkProcessInfo> processData =
            FXCollections.observableArrayList();

    private final NetworkService networkService = new NetworkService();

    private XYChart.Series<Number, Number> rcvdSeries;
    private XYChart.Series<Number, Number> sentSeries;
    private int time = 0;

    private void setupChartSeries() {
        networkChart.getData().clear();

        rcvdSeries = new XYChart.Series<>();
        rcvdSeries.setName("Rcvd");

        sentSeries = new XYChart.Series<>();
        sentSeries.setName("Sent");

        networkChart.getData().addAll(rcvdSeries, sentSeries);
    }

    private void loadChartStylesheet() {
        try {
            String css = Objects.requireNonNull(getClass().getResource("/css/chart-style.css")).toExternalForm();
            networkChart.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.err.println("CSS file not found: /css/chart-style.css");
            e.printStackTrace();
        }
    }

    private void startNetworkRealtimeUpdate(NumberAxis xAxis) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(2000), event -> {
                    updateNetworkData(xAxis);
                    updateProcessTable();
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateNetworkData(NumberAxis xAxis) {
        NetworkService.NetworkStats stats = networkService.getNetworkStats();

        lblPacketsIn.setText(String.valueOf(stats.packetsRcvd()));
        lblPacketsOut.setText(String.valueOf(stats.packetsSent()));
        lblPacketsInSec.setText("0");
        lblDataReceived.setText(FormatUtils.formatBytes(stats.bytesRcvd()));
        lblDataSent.setText(FormatUtils.formatBytes(stats.bytesSent()));
        lblDataReceivedSec.setText("0");

        double rcvdPercent = Math.min(50, (stats.bytesRcvd() % 100));
        double sentPercent = Math.min(50, (stats.bytesSent() % 100));

        rcvdSeries.getData().add(new XYChart.Data<>(time, rcvdPercent));
        sentSeries.getData().add(new XYChart.Data<>(time, sentPercent));
        time++;

        if (rcvdSeries.getData().size() > 30) {
            rcvdSeries.getData().remove(0);
            sentSeries.getData().remove(0);
        }

        if (time > 30) {
            xAxis.setLowerBound(time - 30);
            xAxis.setUpperBound(time);
        }
    }

    private void updateProcessTable() {
        var list = networkService.getProcessesByNetworkIO();
        processData.clear();
        processData.addAll(list);
    }

    private void setupTableColumns() {
        colProcessName.setCellValueFactory(
                new PropertyValueFactory<>("processName")
        );

        colSentBytes.setCellValueFactory(cellData -> {
            long bytes = cellData.getValue().getSentBytes();
            return new SimpleStringProperty(FormatUtils.formatBytes(bytes));
        });

        colRcvdBytes.setCellValueFactory(cellData -> {
            long bytes = cellData.getValue().getRcvdBytes();
            return new SimpleStringProperty(FormatUtils.formatBytes(bytes));
        });

        colSentPackets.setCellValueFactory(
                new PropertyValueFactory<>("sentPackets")
        );

        colRcvdPackets.setCellValueFactory(
                new PropertyValueFactory<>("rcvdPackets")
        );

        colPid.setCellValueFactory(
                new PropertyValueFactory<>("pid")
        );

        colUser.setCellValueFactory(
                new PropertyValueFactory<>("user")
        );

        networkTable.setItems(processData);
    }
}
