package com.smartalarm.manager;

import com.smartalarm.model.Alarm;
import com.smartalarm.model.SnoozeSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SnoozeManager {

    private final Map<String, SnoozeSession> activeSessions;

    public SnoozeManager() {
        this.activeSessions = new HashMap<>();
    }

    public boolean snooze(Alarm alarm) {
        return snooze(alarm, LocalDateTime.now());
    }

    public boolean snooze(Alarm alarm, LocalDateTime now) {
        String id = alarm.getId();
        SnoozeSession session = activeSessions.computeIfAbsent(id,
                k -> new SnoozeSession(id, alarm.getMaxSnoozeCount(), alarm.getSnoozeDurationMinutes()));

        boolean applied = session.snooze(now);
        if (!applied) {
            System.out.printf("[SnoozeManager] Alarma '%s' ha agotado los %d aplazamientos.%n",
                    alarm.getLabel(), alarm.getMaxSnoozeCount());
        } else {
            System.out.printf("[SnoozeManager] Alarma '%s' aplazada %d min (vez %d/%d). Suena a las %s%n",
                    alarm.getLabel(), alarm.getSnoozeDurationMinutes(),
                    session.getSnoozeCount(), alarm.getMaxSnoozeCount(),
                    session.getSnoozeUntil().toLocalTime());
        }
        return applied;
    }

    public boolean isSnoozed(String alarmId) {
        return isSnoozed(alarmId, LocalDateTime.now());
    }

    public boolean isSnoozed(String alarmId, LocalDateTime now) {
        SnoozeSession session = activeSessions.get(alarmId);
        return session != null && session.isSnoozed(now);
    }

    public void dismiss(String alarmId) {
        activeSessions.remove(alarmId);
    }

    public Optional<SnoozeSession> getSession(String alarmId) {
        return Optional.ofNullable(activeSessions.get(alarmId));
    }

    public int getSnoozeCount(String alarmId) {
        SnoozeSession session = activeSessions.get(alarmId);
        return session != null ? session.getSnoozeCount() : 0;
    }

    public void clearAll() {
        activeSessions.clear();
    }
}