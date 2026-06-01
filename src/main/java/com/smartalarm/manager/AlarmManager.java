package com.smartalarm.manager;

import com.smartalarm.model.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central manager for all alarm operations.
 * Handles CRUD, state changes, conflict detection, and upcoming alarm queries.
 * Single point of entry for alarm lifecycle management.
 */
public class AlarmManager {

    private static final int CONFLICT_THRESHOLD_MINUTES = 5;

    private final Map<String, Alarm> alarms;

    public AlarmManager() {
        this.alarms = new LinkedHashMap<>();
    }

    // =========== CRUD ===========

    /**
     * Adds a new alarm. Detects conflicts with nearby alarms.
     * @return the created alarm
     */
    public Alarm addAlarm(Alarm alarm) {
        Objects.requireNonNull(alarm, "Alarm cannot be null");
        alarms.put(alarm.getId(), alarm);
        return alarm;
    }

    /**
     * Creates and adds a simple alarm at a given time with a label.
     */
    public Alarm createAlarm(String label, LocalTime time) {
        Alarm alarm = new Alarm(label, time);
        return addAlarm(alarm);
    }

    /**
     * Removes an alarm by ID.
     * @return true if removed, false if not found
     */
    public boolean removeAlarm(String alarmId) {
        return alarms.remove(alarmId) != null;
    }

    /**
     * Retrieves an alarm by ID.
     */
    public Optional<Alarm> getAlarm(String alarmId) {
        return Optional.ofNullable(alarms.get(alarmId));
    }

    /**
     * Returns all alarms (unmodifiable view).
     */
    public List<Alarm> getAllAlarms() {
        return Collections.unmodifiableList(new ArrayList<>(alarms.values()));
    }

    // =========== ACTIVATION ===========

    public void activateAlarm(String alarmId) {
        getAlarm(alarmId).ifPresent(a -> a.setActive(true));
    }

    public void deactivateAlarm(String alarmId) {
        getAlarm(alarmId).ifPresent(a -> a.setActive(false));
    }

    public void toggleAlarm(String alarmId) {
        getAlarm(alarmId).ifPresent(a -> a.setActive(!a.isActive()));
    }

    // =========== QUERIES ===========

    /**
     * Returns the next N active alarms sorted by next trigger time.
     */
    public List<Alarm> getUpcomingAlarms(int count) {
        LocalDateTime now = LocalDateTime.now();
        return alarms.values().stream()
                .filter(Alarm::isActive)
                .filter(a -> a.getNextTriggerTime(now) != null)
                .sorted(Comparator.comparing(a -> a.getNextTriggerTime(now)))
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Returns all active alarms.
     */
    public List<Alarm> getActiveAlarms() {
        return alarms.values().stream()
                .filter(Alarm::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Returns alarms filtered by category.
     */
    public List<Alarm> getAlarmsByCategory(AlarmCategory category) {
        return alarms.values().stream()
                .filter(a -> a.getCategory() == category)
                .collect(Collectors.toList());
    }

    // =========== CONFLICT DETECTION ===========

    /**
     * Detects alarms that would trigger within CONFLICT_THRESHOLD_MINUTES of each other.
     * @return list of conflicting alarm pairs as descriptive messages
     */
    public List<String> detectConflicts() {
        LocalDateTime now = LocalDateTime.now();
        List<Alarm> active = getActiveAlarms();
        List<String> conflicts = new ArrayList<>();

        for (int i = 0; i < active.size(); i++) {
            for (int j = i + 1; j < active.size(); j++) {
                Alarm a = active.get(i);
                Alarm b = active.get(j);

                LocalDateTime nextA = a.getNextTriggerTime(now);
                LocalDateTime nextB = b.getNextTriggerTime(now);

                if (nextA != null && nextB != null) {
                    long diff = Math.abs(Duration.between(nextA, nextB).toMinutes());
                    if (diff <= CONFLICT_THRESHOLD_MINUTES) {
                        conflicts.add(String.format(
                                "CONFLICTO: '%s' (%s) y '%s' (%s) tienen %d min de diferencia",
                                a.getLabel(), nextA.toLocalTime(),
                                b.getLabel(), nextB.toLocalTime(),
                                diff
                        ));
                    }
                }
            }
        }
        return conflicts;
    }

    /**
     * Checks if a new alarm at the given time would conflict with existing ones.
     */
    public boolean wouldConflict(LocalTime proposedTime) {
        return alarms.values().stream()
                .filter(Alarm::isActive)
                .anyMatch(a -> {
                    long diff = Math.abs(Duration.between(a.getTime(), proposedTime).toMinutes());
                    return diff <= CONFLICT_THRESHOLD_MINUTES;
                });
    }

    // =========== VACATION MODE ===========

    /**
     * Deactivates ALL alarms (vacation mode).
     * @return number of alarms deactivated
     */
    public int activateVacationMode() {
        int count = 0;
        for (Alarm alarm : alarms.values()) {
            if (alarm.isActive()) {
                alarm.setActive(false);
                count++;
            }
        }
        return count;
    }

    /**
     * Reactivates all alarms (exit vacation mode).
     * @return number of alarms reactivated
     */
    public int deactivateVacationMode() {
        int count = 0;
        for (Alarm alarm : alarms.values()) {
            if (!alarm.isActive()) {
                alarm.setActive(true);
                count++;
            }
        }
        return count;
    }

    // =========== SUMMARY ===========

    public int getTotalAlarmCount() { return alarms.size(); }
    public int getActiveAlarmCount() { return (int) alarms.values().stream().filter(Alarm::isActive).count(); }

    @Override
    public String toString() {
        return String.format("AlarmManager{total=%d, active=%d}", getTotalAlarmCount(), getActiveAlarmCount());
    }
}
