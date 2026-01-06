package com.plxg.activitymonitor.model;

public class MemoryProcessInfo {

    private final String processName;
    private final long memoryBytes;
    private final int threads;
    private final int ports;
    private final int pid;
    private final String user;

    public MemoryProcessInfo(String processName, long memoryBytes, int threads, int ports, int pid, String user){
        this.processName = processName;
        this.memoryBytes = memoryBytes;
        this.threads = threads;
        this.ports = ports;
        this.pid = pid;
        this.user = user;
    }

    public String getProcessName() {
        return processName;
    }

    public long getMemoryBytes() {
        return memoryBytes;
    }

    public int getThreads() {
        return threads;
    }

    public int getPorts() {
        return ports;
    }

    public int getPid() {
        return pid;
    }

    public String getUser() {
        return user;
    }
}
