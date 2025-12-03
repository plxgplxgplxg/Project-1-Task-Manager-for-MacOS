package com.plxg.activitymonitor.model;

public class ProcessInfo {
    
    private String processName;
    private double cpuPercent;
    private long cpuTime;
    private int threads;
    private int idleWakeUps;
    private int pid;
    private String user;

    public ProcessInfo(String processName, double cpuPercent, long cpuTime, int threads, int idleWakeUps, int pid,
            String user) {
        this.processName = processName;
        this.cpuPercent = cpuPercent;
        this.cpuTime = cpuTime;
        this.threads = threads;
        this.idleWakeUps = idleWakeUps;
        this.pid = pid;
        this.user = user;
    }

    public String getProcessName() {
        return processName;
    }

    public double getCpuPercent() {
        return cpuPercent;
    }

    public long getCpuTime() {
        return cpuTime;
    }

    public int getThreads() {
        return threads;
    }

    public int getIdleWakeUps() {
        return idleWakeUps;
    }

    public int getPid() {
        return pid;
    }

    public String getUser() {
        return user;
    }

    
    
    
}
