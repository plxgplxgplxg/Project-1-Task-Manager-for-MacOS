package com.plxg.activitymonitor.controller;

import com.plxg.activitymonitor.model.EnergyProcessInfo;
import com.plxg.activitymonitor.service.EnergyService;
import com.plxg.activitymonitor.service.ProcessKillService;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.util.Objects;
import java.util.Optional;

public class EnergyController {

    private String filterText = "";

    @FXML
    private TableView<EnergyProcessInfo> energyTable;

    @FXML
    private TableColumn<EnergyProcessInfo, String> colAppName;

    @FXML
    private TableColumn<EnergyProcessInfo, Double> colEnergyImpact;
    @FXML
    private TableColumn<EnergyProcessInfo, String> col12HrPower;

    @FXML
    private TableColumn<EnergyProcessInfo, String> colAppNap;

    @FXML
    private TableColumn<EnergyProcessInfo, String> colPreventSleep;

    @FXML
    private TableColumn<EnergyProcessInfo, String> colUser;

    @FXML
    private LineChart<Number, Number> energyImpactChart;

    @FXML
    private AreaChart<Number, Number> batteryChart;

    @FXML
    private Label lblRemainingCharge;

    @FXML
    private Label lblTimeRemaining;

    @FXML
    private Label lblTimeOnBattery;

    private final ObservableList<EnergyProcessInfo> processData = FXCollections.observableArrayList();

    private final EnergyService energyService = new EnergyService();
    private final ProcessKillService killService = new ProcessKillService();

    private XYChart.Series<Number, Number> energyImpactSeries;
    private XYChart.Series<Number, Number> batterySeries;
    private int time = 0;

    @FXML
    private void initialize() {
        setUpTableColumns();
        setUpCharts();
        loadChartStylesheet();
        startRealtimeUpdate();
    }

    private void setUpTableColumns() {
        colAppName.setCellValueFactory(new PropertyValueFactory<>("appName"));

        colEnergyImpact.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleDoubleProperty(
                    cellData.getValue().getEnergyImpact()
            ).asObject()
        );

        col12HrPower.setCellValueFactory(new PropertyValueFactory<>("power12Hr"));
        colAppNap.setCellValueFactory(new PropertyValueFactory<>("appNap"));
        colPreventSleep.setCellValueFactory(new PropertyValueFactory<>("preventingSleep"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("user"));

        energyTable.setItems(processData);
        setupContextMenu();
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem killItem = new MenuItem("Kết thúc tiến trình");
        killItem.setOnAction(e -> killSelectedProcess());
        contextMenu.getItems().add(killItem);
        energyTable.setContextMenu(contextMenu);
    }

    private void killSelectedProcess() {
        EnergyProcessInfo selected = energyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Kết thúc tiến trình: " + selected.getAppName());
        confirm.setContentText("Bạn có chắc muốn kết thúc tiến trình này? (PID: " + selected.getPid() + ")");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ProcessKillService.KillResult killResult = killService.killProcess(selected.getPid());
            showKillResult(killResult, selected.getAppName());
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

    private void setUpCharts() {
        //impact chart
        energyImpactChart.getData().clear();

        NumberAxis energyXAxis = (NumberAxis) energyImpactChart.getXAxis();
        energyXAxis.setAutoRanging(false);
        energyXAxis.setLowerBound(0);
        energyXAxis.setUpperBound(30);
        energyXAxis.setTickUnit(5);

        NumberAxis energyYAxis = (NumberAxis) energyImpactChart.getYAxis();
        energyYAxis.setAutoRanging(false);
        energyYAxis.setLowerBound(0);
        energyYAxis.setUpperBound(100);
        energyYAxis.setTickUnit(10);

        energyImpactSeries = new XYChart.Series<>();
        energyImpactSeries.setName("Energy Impact");
        energyImpactChart.getData().add(energyImpactSeries);

        //battery chart
        batteryChart.getData().clear();

        NumberAxis batteryXAxis = (NumberAxis) batteryChart.getXAxis();
        batteryXAxis.setAutoRanging(false);
        batteryXAxis.setLowerBound(0);
        batteryXAxis.setUpperBound(30);
        batteryXAxis.setTickUnit(5);

        NumberAxis batteryYAxis = (NumberAxis) batteryChart.getYAxis();
        batteryYAxis.setAutoRanging(false);
        batteryYAxis.setLowerBound(0);
        batteryYAxis.setUpperBound(100);
        batteryYAxis.setTickUnit(10);

        batterySeries = new XYChart.Series<>();
        batterySeries.setName("Battery");
        batteryChart.getData().add(batterySeries);
    }

    private void loadChartStylesheet() {
        try {
            String css = Objects.requireNonNull(
                    getClass().getResource("/css/chart-style.css")
            ).toExternalForm();
            energyImpactChart.getStylesheets().add(css);
            batteryChart.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.err.println("CSS file not found: /css/chart-style.css");
        }
    }

    private void startRealtimeUpdate() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(2000), event -> {
                    updateEnergyData();
                    updateProcessTable();
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        // Update lan dau
        updateEnergyData();
        updateProcessTable();
    }
    private void updateEnergyData(){
        EnergyService.BatteryStats stats = energyService.getBatteryStats();

        if (stats.remainingCapacityPercent() > 0) {
            lblRemainingCharge.setText(String.format("%.0f%%", stats.remainingCapacityPercent()));
            lblTimeRemaining.setText(stats.getTimeRemainingFormatted());
            lblTimeOnBattery.setText(stats.getTimeOnBatteryFormatted());
        } else {
            lblRemainingCharge.setText("N/A");
            lblTimeRemaining.setText("N/A");
            lblTimeOnBattery.setText("N/A");
        }

        double avgEnergyImpact = processData.stream()
                .limit(10)
                .mapToDouble(EnergyProcessInfo::getEnergyImpact)
                .average()
                .orElse(0);

        energyImpactSeries.getData().add(new XYChart.Data<>(time, avgEnergyImpact));

        if (energyImpactSeries.getData().size() > 30) {
            energyImpactSeries.getData().remove(0);
        }


        if (stats.remainingCapacityPercent() > 0) {
            batterySeries.getData().add(
                    new XYChart.Data<>(time, stats.remainingCapacityPercent())
            );

            if (batterySeries.getData().size() > 30) {
                batterySeries.getData().remove(0);
            }
        }

        time++;

        // di chuyen truc x khi vuot qua 30 diem
        if (time > 30) {
            NumberAxis energyXAxis = (NumberAxis) energyImpactChart.getXAxis();
            energyXAxis.setLowerBound(time - 30);
            energyXAxis.setUpperBound(time);

            NumberAxis batteryXAxis = (NumberAxis) batteryChart.getXAxis();
            batteryXAxis.setLowerBound(time - 30);
            batteryXAxis.setUpperBound(time);
        }
    }

    private void updateProcessTable() {
        var processes = energyService.getProcessByEnergyImpact();
        processData.clear();
        for (var p : processes) {
            if (filterText.isEmpty() || p.getAppName().toLowerCase().contains(filterText)) {
                processData.add(p);
            }
        }
    }

    public void setFilter(String filter) {
        this.filterText = filter;
    }
}
