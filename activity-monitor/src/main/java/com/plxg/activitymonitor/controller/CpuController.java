package com.plxg.activitymonitor.controller;

import com.plxg.activitymonitor.model.CpuProcessInfo;
import com.plxg.activitymonitor.service.CpuService;
import com.plxg.activitymonitor.service.ProcessKillService;
import com.plxg.activitymonitor.service.ProcessService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import oshi.software.os.OSProcess;

import java.util.Objects;
import java.util.Optional;


public class CpuController {

    private String filterText = "";

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
    private StackedAreaChart<Number, Number> cpuLoadChart;


    @FXML
    private NumberAxis yAxis;

    @FXML
    private void initialize() {
        setupTableColumns();

        NumberAxis xAxis = (NumberAxis) cpuLoadChart.getXAxis();
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

        startCpuRealtimeUpdate(xAxis);
    }




    private final ObservableList<CpuProcessInfo> processData =
            FXCollections.observableArrayList();

    private final ProcessService processService = new ProcessService();
    private final ProcessKillService killService = new ProcessKillService();


    private CpuProcessInfo convert(OSProcess p) {
        String name = p.getName();

        double cpu = processService.getCpuLoad(p.getProcessID());
        if (cpu < 1.0) {
            cpu *=100;
        }
        cpu = Math.min(cpu, 100.0);

        String cpuTime = formatCpuTime(p.getKernelTime() + p.getUserTime());
        int threads = p.getThreadCount();

        int idleWakeUps = 0;
        String kind = "N/A";
        double gpuPercent = 0.0;
        String gpuTime = "N/A";

        int pid = p.getProcessID();
        String user = p.getUser();

        return new CpuProcessInfo(name, cpu, cpuTime, threads, idleWakeUps, kind, gpuPercent, gpuTime, pid, user);
    }

    //update process list:
    private void updateProcessTable() {
        var list = processService.getAllProcesses();

        processData.clear();
        for (OSProcess process : list) {
            CpuProcessInfo info = convert(process);
            if (filterText.isEmpty() || info.getProcessName().toLowerCase().contains(filterText)) {
                processData.add(info);
            }
        }
        updateSummaryTableInfo();

    }



    private  final CpuService cpuService = new CpuService();

    private  XYChart.Series<Number, Number> userSeries;
    private XYChart.Series<Number, Number> systemSeries;
    private int time = 0;


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
            String css = Objects.requireNonNull(getClass().getResource("/css/chart-style.css")).toExternalForm();
            cpuLoadChart.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.err.println("CSS file not found: /css/chart-style.css");
            e.printStackTrace();
        }
    }

    private void startCpuRealtimeUpdate(NumberAxis xAxis) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(2000), event -> {
                    updateCpuData(xAxis);
                    updateProcessTable();
                } )
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
        setupContextMenu();
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem killItem = new MenuItem("Kết thúc tiến trình");
        killItem.setOnAction(e -> killSelectedProcess());
        contextMenu.getItems().add(killItem);
        cpuTable.setContextMenu(contextMenu);
    }

    private void killSelectedProcess() {
        CpuProcessInfo selected = cpuTable.getSelectionModel().getSelectedItem();
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

    // milis sang hh:mm:ss
    private String formatCpuTime(long millis) {
        long sec = millis / 1000;
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        return String.format("%d:%02d:%02d", h, m, s);
    }

    public void setFilter(String filter) {
        this.filterText = filter;
    }
}
