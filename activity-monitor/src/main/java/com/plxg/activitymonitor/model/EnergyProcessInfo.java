package com.plxg.activitymonitor.model;

public class EnergyProcessInfo {
    private final String appName;
    private final double energyImpact;
    private final String power12Hr;
    private final String appNap;
    private final String preventingSleep;
    private final String user;
    private final int pid;

    public EnergyProcessInfo(String appName, double energyImpact, String power12Hr, String appNap, String preventingSleep, String user, int pid) {
        this.appName = appName;
        this.energyImpact = energyImpact;
        this.power12Hr = power12Hr;
        this.appNap = appNap;
        this.preventingSleep = preventingSleep;
        this.user = user;
        this.pid = pid;
    }

    public String getAppName() {
        return appName;
    }

    public double getEnergyImpact() {
        return energyImpact;
    }

    public String getPower12Hr() {
        return power12Hr;
    }

    public String getAppNap() {
        return appNap;
    }

    public String getPreventingSleep() {
        return preventingSleep;
    }

    public String getUser() {
        return user;
    }

    public int getPid() {
        return pid;
    }
}
