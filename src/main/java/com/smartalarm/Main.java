package com.smartalarm;

import com.smartalarm.manager.AlarmManager;
import com.smartalarm.manager.Scheduler;
import com.smartalarm.manager.SnoozeManager;
import com.smartalarm.mode.CircadianMode;
import com.smartalarm.mode.GeoAlarm;
import com.smartalarm.model.*;
import com.smartalarm.model.MathChallenge.Difficulty;
import com.smartalarm.stats.SleepStatistics;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Entry point for testing and demonstrating all SmartAlarm features.
 * No GUI — logic is validated entirely through this main class.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║      SMART ALARM SYSTEM v1.0          ║");
        System.out.println("╚══════════════════════════════════════╝\n");

        AlarmManager manager = new AlarmManager();
        SnoozeManager snoozeManager = new SnoozeManager();
        Scheduler scheduler = new Scheduler(manager);
        SleepStatistics stats = new SleepStatistics();
        UserPreferences prefs = new UserPreferences();

        // ============================================================
        // TEST 1: Crear y gestionar alarmas básicas
        // ============================================================
        printSection("1. GESTIÓN BÁSICA DE ALARMAS");

        Alarm alarm1 = manager.createAlarm("Levantarse", LocalTime.of(7, 0));
        Alarm alarm2 = manager.createAlarm("Gym", LocalTime.of(6, 30));
        Alarm alarm3 = manager.createAlarm("Reunión", LocalTime.of(9, 0));

        alarm1.setCategory(AlarmCategory.GENERAL);
        alarm2.setCategory(AlarmCategory.SPORT);
        alarm3.setCategory(AlarmCategory.WORK);

        System.out.println("Alarmas creadas:");
        manager.getAllAlarms().forEach(a -> System.out.println("  " + a));

        System.out.println("\nDesactivando alarma 'Gym'...");
        manager.deactivateAlarm(alarm2.getId());
        System.out.println("Alarmas activas: " + manager.getActiveAlarmCount() + "/" + manager.getTotalAlarmCount());

        System.out.println("\nReactivando alarma 'Gym'...");
        manager.activateAlarm(alarm2.getId());
        System.out.println("Alarmas activas: " + manager.getActiveAlarmCount() + "/" + manager.getTotalAlarmCount());

        // ============================================================
        // TEST 2: Repetición semanal
        // ============================================================
        printSection("2. REPETICIÓN SEMANAL");

        alarm1.setRepeatMode(Alarm.RepeatMode.WEEKDAYS);
        alarm2.setRepeatMode(Alarm.RepeatMode.CUSTOM);
        alarm2.setCustomDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        alarm3.setRepeatMode(Alarm.RepeatMode.DAILY);

        System.out.println("alarm1 'Levantarse' - RepeatMode: " + alarm1.getRepeatMode());
        System.out.printf("  ¿Suena el lunes? %s%n", alarm1.shouldRingOn(DayOfWeek.MONDAY));
        System.out.printf("  ¿Suena el sábado? %s%n", alarm1.shouldRingOn(DayOfWeek.SATURDAY));

        System.out.println("alarm2 'Gym' - Días: " + alarm2.getCustomDays());
        System.out.printf("  ¿Suena el miércoles? %s%n", alarm2.shouldRingOn(DayOfWeek.WEDNESDAY));
        System.out.printf("  ¿Suena el domingo? %s%n", alarm2.shouldRingOn(DayOfWeek.SUNDAY));

        // ============================================================
        // TEST 3: Configurar sonido y volumen
        // ============================================================
        printSection("3. PERFILES DE SONIDO");

        SoundProfile profile1 = new SoundProfile("bosque_lluvia", SoundProfile.SoundType.NATURE, 60);
        profile1.setGradualVolume(true);
        profile1.setGradualDurationSeconds(120);
        alarm1.setSoundProfile(profile1);

        SoundProfile profile2 = new SoundProfile("rock_clasico", SoundProfile.SoundType.MUSIC, 80);
        alarm2.setSoundProfile(profile2);

        System.out.println("Perfil alarm1: " + alarm1.getSoundProfile());
        System.out.println("Perfil alarm2: " + alarm2.getSoundProfile());
        System.out.println("Volumen efectivo a los 60s (gradual): " + profile1.getEffectiveVolume(60) + "%");
        System.out.println("Volumen efectivo a los 120s (gradual): " + profile1.getEffectiveVolume(120) + "%");

        // ============================================================
        // TEST 4: Snooze
        // ============================================================
        printSection("4. SNOOZE");

        alarm1.setSnoozeDurationMinutes(5);
        alarm1.setMaxSnoozeCount(2);

        System.out.println("Intentando posponer 'Levantarse' (max=" + alarm1.getMaxSnoozeCount() + "):");
        System.out.println("  Snooze 1: " + snoozeManager.snooze(alarm1, LocalDateTime.now()));
        System.out.println("  Snooze 2: " + snoozeManager.snooze(alarm1, LocalDateTime.now()));
        System.out.println("  Snooze 3 (límite): " + snoozeManager.snooze(alarm1, LocalDateTime.now()));

        System.out.println("Desactivando snooze...");
        snoozeManager.dismiss(alarm1.getId());

        // ============================================================
        // TEST 5: Próximas alarmas
        // ============================================================
        printSection("5. PRÓXIMAS ALARMAS");
        System.out.println(scheduler.getScheduleSummary(5));

        // ============================================================
        // TEST 6: Detección de conflictos
        // ============================================================
        printSection("6. DETECCIÓN DE CONFLICTOS");

        Alarm conflictAlarm = manager.createAlarm("Alarma conflicto", LocalTime.of(7, 3));
        conflictAlarm.setRepeatMode(Alarm.RepeatMode.DAILY);

        List<String> conflicts = manager.detectConflicts();
        if (conflicts.isEmpty()) {
            System.out.println("No se detectaron conflictos.");
        } else {
            conflicts.forEach(System.out::println);
        }

        manager.removeAlarm(conflictAlarm.getId());
        System.out.println("Alarma de conflicto eliminada.");

        // ============================================================
        // TEST 7: Modo Vacaciones
        // ============================================================
        printSection("7. MODO VACACIONES");

        System.out.println("Alarmas activas antes: " + manager.getActiveAlarmCount());
        int deactivated = manager.activateVacationMode();
        System.out.println("Modo vacaciones activado. Alarmas desactivadas: " + deactivated);
        System.out.println("Alarmas activas durante vacaciones: " + manager.getActiveAlarmCount());

        int reactivated = manager.deactivateVacationMode();
        System.out.println("Modo vacaciones desactivado. Alarmas reactivadas: " + reactivated);
        System.out.println("Alarmas activas tras vacaciones: " + manager.getActiveAlarmCount());

        // ============================================================
        // TEST 8: Despertar Circadiano (avanzado)
        // ============================================================
        printSection("8. MODO CIRCADIANO (AVANZADO)");

        CircadianMode circadian = new CircadianMode(alarm1);
        circadian.activate();

        System.out.println(circadian.simulatePhase(35));
        System.out.println(circadian.simulatePhase(10));
        System.out.println(circadian.simulatePhase(0));

        // ============================================================
        // TEST 9: Reto Matemático (avanzado)
        // ============================================================
        printSection("9. RETO MATEMÁTICO (AVANZADO)");

        prefs.setMathChallengeEnabled(true);
        prefs.setMathChallengeDifficulty(Difficulty.MEDIUM);

        MathChallenge challenge = new MathChallenge(Difficulty.EASY);
        System.out.println("Reto fácil: " + challenge.getQuestion());
        System.out.println("  Intento incorrecto (99): " + challenge.attempt(99));

        MathChallenge challenge2 = new MathChallenge(Difficulty.MEDIUM);
        System.out.println("Reto medio: " + challenge2.getQuestion());

        MathChallenge challenge3 = new MathChallenge(Difficulty.HARD);
        System.out.println("Reto difícil: " + challenge3.getQuestion());

        // ============================================================
        // TEST 10: Alarma Geolocalizada (avanzado)
        // ============================================================
        printSection("10. ALARMA GEOLOCALIZADA (AVANZADO)");

        Alarm geoAlarmModel = manager.createAlarm("Trabajo - llegada", LocalTime.of(9, 0));
        geoAlarmModel.setRepeatMode(Alarm.RepeatMode.WEEKDAYS);

        // Madrid Puerta del Sol coordinates
        GeoAlarm geoAlarm = new GeoAlarm(geoAlarmModel, 40.4168, -3.7038, 200.0);
        System.out.println(geoAlarm);

        // Simulate being at location (same coords = 0m distance)
        System.out.print("Simulando posición EN el punto objetivo: ");
        System.out.println("¿Debería sonar? " + geoAlarm.shouldTrigger(40.4168, -3.7038));

        // Simulate being far away
        System.out.print("Simulando posición LEJOS (Barcelona): ");
        System.out.println("¿Debería sonar? " + geoAlarm.shouldTrigger(41.3851, 2.1734));

        // ============================================================
        // TEST 11: Perfil de sueño / estadísticas (avanzado)
        // ============================================================
        printSection("11. PERFIL DE SUEÑO (AVANZADO)");

        LocalDateTime scheduledWake = LocalDateTime.now().minusDays(1).withHour(7).withMinute(0);
        SleepRecord record1 = new SleepRecord(alarm1.getId(), scheduledWake);
        record1.recordWake(scheduledWake.plusMinutes(18), 2); // woke 18 min late, 2 snoozes

        SleepRecord record2 = new SleepRecord(alarm1.getId(), scheduledWake.plusDays(1));
        record2.recordWake(scheduledWake.plusDays(1).plusMinutes(1), 0); // on time

        SleepRecord record3 = new SleepRecord(alarm1.getId(), scheduledWake.plusDays(2));
        record3.recordWake(scheduledWake.plusDays(2).plusMinutes(9), 1); // 9 min late, 1 snooze

        stats.addRecord(record1);
        stats.addRecord(record2);
        stats.addRecord(record3);

        System.out.println(stats.getSummaryReport());

        // ============================================================
        // TEST 12: Categorías y filtrado
        // ============================================================
        printSection("12. ALARMAS POR CATEGORÍA");

        System.out.println("Alarmas de trabajo:");
        manager.getAlarmsByCategory(AlarmCategory.WORK)
                .forEach(a -> System.out.println("  " + a));

        System.out.println("\nAlarmas de deporte:");
        manager.getAlarmsByCategory(AlarmCategory.SPORT)
                .forEach(a -> System.out.println("  " + a));

        // ============================================================
        // TEST 13: Eliminar alarma
        // ============================================================
        printSection("13. ELIMINAR ALARMA");

        System.out.println("Total antes de eliminar: " + manager.getTotalAlarmCount());
        boolean removed = manager.removeAlarm(alarm3.getId());
        System.out.println("Eliminada 'Reunión': " + removed);
        System.out.println("Total después: " + manager.getTotalAlarmCount());

        // ============================================================
        // RESUMEN FINAL
        // ============================================================
        printSection("RESUMEN FINAL");
        System.out.println(manager);
        System.out.println(prefs);
        System.out.println("\n" + scheduler.getScheduleSummary(10));
    }

    private static void printSection(String title) {
        System.out.println("\n┌─────────────────────────────────────────");
        System.out.println("│ " + title);
        System.out.println("└─────────────────────────────────────────");
    }
}
