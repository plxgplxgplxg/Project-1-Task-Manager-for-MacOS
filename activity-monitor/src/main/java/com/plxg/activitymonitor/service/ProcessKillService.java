package com.plxg.activitymonitor.service;

import java.io.IOException;

public class ProcessKillService {

    public enum KillResult {
        SUCCESS,
        ACCESS_DENIED,
        NOT_FOUND,
        FAILED
    }

    public KillResult killProcess(int pid) {
        if (pid <= 0) {
            return KillResult.NOT_FOUND;
        }

        return ProcessHandle.of(pid)
                .map(this::tryKill)
                .orElse(KillResult.NOT_FOUND);
    }

    private KillResult tryKill(ProcessHandle handle) {
        try {
            boolean destroyed = handle.destroyForcibly();
            if (destroyed) {
                return waitForTermination(handle);
            }
            return KillResult.FAILED;
        } catch (SecurityException e) {
            return KillResult.ACCESS_DENIED;
        } catch (Exception e) {
            return KillResult.FAILED;
        }
    }

    private KillResult waitForTermination(ProcessHandle handle) {
        try {
            Thread.sleep(100);
            if (!handle.isAlive()) {
                return KillResult.SUCCESS;
            }
            return killWithSignal(handle.pid());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return KillResult.FAILED;
        }
    }

    private KillResult killWithSignal(long pid) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"kill", "-9", String.valueOf(pid)});
            int exitCode = process.waitFor();
            return exitCode == 0 ? KillResult.SUCCESS : KillResult.ACCESS_DENIED;
        } catch (IOException | InterruptedException e) {
            return KillResult.FAILED;
        }
    }

    public boolean canKillProcess(int pid) {
        return ProcessHandle.of(pid)
                .map(h -> !h.info().user().orElse("").equals("root"))
                .orElse(false);
    }
}
