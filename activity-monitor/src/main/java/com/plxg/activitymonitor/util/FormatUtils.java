package com.plxg.activitymonitor.util;

public class FormatUtils {

    private static final long KB = 1024L;
    private static final long MB = KB * 1024L;
    private static final long GB = MB * 1024L;


    //chuyen bytes sag don vi phu hop
    public static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "0 B";
        }
        if (bytes < KB) {
            return bytes + " B";
        } else if (bytes < MB) {
            return String.format("%.2f KB", bytes / (double)KB);
        } else if (bytes < GB) {
            return String.format("%.2f MB", bytes / (double)MB);
        } else {
            return String.format("%.2f GB", bytes / (double)GB);
        }
    }

    //format %:
    public static String formatPercent(double percent) {
        return String.format("%.1f %", percent);
    }

    //format tgian hh:mm:ss
    public static String formatTime(long milis){
        if (milis < 0) {
            return "00:00:00";
        }
        long totalSeconds = milis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    // format so lon voi dau cham
    public static String formatNumber(long number) {
        if (number < 1000) {
            return String.valueOf(number);
        }
        return String.format("%,d", number).replace(",", ".");
    }
}
