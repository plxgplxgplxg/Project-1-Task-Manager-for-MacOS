package com.plxg.activitymonitor.controller;

import com.plxg.activitymonitor.model.DiskProcessInfo;
import com.plxg.activitymonitor.service.DiskService;
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

public class DiskController {

    private String filterText = "";

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
    private Label lblWritesOutSec;

    @FXML
    private Label lblDataRead;

    @FXML
    private Label lblDataWritten;

    @FXML
    private Label lblDataReadSec;

    @FXML
    private Label lblDataWrittenSec;

    @FXML
    private AreaChart<Number, Number> diskChart;

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

        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(true);

        setupChartSeries();
        loadChartStylesheet();
        startDiskRealtimeUpdate(xAxis);
    }

    private final ObservableList<DiskProcessInfo> processData =
            FXCollections.observableArrayList();

    private final DiskService diskService = new DiskService();
    private final ProcessKillService killService = new ProcessKillService();

    private XYChart.Series<Number, Number> readSeries;
    private XYChart.Series<Number, Number> writeSeries;
    private int time = 0;

    private void setupChartSeries() {
        diskChart.getData().clear();
        diskChart.getStyleClass().add("disk-io-chart");

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

        lblReadsIn.setText(FormatUtils.formatNumber(stats.totalReads()));
        lblWritesOut.setText(FormatUtils.formatNumber(stats.totalWrites()));
        lblReadsInSec.setText(String.valueOf(stats.readsPerSec()));
        lblWritesOutSec.setText(String.valueOf(stats.writesPerSec()));
        lblDataRead.setText(FormatUtils.formatBytes(stats.dataRead()));
        lblDataWritten.setText(FormatUtils.formatBytes(stats.dataWritten()));
        lblDataReadSec.setText(FormatUtils.formatBytes(stats.dataReadPerSec()));
        lblDataWrittenSec.setText(FormatUtils.formatBytes(stats.dataWrittenPerSec()));

        // ve chart theo reads/writes per sec
        // reads ve phia tren (duong), writes ve phia duoi (am)
        readSeries.getData().add(new XYChart.Data<>(time, stats.readsPerSec()));
        writeSeries.getData().add(new XYChart.Data<>(time, -stats.writesPerSec()));
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
        setupContextMenu();
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem killItem = new MenuItem("Kết thúc tiến trình");
        killItem.setOnAction(e -> killSelectedProcess());
        contextMenu.getItems().add(killItem);
        diskTable.setContextMenu(contextMenu);
    }

    private void killSelectedProcess() {
        DiskProcessInfo selected = diskTable.getSelectionModel().getSelectedItem();
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
