package com.plxg.activitymonitor.service;

import com.plxg.activitymonitor.model.MemoryProcessInfo;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.Comparator;
import java.util.List;

public class MemoryService {

    private final SystemInfo si;
    private final OperatingSystem os;
    private final GlobalMemory memory;

    public MemoryService() {
        si = new SystemInfo();
        os = si.getOperatingSystem();
        memory = si.getHardware().getMemory();
    }

    //lay ttin tong quan memory he thong
    public MemoryStats getMemoryStats() {
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        long usedMemory = totalMemory - availableMemory;

        //macos khong cung cap api cho cached memory qua oshi
        long cachedMemory = 0;

        long swapUsed = 0;
        if (memory.getVirtualMemory() != null) {
            swapUsed = memory.getVirtualMemory().getSwapUsed();
        }

        return new MemoryStats(totalMemory, usedMemory, availableMemory, cachedMemory, swapUsed);
    }

    //lay ds process sort theo memory usage giam dan
    public List<MemoryProcessInfo> getProcessesByMemory() {
        List<OSProcess> processes = os.getProcesses();

        return processes.stream()
                .sorted(Comparator.comparingLong(OSProcess::getResidentSetSize).reversed())
                .map(this::toMemoryProcessInfo)
                .toList();
    }


    private MemoryProcessInfo toMemoryProcessInfo(OSProcess process){
        return new MemoryProcessInfo(
                process.getName(),
                process.getResidentSetSize(),
                process.getThreadCount(),
                0,
                process.getProcessID(),
                process.getUser()
        );
    }

    //record chua ttin memory he thong
    public record MemoryStats (
        long totalMemory,
        long usedMemory,
        long availableMemory,
        long cachedMemory,
        long swapUsed
    ) {}
}