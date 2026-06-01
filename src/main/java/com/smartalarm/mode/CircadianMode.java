package com.smartalarm.mode;

import com.smartalarm.model.Alarm;
import com.smartalarm.model.SoundProfile;

public class CircadianMode {

    public enum Phase {
        LIGHT_SLEEP_PREP,
        GENTLE_RAMP,
        FULL_ALARM
    }

    private static final int PREP_MINUTES = 30;
    private static final int RAMP_MINUTES = 15;

    private final Alarm alarm;
    private Phase currentPhase;
    private boolean active;

    public CircadianMode(Alarm alarm) {
        this.alarm = alarm;
        this.active = false;
        this.currentPhase = Phase.LIGHT_SLEEP_PREP;
    }

    public void activate() {
        SoundProfile profile = alarm.getSoundProfile();
        profile.setSoundType(SoundProfile.SoundType.NATURE);
        profile.setGradualVolume(true);
        profile.setGradualDurationSeconds(Math.min(RAMP_MINUTES * 60, 300));
        profile.setVolume(85);
        alarm.setCircadianMode(true);
        this.active = true;
        System.out.printf("[CircadianMode] Activado para alarma '%s'. Sonidos de naturaleza con volumen gradual.%n",
                alarm.getLabel());
    }

    public void deactivate() {
        alarm.setCircadianMode(false);
        this.active = false;
        System.out.printf("[CircadianMode] Desactivado para alarma '%s'.%n", alarm.getLabel());
    }

    public Phase getCurrentPhase(int minutesUntilAlarm) {
        if (minutesUntilAlarm <= 0) return Phase.FULL_ALARM;
        if (minutesUntilAlarm <= RAMP_MINUTES) return Phase.GENTLE_RAMP;
        if (minutesUntilAlarm <= PREP_MINUTES) return Phase.LIGHT_SLEEP_PREP;
        return Phase.LIGHT_SLEEP_PREP;
    }

    public String simulatePhase(int minutesUntilAlarm) {
        Phase phase = getCurrentPhase(minutesUntilAlarm);
        int effectiveVolume = alarm.getSoundProfile().getEffectiveVolume(
                (RAMP_MINUTES - Math.max(0, minutesUntilAlarm)) * 60
        );

        return switch (phase) {
            case LIGHT_SLEEP_PREP -> String.format(
                    "[Circadiano] Fase preparacion (%d min). Sonidos suaves de naturaleza. Volumen: 10%%", minutesUntilAlarm);
            case GENTLE_RAMP -> String.format(
                    "[Circadiano] Fase rampa (%d min). Volumen actual: %d%%", minutesUntilAlarm, effectiveVolume);
            case FULL_ALARM -> String.format(
                    "[Circadiano] Alarma completa. Volumen: %d%%", alarm.getSoundProfile().getVolume());
        };
    }

    public boolean isActive() { return active; }
    public Phase getCurrentPhase() { return currentPhase; }
    public Alarm getAlarm() { return alarm; }
}