package com.plxg.activitymonitor.model;

public class NetworkProcessInfo {

    private final String processName;
    private final long sentBytes;
    private final long rcvdBytes;
    private final long sentPackets;
    private final long rcvdPackets;
    private final int pid;
    private final String user;

    public NetworkProcessInfo(String processName, long sentBytes, long rcvdBytes, long sentPackets, long rcvdPackets, int pid, String user) {
        this.processName = processName;
        this.sentBytes = sentBytes;
        this.rcvdBytes = rcvdBytes;
        this.sentPackets = sentPackets;
        this.rcvdPackets = rcvdPackets;
        this.pid = pid;
        this.user = user;
    }

    public String getProcessName() {
        return processName;
    }

    public long getSentBytes() {
        return sentBytes;
    }

    public long getRcvdBytes() {
        return rcvdBytes;
    }

    public long getSentPackets() {
        return sentPackets;
    }

    public long getRcvdPackets() {
        return rcvdPackets;
    }

    public int getPid() {
        return pid;
    }

    public String getUser() {
        return user;
    }
}
