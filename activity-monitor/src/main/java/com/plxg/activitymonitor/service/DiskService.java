package com.plxg.activitymonitor.service;

import com.plxg.activitymonitor.model.DiskProcessInfo;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiskService {

    private final SystemInfo si;
    private final OperatingSystem os;
    private final Map<Integer, DiskIOData> previousDiskIO = new HashMap<>();

    public DiskService() {
        si = new SystemInfo();
        os = si.getOperatingSystem();
        initializeDiskSnapshot();
    }

    private void initializeDiskSnapshot() {
        List<OSProcess> processes = os.getProcesses();
        for (OSProcess process : processes) {
            previousDiskIO.put(process.getProcessID(), 
                    new DiskIOData(process.getBytesRead(), process.getBytesWritten()));
        }
    }

    public DiskStats getDiskStats() {
        List<OSProcess> processes = os.getProcesses();
        long totalReads = 0;
        long totalWrites = 0;

        for (OSProcess process : processes) {
            totalReads += process.getBytesRead();
            totalWrites += process.getBytesWritten();
        }

        return new DiskStats(totalReads, totalWrites);
    }

    public List<DiskProcessInfo> getProcessesByDiskIO() {
        List<OSProcess> processes = os.getProcesses();

        List<DiskProcessInfo> result = processes.stream()
                .map(p -> new DiskProcessInfo(
                        p.getName(),
                        p.getBytesWritten(),
                        p.getBytesRead(),
                        p.getProcessID(),
                        p.getUser()
                ))
                .filter(p -> p.getBytesRead() > 0 || p.getBytesWritten() > 0)
                .sorted(Comparator.comparingLong((DiskProcessInfo p) -> 
                        p.getBytesRead() + p.getBytesWritten()).reversed())
                .toList();

        previousDiskIO.clear();
        for (OSProcess process : processes) {
            previousDiskIO.put(process.getProcessID(), 
                    new DiskIOData(process.getBytesRead(), process.getBytesWritten()));
        }

        return result;
    }

    public record DiskStats(long totalReads, long totalWrites) {}

    private record DiskIOData(long bytesRead, long bytesWritten) {}
}
