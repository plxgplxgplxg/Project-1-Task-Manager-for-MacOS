package com.example;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class CPUView {
    
    private Label cpuLabel;
    private VBox root;

    public CPUView() {
        cpuLabel = new Label("Loading CPU...");
        root = new VBox(10, cpuLabel);

        startMonitoring();
    }

    public VBox getView() {
        return root;
    }

    private void startMonitoring() {

        SystemInfo si = new SystemInfo();
        CentralProcessor cpu = si.getHardware().getProcessor();

        Thread thread = new Thread(() -> {
            long[] prevTicks = cpu.getSystemCpuLoadTicks();

            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long[] ticks = cpu.getSystemCpuLoadTicks();
                double load = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
                prevTicks = ticks;

                String text = String.format("CPU Load: %.2f%%", load);

                //cap nhat ui
                Platform.runLater(() -> cpuLabel.setText(text));
            }
        });

        thread.setDaemon(true);
        thread.start();
    }
}
