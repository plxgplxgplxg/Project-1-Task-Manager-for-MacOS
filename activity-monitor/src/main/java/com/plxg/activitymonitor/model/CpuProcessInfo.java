package com.plxg.activitymonitor.model;

public class CpuProcessInfo {

    private final String processName;
    private final double cpuPercent;
    private final String cpuTime;
    private final int threads;
    private final int idleWakeUps;
    private final String kind;
    private final double gpuPercent;
    private final String gpuTime;
    private final int pid;
    private final String user;

    public CpuProcessInfo(String processName, double cpuPercent, String cpuTime, int threads, int idleWakeUps, String kind, double gpuPercent, String gpuTime, int pid, String user) {
        this.processName = processName;
        this.cpuPercent = cpuPercent;
        this.cpuTime = cpuTime;
        this.threads = threads;
        this.idleWakeUps = idleWakeUps;
        this.kind = kind;
        this.gpuPercent = gpuPercent;
        this.gpuTime = gpuTime;
        this.pid = pid;
        this.user = user;
    }

    public String getProcessName() {
        return processName;
    }

    public double getCpuPercent() {
        return cpuPercent;
    }

    public String getCpuTime() {
        return cpuTime;
    }

    public int getThreads() {
        return threads;
    }

    public int getIdleWakeUps() {
        return idleWakeUps;
    }

    public String getKind() {
        return kind;
    }

    public double getGpuPercent() {
        return gpuPercent;
    }

    public String getGpuTime() {
        return gpuTime;
    }

    public int getPid() {
        return pid;
    }

    public String getUser() {
        return user;
    }
}
