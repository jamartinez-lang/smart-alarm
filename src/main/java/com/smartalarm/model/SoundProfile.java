package com.smartalarm.model;

import java.util.Objects;

public class SoundProfile {

    public enum SoundType {
        BEEP("Pitido estándar"),
        MUSIC("Música"),
        NATURE("Sonidos de naturaleza"),
        GRADUAL("Despertar gradual"),
        SILENT("Silencioso (vibración)");

        private final String displayName;
        SoundType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    private String soundName;
    private SoundType soundType;
    private int volume;
    private boolean gradualVolume;
    private int gradualDurationSeconds;

    public SoundProfile() {
        this.soundName = "default_beep";
        this.soundType = SoundType.BEEP;
        this.volume = 70;
        this.gradualVolume = false;
        this.gradualDurationSeconds = 30;
    }

    public SoundProfile(String soundName, SoundType soundType, int volume) {
        this();
        this.soundName = Objects.requireNonNull(soundName);
        this.soundType = Objects.requireNonNull(soundType);
        setVolume(volume);
    }

    public String getSoundName() { return soundName; }
    public SoundType getSoundType() { return soundType; }
    public int getVolume() { return volume; }
    public boolean isGradualVolume() { return gradualVolume; }
    public int getGradualDurationSeconds() { return gradualDurationSeconds; }

    public void setSoundName(String soundName) {
        this.soundName = Objects.requireNonNull(soundName);
    }

    public void setSoundType(SoundType soundType) {
        this.soundType = Objects.requireNonNull(soundType);
    }

    public void setVolume(int volume) {
        if (volume < 0 || volume > 100) throw new IllegalArgumentException("Volume must be 0-100");
        this.volume = volume;
    }

    public void setGradualVolume(boolean gradualVolume) {
        this.gradualVolume = gradualVolume;
    }

    public void setGradualDurationSeconds(int seconds) {
        if (seconds < 5 || seconds > 300) throw new IllegalArgumentException("Gradual duration must be 5-300 seconds");
        this.gradualDurationSeconds = seconds;
    }

    public int getEffectiveVolume(int elapsedSeconds) {
        if (!gradualVolume) return volume;
        if (elapsedSeconds >= gradualDurationSeconds) return volume;
        return (int) ((double) elapsedSeconds / gradualDurationSeconds * volume);
    }

    @Override
    public String toString() {
        return String.format("SoundProfile{sound='%s', type=%s, volume=%d%s}",
                soundName, soundType, volume, gradualVolume ? " (gradual)" : "");
    }
}