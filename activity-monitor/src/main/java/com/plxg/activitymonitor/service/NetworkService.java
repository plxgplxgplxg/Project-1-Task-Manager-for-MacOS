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
        var networks = si.getHardware().getNetworkIFs(true);
        
        long totalRcvd = 0;
        long totalSent = 0;
        long totalPacketsRcvd = 0;
        long totalPacketsSent = 0;

        for (var net : networks) {
            net.updateAttributes();
            totalRcvd += net.getBytesRecv();
            totalSent += net.getBytesSent();
            totalPacketsRcvd += net.getPacketsRecv();
            totalPacketsSent += net.getPacketsSent();
        }

        return new NetworkStats(totalRcvd, totalSent, totalPacketsRcvd, totalPacketsSent);
    }

    public List<NetworkProcessInfo> getProcessesByNetworkIO() {
        List<OSProcess> processes = os.getProcesses();

        List<NetworkProcessInfo> result = processes.stream()
                .map(p -> {
                    return new NetworkProcessInfo(
                            p.getName(),
                            0,
                            0,
                            0,
                            0,
                            p.getProcessID(),
                            p.getUser()
                    );
                })
                .toList();

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
