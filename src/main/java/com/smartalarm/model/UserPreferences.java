package com.smartalarm.model;

import com.smartalarm.model.MathChallenge.Difficulty;

public class UserPreferences {

    private int defaultSnoozeDurationMinutes;
    private int defaultMaxSnoozeCount;
    private int defaultVolume;
    private SoundProfile.SoundType defaultSoundType;
    private boolean mathChallengeEnabled;
    private Difficulty mathChallengeDifficulty;
    private boolean vacationMode;
    private boolean conflictDetectionEnabled;

    public UserPreferences() {
        this.defaultSnoozeDurationMinutes = 9;
        this.defaultMaxSnoozeCount = 3;
        this.defaultVolume = 70;
        this.defaultSoundType = SoundProfile.SoundType.BEEP;
        this.mathChallengeEnabled = false;
        this.mathChallengeDifficulty = Difficulty.MEDIUM;
        this.vacationMode = false;
        this.conflictDetectionEnabled = true;
    }

    public int getDefaultSnoozeDurationMinutes() { return defaultSnoozeDurationMinutes; }
    public int getDefaultMaxSnoozeCount() { return defaultMaxSnoozeCount; }
    public int getDefaultVolume() { return defaultVolume; }
    public SoundProfile.SoundType getDefaultSoundType() { return defaultSoundType; }
    public boolean isMathChallengeEnabled() { return mathChallengeEnabled; }
    public Difficulty getMathChallengeDifficulty() { return mathChallengeDifficulty; }
    public boolean isVacationMode() { return vacationMode; }
    public boolean isConflictDetectionEnabled() { return conflictDetectionEnabled; }

    public void setDefaultSnoozeDurationMinutes(int minutes) {
        if (minutes < 1 || minutes > 60) throw new IllegalArgumentException("Snooze duration must be 1-60");
        this.defaultSnoozeDurationMinutes = minutes;
    }

    public void setDefaultMaxSnoozeCount(int count) {
        if (count < 0 || count > 10) throw new IllegalArgumentException("Max snooze count must be 0-10");
        this.defaultMaxSnoozeCount = count;
    }

    public void setDefaultVolume(int volume) {
        if (volume < 0 || volume > 100) throw new IllegalArgumentException("Volume must be 0-100");
        this.defaultVolume = volume;
    }

    public void setDefaultSoundType(SoundProfile.SoundType soundType) {
        this.defaultSoundType = soundType;
    }

    public void setMathChallengeEnabled(boolean enabled) { this.mathChallengeEnabled = enabled; }
    public void setMathChallengeDifficulty(Difficulty difficulty) { this.mathChallengeDifficulty = difficulty; }
    public void setVacationMode(boolean vacationMode) { this.vacationMode = vacationMode; }
    public void setConflictDetectionEnabled(boolean enabled) { this.conflictDetectionEnabled = enabled; }

    @Override
    public String toString() {
        return String.format(
                "UserPreferences{snooze=%dmin x%d, volume=%d, mathChallenge=%s(%s), vacation=%s}",
                defaultSnoozeDurationMinutes, defaultMaxSnoozeCount, defaultVolume,
                mathChallengeEnabled, mathChallengeDifficulty, vacationMode);
    }
}