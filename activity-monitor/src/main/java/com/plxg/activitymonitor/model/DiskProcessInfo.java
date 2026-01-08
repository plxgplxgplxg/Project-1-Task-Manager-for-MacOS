package com.plxg.activitymonitor.model;

public class DiskProcessInfo {

    private final String processName;
    private final long bytesWritten;
    private final long bytesRead;
    private final int pid;
    private final String user;

    public DiskProcessInfo(String processName, long bytesWritten, long bytesRead, int pid, String user) {
        this.processName = processName;
        this.bytesWritten = bytesWritten;
        this.bytesRead = bytesRead;
        this.pid = pid;
        this.user = user;
    }

    public String getProcessName() {
        return processName;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public int getPid() {
        return pid;
    }

    public String getUser() {
        return user;
    }
}
