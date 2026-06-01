package com.smartalarm.stats;

import com.smartalarm.model.SleepRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SleepStatistics {

    private final List<SleepRecord> records;

    public SleepStatistics() {
        this.records = new ArrayList<>();
    }

    public void addRecord(SleepRecord record) {
        records.add(record);
    }

    public List<SleepRecord> getAllRecords() {
        return Collections.unmodifiableList(records);
    }

    public int getTotalSnoozeCount() {
        return records.stream().mapToInt(SleepRecord::getSnoozeCount).sum();
    }

    public double getAverageSnoozeCount() {
        if (records.isEmpty()) return 0.0;
        return (double) getTotalSnoozeCount() / records.size();
    }

    public double getPunctualityRate() {
        if (records.isEmpty()) return 0.0;
        long onTime = records.stream().filter(SleepRecord::isDismissedOnTime).count();
        return (double) onTime / records.size() * 100;
    }

    public double getAverageDelayMinutes() {
        List<SleepRecord> completed = records.stream()
                .filter(r -> r.getActualWakeTime() != null)
                .collect(Collectors.toList());
        if (completed.isEmpty()) return 0.0;
        return completed.stream()
                .mapToLong(r -> r.getSleepDelay().toMinutes())
                .average()
                .orElse(0.0);
    }

    public List<SleepRecord> getRecordsForDate(LocalDate date) {
        return records.stream()
                .filter(r -> r.getDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<SleepRecord> getRecordsLastDays(int days) {
        LocalDate cutoff = LocalDate.now().minusDays(days);
        return records.stream()
                .filter(r -> !r.getDate().isBefore(cutoff))
                .collect(Collectors.toList());
    }

    public String getSummaryReport() {
        if (records.isEmpty()) return "Sin registros de sueno disponibles.";

        return String.format(
                """
                === PERFIL DE SUENO ===
                Sesiones registradas : %d
                Total aplazamientos  : %d
                Media aplazamientos  : %.1f por sesion
                Puntualidad          : %.1f%%
                Retraso medio        : %.1f minutos
                """,
                records.size(),
                getTotalSnoozeCount(),
                getAverageSnoozeCount(),
                getPunctualityRate(),
                getAverageDelayMinutes()
        );
    }
}