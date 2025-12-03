package com.plxg.activitymonitor.controller;

import com.plxg.activitymonitor.model.ProcessInfo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class CpuController {

    @FXML private TableView<ProcessInfo> cpuTable;

    @FXML private TableColumn<ProcessInfo, String> colProcessName;
    @FXML private TableColumn<ProcessInfo, Double> colCPU;
    @FXML private TableColumn<ProcessInfo, Long> colCPUTime;
    @FXML private TableColumn<ProcessInfo, Integer> colThreads;
    @FXML private TableColumn<ProcessInfo, Integer> colIdleWakeUps;
    @FXML private TableColumn<ProcessInfo, Integer> colPID;
    @FXML private TableColumn<ProcessInfo, String> colUser;

    @FXML
    public void initialize() {

        colProcessName.setCellValueFactory(new PropertyValueFactory<>("processName"));
        colCPU.setCellValueFactory(new PropertyValueFactory<>("cpuPercent"));
        colCPUTime.setCellValueFactory(new PropertyValueFactory<>("cpuTime"));
        colThreads.setCellValueFactory(new PropertyValueFactory<>("threads"));
        colIdleWakeUps.setCellValueFactory(new PropertyValueFactory<>("idleWakeUps"));
        colPID.setCellValueFactory(new PropertyValueFactory<>("pid"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("user"));

        ObservableList<ProcessInfo> data = FXCollections.observableArrayList(
            new ProcessInfo("Process1", 12.5, 123456, 10, 200, 101, "user1"),
            new ProcessInfo("Process2", 25.0, 234567, 15, 300, 102, "user2"),
            new ProcessInfo("Process3", 7.5, 345678, 8, 150, 103, "user3") 
        );

        cpuTable.setItems(data);
    }
}
