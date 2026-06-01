package com.smartalarm.mode;

import com.smartalarm.model.Alarm;

public class GeoAlarm {

    private final Alarm alarm;
    private double targetLatitude;
    private double targetLongitude;
    private double radiusMeters;
    private boolean enabled;

    public GeoAlarm(Alarm alarm, double targetLatitude, double targetLongitude, double radiusMeters) {
        this.alarm = alarm;
        this.targetLatitude = targetLatitude;
        this.targetLongitude = targetLongitude;
        this.radiusMeters = radiusMeters;
        this.enabled = true;
    }

    public boolean shouldTrigger(double currentLat, double currentLon) {
        if (!enabled || !alarm.isActive()) return false;
        double distance = haversineDistance(currentLat, currentLon, targetLatitude, targetLongitude);
        boolean inRange = distance <= radiusMeters;
        System.out.printf("[GeoAlarm] Alarma '%s': distancia al punto=%.0fm, radio=%.0fm, enRango=%s%n",
                alarm.getLabel(), distance, radiusMeters, inRange);
        return inRange;
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }
    public Alarm getAlarm() { return alarm; }
    public double getTargetLatitude() { return targetLatitude; }
    public double getTargetLongitude() { return targetLongitude; }
    public double getRadiusMeters() { return radiusMeters; }

    public void setLocation(double lat, double lon) {
        this.targetLatitude = lat;
        this.targetLongitude = lon;
    }

    @Override
    public String toString() {
        return String.format("GeoAlarm{alarm='%s', lat=%.4f, lon=%.4f, radius=%.0fm, enabled=%s}",
                alarm.getLabel(), targetLatitude, targetLongitude, radiusMeters, enabled);
    }
}