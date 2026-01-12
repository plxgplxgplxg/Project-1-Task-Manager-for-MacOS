package com.plxg.activitymonitor.controller;

import com.plxg.activitymonitor.model.MemoryDetailInfo;
import com.plxg.activitymonitor.model.OpenFileInfo;
import com.plxg.activitymonitor.model.ProcessDetailInfo;
import com.plxg.activitymonitor.model.StatisticsInfo;
import com.plxg.activitymonitor.service.ProcessInspectService;
import com.plxg.activitymonitor.service.ProcessKillService;
import com.plxg.activitymonitor.util.FormatUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;


public class InspectProcessController {

    @FXML
    private Label lblParentProcess;

    @FXML
    private Label lblUser;

    @FXML
    private Label lblProcessGroup;

    @FXML
    private Label lblCpuPercent;

    @FXML
    private Label lblRecentHangs;

    @FXML
    private Label lblRealMemory;

    @FXML
    private Label lblVirtualMemory;

    @FXML
    private Label lblSharedMemory;

    @FXML
    private Label lblPrivateMemory;

    @FXML
    private Label lblCpuTime;

    @FXML
    private Label lblContextSwitches;

    @FXML
    private Label lblThreads;

    @FXML
    private Label lblPorts;

    @FXML
    private TableView<OpenFileInfo> openFilesTable;

    @FXML
    private TableColumn<OpenFileInfo, String> colFd;

    @FXML
    private TableColumn<OpenFileInfo, String> colType;

    @FXML
    private TableColumn<OpenFileInfo, String> colName;

    @FXML
    private Button btnQuit;

    private int pid;
    private double cpuPercent;
    private String processName;
    private Timeline updateTimeline;
    private final ProcessInspectService inspectService = new ProcessInspectService();
    private final ProcessKillService killService = new ProcessKillService();
    private final ObservableList<OpenFileInfo> openFilesData = FXCollections.observableArrayList();

    public void initData(int pid, String processName, double cpuPercent) {
        this.pid = pid;
        this.processName = processName;
        this.cpuPercent = cpuPercent;

        setupTable();
        updateData();
        startRealtimeUpdate();
    }

    private void setupTable() {
        colFd.setCellValueFactory(new PropertyValueFactory<>("fd"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        openFilesTable.setItems(openFilesData);
    }

    private void startRealtimeUpdate() {
        updateTimeline = new Timeline(
                new KeyFrame(Duration.millis(2000), event -> updateData())
        );
        updateTimeline.setCycleCount(Animation.INDEFINITE);
        updateTimeline.play();
    }

    private void updateData() {
        ProcessDetailInfo details = inspectService.getProcessDetails(pid, cpuPercent);
        if (details == null) {
            showProcessNotFound();
            return;
        }

        lblParentProcess.setText(details.getParentName() + " (" + details.getParentPid() + ")");
        lblUser.setText(details.getUser() + " (" + details.getUid() + ")");
        lblProcessGroup.setText(details.getProcessGroupName() + " (" + details.getProcessGroupId() + ")");
        lblCpuPercent.setText(String.format("%.2f", details.getCpuPercent()));
        lblRecentHangs.setText(String.valueOf(details.getRecentHangs()));

        MemoryDetailInfo memory = inspectService.getMemoryDetails(pid);
        lblRealMemory.setText(FormatUtils.formatBytes(memory.getRealMemorySize()));
        lblVirtualMemory.setText(FormatUtils.formatBytes(memory.getVirtualMemorySize()));
        lblSharedMemory.setText(FormatUtils.formatBytes(memory.getSharedMemorySize()));
        lblPrivateMemory.setText(FormatUtils.formatBytes(memory.getPrivateMemorySize()));

        StatisticsInfo stats = inspectService.getStatistics(pid);
        lblCpuTime.setText(stats.getCpuTime());
        lblContextSwitches.setText(FormatUtils.formatNumber(stats.getContextSwitches()));
        lblThreads.setText(String.valueOf(stats.getThreads()));
        lblPorts.setText(String.valueOf(stats.getPorts()));

        List<OpenFileInfo> files = inspectService.getOpenFiles(pid);
        openFilesData.setAll(files);
    }

    private void showProcessNotFound() {
        if (updateTimeline != null) {
            updateTimeline.stop();
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText("Tiến trình không tồn tại");
        alert.setContentText("Tiến trình " + processName + " (PID: " + pid + ") không còn tồn tại.");
        alert.showAndWait();

        closeWindow();
    }

    @FXML
    private void onQuitClicked() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Kết thúc tiến trình: " + processName);
        confirm.setContentText("Bạn có chắc muốn kết thúc tiến trình này? (PID: " + pid + ")");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ProcessKillService.KillResult killResult = killService.killProcess(pid);
            showKillResult(killResult);
        }
    }

    private void showKillResult(ProcessKillService.KillResult result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Kết quả");

        switch (result) {
            case SUCCESS -> {
                alert.setHeaderText("Thành công");
                alert.setContentText("Đã kết thúc tiến trình: " + processName);
                alert.showAndWait();
                closeWindow();
            }
            case ACCESS_DENIED -> {
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.setHeaderText("Không có quyền");
                alert.setContentText("Không thể kết thúc tiến trình hệ thống. Cần quyền Administrator.");
                alert.showAndWait();
            }
            case NOT_FOUND -> {
                alert.setAlertType(Alert.AlertType.WARNING);
                alert.setHeaderText("Không tìm thấy");
                alert.setContentText("Tiến trình không còn tồn tại.");
                alert.showAndWait();
                closeWindow();
            }
            case FAILED -> {
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.setHeaderText("Thất bại");
                alert.setContentText("Không thể kết thúc tiến trình.");
                alert.showAndWait();
            }
        }
    }

    private void closeWindow() {
        if (updateTimeline != null) {
            updateTimeline.stop();
        }
        Stage stage = (Stage) btnQuit.getScene().getWindow();
        stage.close();
    }
}
