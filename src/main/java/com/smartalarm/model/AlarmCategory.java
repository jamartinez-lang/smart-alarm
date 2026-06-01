package com.smartalarm.model;

public enum AlarmCategory {
    GENERAL("General"),
    WORK("Trabajo"),
    STUDY("Estudio"),
    SPORT("Deporte"),
    MEDICINE("Medicina"),
    PERSONAL("Personal"),
    VACATION("Vacaciones");

    private final String displayName;

    AlarmCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}