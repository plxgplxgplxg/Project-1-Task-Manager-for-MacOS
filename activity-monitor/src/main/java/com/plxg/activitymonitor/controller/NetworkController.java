package com.plxg.activitymonitor.controller;

import com.plxg.activitymonitor.model.NetworkProcessInfo;
import com.plxg.activitymonitor.service.NetworkService;
import com.plxg.activitymonitor.service.ProcessKillService;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.util.Objects;
import java.util.Optional;

public class NetworkController {

    private String filterText = "";

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
    private Label lblPacketsOutSec;

    @FXML
    private Label lblDataReceived;

    @FXML
    private Label lblDataSent;

    @FXML
    private Label lblDataReceivedSec;

    @FXML
    private Label lblDataSentSec;

    @FXML
    private AreaChart<Number, Number> networkChart;

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

        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(true);

        setupChartSeries();
        loadChartStylesheet();
        startNetworkRealtimeUpdate(xAxis);
    }

    private final ObservableList<NetworkProcessInfo> processData =
            FXCollections.observableArrayList();

    private final NetworkService networkService = new NetworkService();
    private final ProcessKillService killService = new ProcessKillService();

    private XYChart.Series<Number, Number> packetsInSeries;
    private XYChart.Series<Number, Number> packetsOutSeries;
    private int time = 0;
    
    // luu packets truoc do de tinh /sec
    private long previousPacketsIn = 0;
    private long previousPacketsOut = 0;
    private long previousBytesIn = 0;
    private long previousBytesOut = 0;

    private void setupChartSeries() {
        networkChart.getData().clear();
        networkChart.getStyleClass().add("network-packets-chart");

        packetsInSeries = new XYChart.Series<>();
        packetsInSeries.setName("Packets In");

        packetsOutSeries = new XYChart.Series<>();
        packetsOutSeries.setName("Packets Out");

        networkChart.getData().addAll(packetsInSeries, packetsOutSeries);
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

        lblPacketsIn.setText(FormatUtils.formatNumber(stats.packetsRcvd()));
        lblPacketsOut.setText(FormatUtils.formatNumber(stats.packetsSent()));
        
        long packetsInPerSec = (stats.packetsRcvd() - previousPacketsIn) / 2;
        long packetsOutPerSec = (stats.packetsSent() - previousPacketsOut) / 2;
        
        long bytesInPerSec = (stats.bytesRcvd() - previousBytesIn) / 2;
        long bytesOutPerSec = (stats.bytesSent() - previousBytesOut) / 2;
        
        lblPacketsInSec.setText(String.valueOf(Math.max(0, packetsInPerSec)));
        lblPacketsOutSec.setText(String.valueOf(Math.max(0, packetsOutPerSec)));
        
        lblDataReceived.setText(FormatUtils.formatBytes(stats.bytesRcvd()));
        lblDataSent.setText(FormatUtils.formatBytes(stats.bytesSent()));
        lblDataReceivedSec.setText(FormatUtils.formatBytes(Math.max(0, bytesInPerSec)));
        lblDataSentSec.setText(FormatUtils.formatBytes(Math.max(0, bytesOutPerSec)));

        packetsInSeries.getData().add(new XYChart.Data<>(time, Math.max(0, packetsInPerSec)));
        packetsOutSeries.getData().add(new XYChart.Data<>(time, -Math.max(0, packetsOutPerSec)));
        
        previousPacketsIn = stats.packetsRcvd();
        previousPacketsOut = stats.packetsSent();
        previousBytesIn = stats.bytesRcvd();
        previousBytesOut = stats.bytesSent();
        
        time++;

        if (packetsInSeries.getData().size() > 30) {
            packetsInSeries.getData().remove(0);
            packetsOutSeries.getData().remove(0);
        }

        if (time > 30) {
            xAxis.setLowerBound(time - 30);
            xAxis.setUpperBound(time);
        }
    }

    private void updateProcessTable() {
        var list = networkService.getProcessesByNetworkIO();
        processData.clear();
        for (var p : list) {
            if (filterText.isEmpty() || p.getProcessName().toLowerCase().contains(filterText)) {
                processData.add(p);
            }
        }
    }

    public void setFilter(String filter) {
        this.filterText = filter;
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
        setupContextMenu();
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem killItem = new MenuItem("Kết thúc tiến trình");
        killItem.setOnAction(e -> killSelectedProcess());
        contextMenu.getItems().add(killItem);
        networkTable.setContextMenu(contextMenu);
    }

    private void killSelectedProcess() {
        NetworkProcessInfo selected = networkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Kết thúc tiến trình: " + selected.getProcessName());
        confirm.setContentText("Bạn có chắc muốn kết thúc tiến trình này? (PID: " + selected.getPid() + ")");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ProcessKillService.KillResult killResult = killService.killProcess(selected.getPid());
            showKillResult(killResult, selected.getProcessName());
        }
    }

    private void showKillResult(ProcessKillService.KillResult result, String processName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Kết quả");

        switch (result) {
            case SUCCESS -> {
                alert.setHeaderText("Thành công");
                alert.setContentText("Đã kết thúc tiến trình: " + processName);
            }
            case ACCESS_DENIED -> {
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.setHeaderText("Không có quyền");
                alert.setContentText("Không thể kết thúc tiến trình hệ thống. Cần quyền Administrator.");
            }
            case NOT_FOUND -> {
                alert.setAlertType(Alert.AlertType.WARNING);
                alert.setHeaderText("Không tìm thấy");
                alert.setContentText("Tiến trình không còn tồn tại.");
            }
            case FAILED -> {
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.setHeaderText("Thất bại");
                alert.setContentText("Không thể kết thúc tiến trình.");
            }
        }
        alert.showAndWait();
    }
}
