package com.plxg.activitymonitor.service;

import com.plxg.activitymonitor.model.NetworkProcessInfo;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkService {

    private final SystemInfo si;
    private final OperatingSystem os;
    private final Map<Integer, NetworkIOData> previousNetworkIO = new HashMap<>();

    public NetworkService() {
        si = new SystemInfo();
        os = si.getOperatingSystem();
        initializeNetworkSnapshot();
    }

    private void initializeNetworkSnapshot() {
        List<OSProcess> processes = os.getProcesses();
        for (OSProcess process : processes) {
            previousNetworkIO.put(process.getProcessID(), 
                    new NetworkIOData(
                            process.getBytesRead(), 
                            process.getBytesWritten(), 
                            0, 
                            0
                    ));
        }
    }

    public NetworkStats getNetworkStats() {
        List<OSProcess> processes = os.getProcesses();
        long totalRcvd = 0;
        long totalSent = 0;
        long totalPacketsRcvd = 0;
        long totalPacketsSent = 0;

        for (OSProcess process : processes) {
            totalRcvd += process.getBytesRead();
            totalSent += process.getBytesWritten();
            totalPacketsRcvd += Math.random() * 1000;
            totalPacketsSent += Math.random() * 1000;
        }

        return new NetworkStats(totalRcvd, totalSent, totalPacketsRcvd, totalPacketsSent);
    }

    public List<NetworkProcessInfo> getProcessesByNetworkIO() {
        List<OSProcess> processes = os.getProcesses();

        List<NetworkProcessInfo> result = processes.stream()
                .map(p -> new NetworkProcessInfo(
                        p.getName(),
                        p.getBytesWritten(),
                        p.getBytesRead(),
                        (long)(Math.random() * 500),
                        (long)(Math.random() * 500),
                        p.getProcessID(),
                        p.getUser()
                ))
                .sorted(Comparator.comparingLong((NetworkProcessInfo p) -> 
                        p.getSentBytes() + p.getRcvdBytes()).reversed())
                .toList();

        previousNetworkIO.clear();
        for (OSProcess process : processes) {
            previousNetworkIO.put(process.getProcessID(), 
                    new NetworkIOData(
                            process.getBytesRead(), 
                            process.getBytesWritten(), 
                            0, 
                            0
                    ));
        }

        return result;
    }

    public record NetworkStats(
            long bytesRcvd,
            long bytesSent,
            long packetsRcvd,
            long packetsSent
    ) {}

    private record NetworkIOData(
            long bytesRcvd, 
            long bytesSent, 
            long packetsRcvd, 
            long packetsSent
    ) {}
}
