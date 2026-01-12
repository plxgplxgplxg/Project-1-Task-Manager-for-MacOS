package com.plxg.activitymonitor.model;

public class StatisticsInfo {

    private final String cpuTime;
    private final long contextSwitches;
    private final int threads;
    private final int ports;
    private final int machPorts;

    public StatisticsInfo(String cpuTime, long contextSwitches, int threads, int ports, int machPorts) {
        this.cpuTime = cpuTime;
        this.contextSwitches = contextSwitches;
        this.threads = threads;
        this.ports = ports;
        this.machPorts = machPorts;
    }

    public String getCpuTime() {
        return cpuTime;
    }

    public long getContextSwitches() {
        return contextSwitches;
    }

    public int getThreads() {
        return threads;
    }

    public int getPorts() {
        return ports;
    }

    public int getMachPorts() {
        return machPorts;
    }
}
