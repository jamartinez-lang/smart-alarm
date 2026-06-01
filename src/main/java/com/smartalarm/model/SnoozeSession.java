package com.smartalarm.model;

import java.time.LocalDateTime;

public class SnoozeSession {

    private final String alarmId;
    private int snoozeCount;
    private LocalDateTime snoozeUntil;
    private final int maxSnoozeCount;
    private final int snoozeDurationMinutes;

    public SnoozeSession(String alarmId, int maxSnoozeCount, int snoozeDurationMinutes) {
        this.alarmId = alarmId;
        this.snoozeCount = 0;
        this.maxSnoozeCount = maxSnoozeCount;
        this.snoozeDurationMinutes = snoozeDurationMinutes;
        this.snoozeUntil = null;
    }

    public boolean snooze(LocalDateTime now) {
        if (snoozeCount >= maxSnoozeCount) return false;
        snoozeCount++;
        snoozeUntil = now.plusMinutes(snoozeDurationMinutes);
        return true;
    }

    public boolean isSnoozed(LocalDateTime now) {
        return snoozeUntil != null && now.isBefore(snoozeUntil);
    }

    public boolean hasExhaustedSnooze() {
        return snoozeCount >= maxSnoozeCount;
    }

    public String getAlarmId() { return alarmId; }
    public int getSnoozeCount() { return snoozeCount; }
    public LocalDateTime getSnoozeUntil() { return snoozeUntil; }
    public int getMaxSnoozeCount() { return maxSnoozeCount; }
    public int getSnoozeDurationMinutes() { return snoozeDurationMinutes; }

    @Override
    public String toString() {
        return String.format("SnoozeSession{alarmId='%s', count=%d/%d, until=%s}",
                alarmId.substring(0, 8), snoozeCount, maxSnoozeCount, snoozeUntil);
    }
}