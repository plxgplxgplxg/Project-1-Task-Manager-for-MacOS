package com.plxg.activitymonitor.service;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class CpuService {

    private final CentralProcessor processor;
    private long[] prevTicks;

    public CpuService() {
        SystemInfo si = new SystemInfo();
        processor = si.getHardware().getProcessor();

        prevTicks = processor.getSystemCpuLoadTicks();
    }

    public CpuStats getCpuStats() {
        long[] ticks = processor.getSystemCpuLoadTicks();

        long user = ticks[CentralProcessor.TickType.USER.ordinal()]
                - prevTicks[CentralProcessor.TickType.USER.ordinal()];

        long nice = ticks[CentralProcessor.TickType.NICE.ordinal()]
                - prevTicks[CentralProcessor.TickType.NICE.ordinal()];

        long system = ticks[CentralProcessor.TickType.SYSTEM.ordinal()]
                - prevTicks[CentralProcessor.TickType.SYSTEM.ordinal()];

        long idle = ticks[CentralProcessor.TickType.IDLE.ordinal()]
                - prevTicks[CentralProcessor.TickType.IDLE.ordinal()];

        long total = user + nice + system + idle;


        //% CPU
        double userPct = 100d * (user + nice) / total;
        double systemPct = 100d * system / total;
        double idlePct = 100d * idle / total;

        prevTicks = ticks;

        return new CpuStats(userPct, systemPct, idlePct);
    }

    public  record CpuStats(double user, double system, double idle){};

}
