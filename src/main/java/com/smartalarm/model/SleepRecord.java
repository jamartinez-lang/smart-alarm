package com.smartalarm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;

public class SleepRecord {

    private final String alarmId;
    private final LocalDate date;
    private final LocalDateTime scheduledWakeTime;
    private LocalDateTime actualWakeTime;
    private int snoozeCount;
    private boolean dismissedOnTime;

    public SleepRecord(String alarmId, LocalDateTime scheduledWakeTime) {
        this.alarmId = alarmId;
        this.date = scheduledWakeTime.toLocalDate();
        this.scheduledWakeTime = scheduledWakeTime;
        this.snoozeCount = 0;
        this.dismissedOnTime = false;
    }

    public void recordWake(LocalDateTime wakeTime, int snoozeCount) {
        this.actualWakeTime = wakeTime;
        this.snoozeCount = snoozeCount;
        this.dismissedOnTime = Duration.between(scheduledWakeTime, wakeTime).toMinutes() <= 1;
    }

    public Duration getSleepDelay() {
        if (actualWakeTime == null) return Duration.ZERO;
        return Duration.between(scheduledWakeTime, actualWakeTime);
    }

    public String getAlarmId() { return alarmId; }
    public LocalDate getDate() { return date; }
    public LocalDateTime getScheduledWakeTime() { return scheduledWakeTime; }
    public LocalDateTime getActualWakeTime() { return actualWakeTime; }
    public int getSnoozeCount() { return snoozeCount; }
    public boolean isDismissedOnTime() { return dismissedOnTime; }

    @Override
    public String toString() {
        return String.format("SleepRecord{date=%s, scheduled=%s, actual=%s, snoozes=%d, onTime=%s}",
                date, scheduledWakeTime.toLocalTime(),
                actualWakeTime != null ? actualWakeTime.toLocalTime() : "pending",
                snoozeCount, dismissedOnTime);
    }
}