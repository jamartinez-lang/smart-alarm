package com.smartalarm.manager;

import com.smartalarm.model.Alarm;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class Scheduler {

    private static final int TRIGGER_WINDOW_SECONDS = 60;

    private final AlarmManager alarmManager;

    public Scheduler(AlarmManager alarmManager) {
        this.alarmManager = alarmManager;
    }

    public List<Alarm> getTriggeredAlarms() {
        return getTriggeredAlarms(LocalDateTime.now());
    }

    public List<Alarm> getTriggeredAlarms(LocalDateTime now) {
        return alarmManager.getActiveAlarms().stream()
                .filter(a -> isTriggering(a, now))
                .collect(Collectors.toList());
    }

    public boolean isTriggering(Alarm alarm, LocalDateTime now) {
        LocalDateTime next = alarm.getNextTriggerTime(now.minusSeconds(TRIGGER_WINDOW_SECONDS));
        if (next == null) return false;
        long diff = Duration.between(next, now).toSeconds();
        return diff >= 0 && diff <= TRIGGER_WINDOW_SECONDS;
    }

    public String getScheduleSummary(int alarmCount) {
        LocalDateTime now = LocalDateTime.now();
        List<Alarm> upcoming = alarmManager.getUpcomingAlarms(alarmCount);

        if (upcoming.isEmpty()) return "No hay alarmas activas programadas.";

        StringBuilder sb = new StringBuilder("=== PRÓXIMAS ALARMAS ===\n");
        for (Alarm alarm : upcoming) {
            LocalDateTime next = alarm.getNextTriggerTime(now);
            Duration until = Duration.between(now, next);
            long hours = until.toHours();
            long minutes = until.toMinutesPart();

            sb.append(String.format("  [%s] '%s' a las %s",
                    alarm.getCategory().getDisplayName(),
                    alarm.getLabel(),
                    next.toLocalTime()));

            if (hours > 0) sb.append(String.format(" (en %dh %dm)", hours, minutes));
            else sb.append(String.format(" (en %d min)", minutes));

            if (alarm.isCircadianMode()) sb.append(" [Circadiano]");
            sb.append("\n");
        }
        return sb.toString();
    }

    public List<Alarm> simulate(LocalDateTime simulatedTime) {
        return getTriggeredAlarms(simulatedTime);
    }
}