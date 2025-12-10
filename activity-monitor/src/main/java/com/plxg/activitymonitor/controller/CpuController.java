package com.plxg.activitymonitor.controller;

import com.plxg.activitymonitor.model.CpuProcessInfo;
import com.plxg.activitymonitor.service.CpuService;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Label;
import javafx.util.Duration;
import oshi.software.os.OSProcess;

import java.util.Objects;


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


    private CpuProcessInfo convert(OSProcess p) {
        String name = p.getName();

        double cpu = processService.getCpuLoad(p.getProcessID());
        if (cpu < 1.0) {
            cpu *=100;
        }
        cpu = Math.min(cpu, 100.0);

        String cpuTime = formatCpuTime(p.getKernelTime() + p.getUserTime());
        int threads = p.getThreadCount();

        // oshi k lay dc vi k co api cong khai cua macos:
        int idleWakeUps = (int) (Math.random() * 200); //fake
        String kind = "Apple"; //fake
        double gpuPercent = 0.0; // fake
        String gpuTime = "0:00"; // fake

        int pid = p.getProcessID();
        String user = p.getUser();

        return new CpuProcessInfo(name, cpu, cpuTime, threads, idleWakeUps, kind, gpuPercent, gpuTime, pid, user);
    }

    //update process list:
    private void updateProcessTable() {
        var list = processService.getAllProcesses();

        processData.clear();
        for (OSProcess process : list) {
            processData.add(convert(process));
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
    }

    // milis sang hh:mm:ss
    private String formatCpuTime(long millis) {
        long sec = millis / 1000;
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        return String.format("%d:%02d:%02d", h, m, s);
    }



}
