package com.plxg.activitymonitor.model;

public class ProcessDetailInfo {

    private final int pid;
    private final String processName;
    private final int parentPid;
    private final String parentName;
    private final String user;
    private final int uid;
    private final int processGroupId;
    private final String processGroupName;
    private final double cpuPercent;
    private final int recentHangs;

    public ProcessDetailInfo(int pid, String processName, int parentPid, String parentName, String user, int uid, int processGroupId, String processGroupName, double cpuPercent, int recentHangs) {
        this.pid = pid;
        this.processName = processName;
        this.parentPid = parentPid;
        this.parentName = parentName;
        this.user = user;
        this.uid = uid;
        this.processGroupId = processGroupId;
        this.processGroupName = processGroupName;
        this.cpuPercent = cpuPercent;
        this.recentHangs = recentHangs;
    }

    public int getPid() {
        return pid;
    }

    public String getProcessName() {
        return processName;
    }

    public int getParentPid() {
        return parentPid;
    }

    public String getParentName() {
        return parentName;
    }

    public String getUser() {
        return user;
    }

    public int getUid() {
        return uid;
    }

    public int getProcessGroupId() {
        return processGroupId;
    }

    public String getProcessGroupName() {
        return processGroupName;
    }

    public double getCpuPercent() {
        return cpuPercent;
    }

    public int getRecentHangs() {
        return recentHangs;
    }
}
