package com.playercontract.utils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhd])");

    public static long parseTime(String timeString) {
        long totalSeconds = 0;
        Matcher matcher = TIME_PATTERN.matcher(timeString.toLowerCase());
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
                case "s":
                    totalSeconds += value;
                    break;
                case "m":
                    totalSeconds += TimeUnit.MINUTES.toSeconds(value);
                    break;
                case "h":
                    totalSeconds += TimeUnit.HOURS.toSeconds(value);
                    break;
                case "d":
                    totalSeconds += TimeUnit.DAYS.toSeconds(value);
                    break;
            }
        }
        return totalSeconds > 0 ? System.currentTimeMillis() + (totalSeconds * 1000) : 0;
    }

    public static String formatTime(long expiryTimestamp) {
        if (expiryTimestamp == 0) {
            return "Never";
        }
        long remainingMillis = expiryTimestamp - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            return "Expired";
        }

        long days = TimeUnit.MILLISECONDS.toDays(remainingMillis);
        remainingMillis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(remainingMillis);
        remainingMillis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m");

        if (sb.length() == 0) {
            return "<1m";
        }

        return sb.toString().trim();
    }

    public static String formatTimeElapsed(long pastTimestamp) {
        if (pastTimestamp == 0) {
            return "N/A";
        }
        long elapsedMillis = System.currentTimeMillis() - pastTimestamp;
        if (elapsedMillis < 0) {
            return "In the future"; // Should not happen
        }

        long days = TimeUnit.MILLISECONDS.toDays(elapsedMillis);
        elapsedMillis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis);
        elapsedMillis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis);
        elapsedMillis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (minutes < 1 && hours < 1 && days < 1) {
            if (seconds > 0) sb.append(seconds).append("s");
        }


        if (sb.length() == 0) {
            return "<1s ago";
        }

        return sb.toString().trim() + " ago";
    }
}
