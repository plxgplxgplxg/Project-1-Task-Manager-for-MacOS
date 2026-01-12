package com.plxg.activitymonitor.model;

public class MemoryDetailInfo {

    private final long realMemorySize;
    private final long virtualMemorySize;
    private final long sharedMemorySize;
    private final long privateMemorySize;

    public MemoryDetailInfo(long realMemorySize, long virtualMemorySize, long sharedMemorySize, long privateMemorySize) {
        this.realMemorySize = realMemorySize;
        this.virtualMemorySize = virtualMemorySize;
        this.sharedMemorySize = sharedMemorySize;
        this.privateMemorySize = privateMemorySize;
    }

    public long getRealMemorySize() {
        return realMemorySize;
    }

    public long getVirtualMemorySize() {
        return virtualMemorySize;
    }

    public long getSharedMemorySize() {
        return sharedMemorySize;
    }

    public long getPrivateMemorySize() {
        return privateMemorySize;
    }
}
