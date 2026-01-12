package com.plxg.activitymonitor.controller;

import com.plxg.activitymonitor.model.MemoryProcessInfo;
import com.plxg.activitymonitor.service.MemoryService;
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


public class MemoryController {

    private String filterText = "";

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
    private final ProcessKillService killService = new ProcessKillService();
    private XYChart.Series<Number, Number> memorySeries;
    private int time = 0;

    @FXML
    private void initialize() {
        setUpTableColumns();
        setUpChart();
        loadChartStylesheet();
        startRealtimeUpdate();
    }

    //setup column 
    private void setUpTableColumns() {
        colProcessName.setCellValueFactory(new PropertyValueFactory<>("processName"));

        //format memory 
        colMemory.setCellValueFactory(cellData -> {
            long bytes = cellData.getValue().getMemoryBytes();
            return new SimpleStringProperty(FormatUtils.formatBytes(bytes));
        });

        colThreads.setCellValueFactory(new PropertyValueFactory<>("threads"));
        colPorts.setCellValueFactory(new PropertyValueFactory<>("ports"));
        colPid.setCellValueFactory(new PropertyValueFactory<>("pid"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("user"));

        memoryTable.setItems(processData);
        memoryTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void inspectSelectedProcess() {
        MemoryProcessInfo selected = memoryTable.getSelectionModel().getSelectedItem();
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
        List<MemoryProcessInfo> selectedItems = new ArrayList<>(memoryTable.getSelectionModel().getSelectedItems());
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
            for (MemoryProcessInfo item : selectedItems) {
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

    private void setUpChart() {
        memoryPressureChart.getData().clear();
        
        memoryPressureChart.getStyleClass().add("memory-pressure-chart");

        NumberAxis xAxis = (NumberAxis) memoryPressureChart.getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(30);
        xAxis.setTickUnit(5);

        NumberAxis yAxis = (NumberAxis) memoryPressureChart.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(50);
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
        lblCachedFiles.setText("N/A");
        lblSwapUsed.setText(FormatUtils.formatBytes(stats.swapUsed()));

        //MACOS ko cung cap api cho app/wired/compressed memory
        lblAppMemory.setText("N/A");
        lblWiredMemory.setText("N/A");
        lblCompressed.setText("N/A");

        //update chart 
        double pressure = memoryService.getMemoryPressure();
        memorySeries.getData().add(new XYChart.Data<>(time, pressure));
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
        for (var p : processes) {
            if (filterText.isEmpty() || p.getProcessName().toLowerCase().contains(filterText)) {
                processData.add(p);
            }
        }
    }

    public void setFilter(String filter) {
        this.filterText = filter;
    }
}
