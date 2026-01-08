package com.plxg.activitymonitor.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class MainController {

    @FXML
    private TextField txtSearch;

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
}
