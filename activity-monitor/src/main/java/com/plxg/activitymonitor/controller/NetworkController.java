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
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        networkTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void inspectSelectedProcess() {
        NetworkProcessInfo selected = networkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/InspectProcess.fxml"));
            Scene scene = new Scene(loader.load());
            InspectProcessController controller = loader.getController();
            controller.initData(selected.getPid(), selected.getProcessName(), 0);
            Stage stage = new Stage();
            stage.setTitle(selected.getProcessName() + " (" + selected.getPid() + ")");
            stage.setScene(scene);
            stage.initModality(Modality.NONE);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void killSelectedProcess() {
        List<NetworkProcessInfo> selectedItems = new ArrayList<>(networkTable.getSelectionModel().getSelectedItems());
        if (selectedItems.isEmpty()) {
            return;
        }

        String message = selectedItems.size() == 1 
            ? "Kết thúc tiến trình: " + selectedItems.get(0).getProcessName()
            : "Kết thúc " + selectedItems.size() + " tiến trình đã chọn";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(message);
        confirm.setContentText("Bạn có chắc muốn kết thúc?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int success = 0, failed = 0;
            for (NetworkProcessInfo item : selectedItems) {
                ProcessKillService.KillResult killResult = killService.killProcess(item.getPid());
                if (killResult == ProcessKillService.KillResult.SUCCESS) {
                    success++;
                } else {
                    failed++;
                }
            }
            showMultiKillResult(success, failed);
        }
    }

    private void showMultiKillResult(int success, int failed) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Kết quả");
        alert.setHeaderText("Hoàn thành");
        alert.setContentText("Thành công: " + success + ", Thất bại: " + failed);
        if (failed > 0) {
            alert.setAlertType(Alert.AlertType.WARNING);
        }
        alert.showAndWait();
    }
}
