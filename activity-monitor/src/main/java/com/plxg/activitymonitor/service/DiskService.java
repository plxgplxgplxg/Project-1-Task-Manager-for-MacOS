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
    
    private long prevTotalReads = 0;
    private long prevTotalWrites = 0;
    private long prevReadBytes = 0;
    private long prevWriteBytes = 0;

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
        long totalReads = 0;
        long totalWrites = 0;
        long totalReadBytes = 0;
        long totalWriteBytes = 0;

        // lay so lieu tu disk stores - chi lay disk vat ly (disk0, disk1,...)
        var disks = si.getHardware().getDiskStores();
        for (var disk : disks) {
            // chi lay disk chinh (disk0), bo qua partition (disk0s1, disk3,...)
            String name = disk.getName();
            if (name.equals("disk0")) {
                disk.updateAttributes();
                totalReads = disk.getReads();
                totalWrites = disk.getWrites();
                totalReadBytes = disk.getReadBytes();
                totalWriteBytes = disk.getWriteBytes();
                break;
            }
        }
        
        // tinh per sec (chia 2 vi update moi 2 giay)
        long readsPerSec = (totalReads - prevTotalReads) / 2;
        long writesPerSec = (totalWrites - prevTotalWrites) / 2;
        long readBytesPerSec = (totalReadBytes - prevReadBytes) / 2;
        long writeBytesPerSec = (totalWriteBytes - prevWriteBytes) / 2;
        
        // luu gia tri hien tai
        prevTotalReads = totalReads;
        prevTotalWrites = totalWrites;
        prevReadBytes = totalReadBytes;
        prevWriteBytes = totalWriteBytes;

        return new DiskStats(
            totalReads, 
            totalWrites, 
            Math.max(0, readsPerSec),
            Math.max(0, writesPerSec),
            totalReadBytes,
            totalWriteBytes,
            Math.max(0, readBytesPerSec),
            Math.max(0, writeBytesPerSec)
        );
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

    public record DiskStats(
        long totalReads, 
        long totalWrites,
        long readsPerSec,
        long writesPerSec,
        long dataRead,
        long dataWritten,
        long dataReadPerSec,
        long dataWrittenPerSec
    ) {}

    private record DiskIOData(long bytesRead, long bytesWritten) {}
}
