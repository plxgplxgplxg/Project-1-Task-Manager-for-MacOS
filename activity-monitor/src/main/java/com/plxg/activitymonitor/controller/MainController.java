package com.plxg.activitymonitor.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class MainController {

    @FXML
    private TextField searchField;

    @FXML
    public void initialize() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Search: "+ newVal);
        });

        System.out.println("MainController initialized");
    }
}
