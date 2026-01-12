package com.plxg.activitymonitor.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

public class MainController {

    @FXML
    private TextField txtSearch;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private CpuController cpuContentController;

    @FXML
    private MemoryController memoryContentController;

    @FXML
    private EnergyController energyContentController;

    @FXML
    private DiskController diskContentController;

    @FXML
    private NetworkController networkContentController;

    @FXML
    private void initialize() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal.toLowerCase().trim();
            cpuContentController.setFilter(filter);
            memoryContentController.setFilter(filter);
            energyContentController.setFilter(filter);
            diskContentController.setFilter(filter);
            networkContentController.setFilter(filter);
        });
    }

    // Xử lý click icon kill process
    @FXML
    private void onKillProcess() {
        int tabIndex = mainTabPane.getSelectionModel().getSelectedIndex();
        switch (tabIndex) {
            case 0 -> cpuContentController.killSelectedProcess();
            case 1 -> memoryContentController.killSelectedProcess();
            case 2 -> energyContentController.killSelectedProcess();
            case 3 -> diskContentController.killSelectedProcess();
            case 4 -> networkContentController.killSelectedProcess();
        }
    }

    // Xử lý click icon inspect process
    @FXML
    private void onInspectProcess() {
        int tabIndex = mainTabPane.getSelectionModel().getSelectedIndex();
        switch (tabIndex) {
            case 0 -> cpuContentController.inspectSelectedProcess();
            case 1 -> memoryContentController.inspectSelectedProcess();
            case 2 -> energyContentController.inspectSelectedProcess();
            case 3 -> diskContentController.inspectSelectedProcess();
            case 4 -> networkContentController.inspectSelectedProcess();
        }
    }
}
