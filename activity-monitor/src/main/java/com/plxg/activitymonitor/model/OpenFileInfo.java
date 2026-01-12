package com.plxg.activitymonitor.model;

public class OpenFileInfo {

    private final String fd;
    private final String type;
    private final String name;

    public OpenFileInfo(String fd, String type, String name) {
        this.fd = fd;
        this.type = type;
        this.name = name;
    }

    public String getFd() {
        return fd;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
