package com.plxg.activitymonitor.service;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProcessService {

    private final SystemInfo si;
    private final OperatingSystem os;

    public ProcessService() {
        si = new SystemInfo();
        os = si.getOperatingSystem();
        initializeProcessSnapshots();
    }

    private final Map<Integer, OSProcess> previousProcesses = new HashMap<>();
    private final Map<Integer, Double> cpuLoadMap = new HashMap<>();

    private void initializeProcessSnapshots() {
        List<OSProcess> processes = os.getProcesses();
        for (OSProcess process: processes) {
            previousProcesses.put(process.getProcessID(), process);
        }
    }

    public List<OSProcess> getAllProcesses() {
        List<OSProcess> currentProcesses = os.getProcesses();

        cpuLoadMap.clear();

        for (OSProcess currentProcess: currentProcesses) {
            int pid = currentProcess.getProcessID();
            OSProcess previousProcess = previousProcesses.get(pid);

            double cpuLoad = 0.0;
            if (previousProcess != null) {
                cpuLoad = currentProcess.getProcessCpuLoadBetweenTicks(previousProcess);
            }
            cpuLoadMap.put(pid, cpuLoad);
        }

        // sang lan tiep theo:
        previousProcesses.clear();
        for (OSProcess process: currentProcesses) {
            previousProcesses.put(process.getProcessID(), process);
        }

        return currentProcesses.stream()
                .sorted(Comparator.comparingDouble((OSProcess p) -> {
                    double cpu = cpuLoadMap.getOrDefault(p.getProcessID(), 0.0);
                    if (cpu < 1.0) {
                        cpu *= 100;
                    }
                    return cpu;
                }).reversed())
                .toList();
    }

    public double getCpuLoad(int pid) {
        return cpuLoadMap.getOrDefault(pid, 0.0);
    }
}