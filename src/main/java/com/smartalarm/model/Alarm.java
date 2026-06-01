package com.smartalarm.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a single alarm with all its configuration.
 * Encapsulates time, recurrence, sound, and snooze behavior.
 */
public class Alarm {

    public enum RepeatMode {
        NONE, DAILY, WEEKDAYS, WEEKENDS, CUSTOM
    }

    private final String id;
    private String label;
    private LocalTime time;
    private boolean active;
    private RepeatMode repeatMode;
    private Set<DayOfWeek> customDays;
    private SoundProfile soundProfile;
    private int snoozeDurationMinutes;
    private int maxSnoozeCount;
    private AlarmCategory category;
    private boolean circadianMode;
    private String location; // for geo-fenced alarms (simulated)
    private LocalDateTime createdAt;

    public Alarm(String label, LocalTime time) {
        this.id = UUID.randomUUID().toString();
        this.label = Objects.requireNonNull(label, "Label cannot be null");
        this.time = Objects.requireNonNull(time, "Time cannot be null");
        this.active = true;
        this.repeatMode = RepeatMode.NONE;
        this.customDays = EnumSet.noneOf(DayOfWeek.class);
        this.soundProfile = new SoundProfile();
        this.snoozeDurationMinutes = 9;
        this.maxSnoozeCount = 3;
        this.category = AlarmCategory.GENERAL;
        this.circadianMode = false;
        this.location = null;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getLabel() { return label; }
    public LocalTime getTime() { return time; }
    public boolean isActive() { return active; }
    public RepeatMode getRepeatMode() { return repeatMode; }
    public Set<DayOfWeek> getCustomDays() { return EnumSet.copyOf(customDays.isEmpty() ? EnumSet.noneOf(DayOfWeek.class) : customDays); }
    public SoundProfile getSoundProfile() { return soundProfile; }
    public int getSnoozeDurationMinutes() { return snoozeDurationMinutes; }
    public int getMaxSnoozeCount() { return maxSnoozeCount; }
    public AlarmCategory getCategory() { return category; }
    public boolean isCircadianMode() { return circadianMode; }
    public String getLocation() { return location; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Setters with validation ---

    public void setLabel(String label) {
        this.label = Objects.requireNonNull(label, "Label cannot be null");
    }

    public void setTime(LocalTime time) {
        this.time = Objects.requireNonNull(time, "Time cannot be null");
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setRepeatMode(RepeatMode repeatMode) {
        this.repeatMode = Objects.requireNonNull(repeatMode);
        if (repeatMode != RepeatMode.CUSTOM) {
            this.customDays.clear();
        }
    }

    public void setCustomDays(Set<DayOfWeek> days) {
        Objects.requireNonNull(days, "Days cannot be null");
        if (days.isEmpty()) throw new IllegalArgumentException("Custom days cannot be empty");
        this.customDays = EnumSet.copyOf(days);
        this.repeatMode = RepeatMode.CUSTOM;
    }

    public void setSoundProfile(SoundProfile soundProfile) {
        this.soundProfile = Objects.requireNonNull(soundProfile);
    }

    public void setSnoozeDurationMinutes(int minutes) {
        if (minutes < 1 || minutes > 60) throw new IllegalArgumentException("Snooze duration must be 1-60 minutes");
        this.snoozeDurationMinutes = minutes;
    }

    public void setMaxSnoozeCount(int count) {
        if (count < 0 || count > 10) throw new IllegalArgumentException("Max snooze count must be 0-10");
        this.maxSnoozeCount = count;
    }

    public void setCategory(AlarmCategory category) {
        this.category = Objects.requireNonNull(category);
    }

    public void setCircadianMode(boolean circadianMode) {
        this.circadianMode = circadianMode;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Checks if this alarm should ring on a given day of week.
     */
    public boolean shouldRingOn(DayOfWeek day) {
        return switch (repeatMode) {
            case NONE -> false; // handled separately for one-shot alarms
            case DAILY -> true;
            case WEEKDAYS -> day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
            case WEEKENDS -> day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
            case CUSTOM -> customDays.contains(day);
        };
    }

    /**
     * Returns next trigger time from now.
     */
    public LocalDateTime getNextTriggerTime(LocalDateTime from) {
        if (!active) return null;

        LocalDateTime candidate = from.withSecond(0).withNano(0);
        LocalTime alarmTime = time.withSecond(0).withNano(0);

        // Set to today at alarm time
        candidate = candidate.withHour(alarmTime.getHour()).withMinute(alarmTime.getMinute());

        // If already passed today, move to next occurrence
        if (!candidate.isAfter(from)) {
            candidate = candidate.plusDays(1);
        }

        if (repeatMode == RepeatMode.NONE) {
            return candidate;
        }

        // Find next valid day
        for (int i = 0; i < 8; i++) {
            if (shouldRingOn(candidate.getDayOfWeek())) {
                return candidate;
            }
            candidate = candidate.plusDays(1);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alarm alarm)) return false;
        return id.equals(alarm.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Alarm{id='%s', label='%s', time=%s, active=%s, repeat=%s, category=%s}",
                id.substring(0, 8), label, time, active, repeatMode, category);
    }
}
