package com.plxg.activitymonitor.service;

import com.plxg.activitymonitor.model.EnergyProcessInfo;
import oshi.SystemInfo;
import oshi.hardware.PowerSource;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class EnergyService {

    private final SystemInfo si;
    private final OperatingSystem os;
    private final Random random = new Random();
    
    // luu trang thai truoc do de tinh energy impact
    private final Map<Integer, ProcessSnapshot> previousSnapshots = new ConcurrentHashMap<>();
    private long lastUpdateTime = System.currentTimeMillis();
    
    // thoi diem bat dau chay tren pin
    private long batteryStartTime = -1;

    public EnergyService() {
        si = new SystemInfo();
        os = si.getOperatingSystem();
    }

    public BatteryStats getBatteryStats() {
        List<PowerSource> powerSources = si.getHardware().getPowerSources();

        if (powerSources.isEmpty()) {
            //k co battery
            return new BatteryStats(0, 0, 0, false, false);
        }

        PowerSource battery = powerSources.get(0);
        // nhan 100 vi getRemainingCapacityPercent tra ve 0.0-1.0
        double remainingCapacity = battery.getRemainingCapacityPercent() * 100.0;
        double timeRemaining = battery.getTimeRemainingEstimated();

        //doi sang phut
        int timeRemainingMinutes = (timeRemaining > 0) ? (int)(timeRemaining / 60) : -1;

        boolean isCharging = battery.isCharging();
        boolean isPluggedIn = battery.isPowerOnLine();
        
        // tinh time on battery
        int timeOnBattery = 0;
        if (!isPluggedIn && !isCharging) {
            if (batteryStartTime < 0) {
                batteryStartTime = System.currentTimeMillis();
            }
            timeOnBattery = (int)((System.currentTimeMillis() - batteryStartTime) / 60000);
        } else {
            // reset khi cam sac
            batteryStartTime = -1;
        }

        return new BatteryStats(
                remainingCapacity,
                timeRemainingMinutes,
                timeOnBattery,
                isCharging,
                isPluggedIn
        );
    }

    //lay ds process sort theo energy impact:
    public List<EnergyProcessInfo> getProcessByEnergyImpact() {
        List<OSProcess> processes = os.getProcesses();
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastUpdateTime;
        
        // tranh chia cho 0
        if (elapsed < 100) {
            elapsed = 100;
        }
        
        final long finalElapsed = elapsed;

        List<EnergyProcessInfo> result = processes.stream()
                .map(p -> toEnergyProcessInfo(p, finalElapsed))
                .sorted(Comparator.comparingDouble(EnergyProcessInfo::getEnergyImpact).reversed())
                .toList();
        
        // cap nhat snapshot
        lastUpdateTime = currentTime;
        for (OSProcess p : processes) {
            previousSnapshots.put(p.getProcessID(), 
                new ProcessSnapshot(p.getKernelTime(), p.getUserTime(), currentTime));
        }
        
        return result;
    }

    private EnergyProcessInfo toEnergyProcessInfo(OSProcess process, long elapsedMs) {
        int pid = process.getProcessID();
        long currentCpuTime = process.getKernelTime() + process.getUserTime();
        
        // tinh cpu % dua tren thay doi
        double cpuPercent = 0;
        ProcessSnapshot prev = previousSnapshots.get(pid);
        if (prev != null && elapsedMs > 0) {
            long prevCpuTime = prev.kernelTime + prev.userTime;
            long cpuDelta = currentCpuTime - prevCpuTime;
            cpuPercent = 100.0 * cpuDelta / elapsedMs;
            cpuPercent = Math.max(0, Math.min(100, cpuPercent));
        }
        
        // energy impact: tinh tu cpu%, lam tron 1 chu so thap phan
        double energyImpact = Math.round(cpuPercent) / 10.0;

        // 12hr power: OSHI khong ho tro, de "-"
        String power12Hr = "-";

        // app nap: Yes neu process dang sleep va cpu thap
        String appNap = "No";
        if (cpuPercent < 0.01 && process.getState() == OSProcess.State.SLEEPING) {
            appNap = "Yes";
        }

        // preventing sleep: OSHI khong ho tro
        String preventingSleep = "No";

        return new EnergyProcessInfo(
                process.getName(),
                Math.max(0, energyImpact),
                power12Hr,
                appNap,
                preventingSleep,
                process.getUser(),
                process.getProcessID()
        );
    }
    
    // luu trang thai process
    private record ProcessSnapshot(long kernelTime, long userTime, long timestamp) {}

    public record BatteryStats(
            double remainingCapacityPercent,
            int timeRemainingMinutes,
            int timeOnBatteryMinutes,
            boolean isCharging,
            boolean isPluggedIn
    ) {
        public String getTimeRemainingFormatted() {
            if (timeRemainingMinutes < 0 || isCharging) {
                return "Calculating...";
            }
            int hours = timeRemainingMinutes / 60;
            int minutes = timeRemainingMinutes % 60;
            return String.format("%d:%02d", hours, minutes);
        }

        public String getTimeOnBatteryFormatted() {
            int hours = timeOnBatteryMinutes / 60;
            int minutes = timeOnBatteryMinutes % 60;
            return String.format("%d:%02d", hours, minutes);
        }
    }


}
