package com.plxg.activitymonitor.service;

import com.plxg.activitymonitor.model.MemoryDetailInfo;
import com.plxg.activitymonitor.model.OpenFileInfo;
import com.plxg.activitymonitor.model.ProcessDetailInfo;
import com.plxg.activitymonitor.model.StatisticsInfo;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class ProcessInspectService {

    private final SystemInfo si;
    private final OperatingSystem os;

    public ProcessInspectService() {
        si = new SystemInfo();
        os = si.getOperatingSystem();
    }

    public ProcessDetailInfo getProcessDetails(int pid, double cpuPercent) {
        OSProcess process = os.getProcess(pid);
        if (process == null) {
            return null;
        }

        String processName = process.getName();
        int parentPid = process.getParentProcessID();
        String parentName = getProcessName(parentPid);
        String user = process.getUser();
        int uid = parseUid(user);
        int processGroupId = getProcessGroupId(pid);
        String processGroupName = processName;

        return new ProcessDetailInfo(
                pid,
                processName,
                parentPid,
                parentName,
                extractUsername(user),
                uid,
                processGroupId,
                processGroupName,
                cpuPercent,
                0
        );
    }

    private String getProcessName(int pid) {
        OSProcess process = os.getProcess(pid);
        if (process != null) {
            return process.getName();
        }
        return "Unknown";
    }

    private String extractUsername(String user) {
        if (user == null) {
            return "Unknown";
        }
        if (user.contains("\\")) {
            return user.substring(user.lastIndexOf("\\") + 1);
        }
        return user;
    }

    private int parseUid(String user) {
        try {
            String[] output = executeCommand("id -u " + extractUsername(user));
            if (output.length > 0 && !output[0].isEmpty()) {
                return Integer.parseInt(output[0].trim());
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }

    private int getProcessGroupId(int pid) {
        try {
            String[] output = executeCommand("ps -o pgid= -p " + pid);
            if (output.length > 0 && !output[0].isEmpty()) {
                return Integer.parseInt(output[0].trim());
            }
        } catch (Exception e) {
            // ignore
        }
        return pid;
    }


    public MemoryDetailInfo getMemoryDetails(int pid) {
        long rss = 0;
        long vsz = 0;
        long privateMemory = 0;
        long sharedMemory = 0;

        try {
            String[] psOutput = executeCommand("ps -o rss=,vsz= -p " + pid);
            if (psOutput.length > 0 && !psOutput[0].isEmpty()) {
                String[] parts = psOutput[0].trim().split("\\s+");
                if (parts.length >= 2) {
                    rss = Long.parseLong(parts[0]) * 1024;
                    vsz = Long.parseLong(parts[1]) * 1024;
                }
            }
        } catch (Exception e) {
            // ignore
        }

        try {
            String[] vmmapOutput = executeCommand("vmmap --summary " + pid + " 2>/dev/null | grep 'Physical footprint:'");
            if (vmmapOutput.length > 0 && !vmmapOutput[0].isEmpty()) {
                privateMemory = parseMemoryValue(vmmapOutput[0]);
            }
        } catch (Exception e) {
            // ignore
        }

        if (privateMemory == 0) {
            privateMemory = rss;
        }

        sharedMemory = Math.max(0, rss - privateMemory);

        return new MemoryDetailInfo(rss, vsz, sharedMemory, privateMemory);
    }

    private long parseMemoryValue(String line) {
        try {
            String[] parts = line.split(":");
            if (parts.length >= 2) {
                String value = parts[1].trim();
                if (value.endsWith("K")) {
                    return (long) (Double.parseDouble(value.replace("K", "").trim()) * 1024);
                } else if (value.endsWith("M")) {
                    return (long) (Double.parseDouble(value.replace("M", "").trim()) * 1024 * 1024);
                } else if (value.endsWith("G")) {
                    return (long) (Double.parseDouble(value.replace("G", "").trim()) * 1024 * 1024 * 1024);
                } else if (value.endsWith("B")) {
                    return Long.parseLong(value.replace("B", "").trim());
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }

    public StatisticsInfo getStatistics(int pid) {
        OSProcess process = os.getProcess(pid);
        if (process == null) {
            return new StatisticsInfo("0:00:00", 0, 0, 0, 0);
        }

        long totalTime = process.getKernelTime() + process.getUserTime();
        String cpuTime = formatCpuTime(totalTime);
        long contextSwitches = process.getContextSwitches();
        int threads = process.getThreadCount();
        int ports = countOpenPorts(pid);

        return new StatisticsInfo(cpuTime, contextSwitches, threads, ports, 0);
    }

    private String formatCpuTime(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        long centiseconds = (millis % 1000) / 10;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d.%02d", hours, minutes, seconds, centiseconds);
        }
        return String.format("%02d:%02d.%02d", minutes, seconds, centiseconds);
    }

    private int countOpenPorts(int pid) {
        try {
            String[] output = executeCommand("lsof -i -P -n -p " + pid + " 2>/dev/null | wc -l");
            if (output.length > 0 && !output[0].isEmpty()) {
                int count = Integer.parseInt(output[0].trim());
                return Math.max(0, count - 1);
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }

    public List<OpenFileInfo> getOpenFiles(int pid) {
        List<OpenFileInfo> files = new ArrayList<>();

        try {
            String[] output = executeCommand("lsof -p " + pid + " 2>/dev/null");
            boolean firstLine = true;
            for (String line : output) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length >= 9) {
                    String fd = parts[3];
                    String type = parts[4];
                    String name = parts[parts.length - 1];
                    files.add(new OpenFileInfo(fd, type, name));
                }
            }
        } catch (Exception e) {
            // ignore
        }

        return files;
    }

    private String[] executeCommand(String command) {
        List<String> result = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.add(line);
                }
            }

            process.waitFor();
        } catch (Exception e) {
            // ignore
        }
        return result.toArray(new String[0]);
    }
}
