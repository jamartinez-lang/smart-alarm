# Diagrama de Clases UML — SmartAlarm

## Diagrama

```mermaid
classDiagram
    direction TB

    class Alarm {
        -String id
        -String label
        -LocalTime time
        -boolean active
        -RepeatMode repeatMode
        -Set~DayOfWeek~ customDays
        -SoundProfile soundProfile
        -int snoozeDurationMinutes
        -int maxSnoozeCount
        -AlarmCategory category
        -boolean circadianMode
        -String location
        +shouldRingOn(DayOfWeek) boolean
        +getNextTriggerTime(LocalDateTime) LocalDateTime
        +setActive(boolean)
        +setRepeatMode(RepeatMode)
        +setCustomDays(Set~DayOfWeek~)
    }

    class RepeatMode {
        <<enumeration>>
        NONE
        DAILY
        WEEKDAYS
        WEEKENDS
        CUSTOM
    }

    class AlarmCategory {
        <<enumeration>>
        GENERAL
        WORK
        STUDY
        SPORT
        MEDICINE
        PERSONAL
        VACATION
        +getDisplayName() String
    }

    class SoundProfile {
        -String soundName
        -SoundType soundType
        -int volume
        -boolean gradualVolume
        -int gradualDurationSeconds
        +getEffectiveVolume(int) int
        +setVolume(int)
        +setGradualVolume(boolean)
    }

    class SoundType {
        <<enumeration>>
        BEEP
        MUSIC
        NATURE
        GRADUAL
        SILENT
    }

    class SnoozeSession {
        -String alarmId
        -int snoozeCount
        -LocalDateTime snoozeUntil
        -int maxSnoozeCount
        -int snoozeDurationMinutes
        +snooze(LocalDateTime) boolean
        +isSnoozed(LocalDateTime) boolean
        +hasExhaustedSnooze() boolean
    }

    class SleepRecord {
        -String alarmId
        -LocalDate date
        -LocalDateTime scheduledWakeTime
        -LocalDateTime actualWakeTime
        -int snoozeCount
        -boolean dismissedOnTime
        +recordWake(LocalDateTime, int)
        +getSleepDelay() Duration
    }

    class MathChallenge {
        -Difficulty difficulty
        -String question
        -int answer
        -boolean solved
        +attempt(int) boolean
        +getQuestion() String
        +isSolved() boolean
    }

    class Difficulty {
        <<enumeration>>
        EASY
        MEDIUM
        HARD
    }

    class UserPreferences {
        -int defaultSnoozeDurationMinutes
        -int defaultMaxSnoozeCount
        -int defaultVolume
        -SoundType defaultSoundType
        -boolean mathChallengeEnabled
        -Difficulty mathChallengeDifficulty
        -boolean vacationMode
        -boolean conflictDetectionEnabled
    }

    class AlarmManager {
        -Map~String,Alarm~ alarms
        +addAlarm(Alarm) Alarm
        +createAlarm(String, LocalTime) Alarm
        +removeAlarm(String) boolean
        +getAlarm(String) Optional~Alarm~
        +getAllAlarms() List~Alarm~
        +activateAlarm(String)
        +deactivateAlarm(String)
        +getUpcomingAlarms(int) List~Alarm~
        +getAlarmsByCategory(AlarmCategory) List~Alarm~
        +detectConflicts() List~String~
        +activateVacationMode() int
        +deactivateVacationMode() int
    }

    class SnoozeManager {
        -Map~String,SnoozeSession~ activeSessions
        +snooze(Alarm) boolean
        +snooze(Alarm, LocalDateTime) boolean
        +isSnoozed(String) boolean
        +dismiss(String)
        +getSnoozeCount(String) int
    }

    class Scheduler {
        -AlarmManager alarmManager
        +getTriggeredAlarms() List~Alarm~
        +getTriggeredAlarms(LocalDateTime) List~Alarm~
        +isTriggering(Alarm, LocalDateTime) boolean
        +getScheduleSummary(int) String
        +simulate(LocalDateTime) List~Alarm~
    }

    class CircadianMode {
        -Alarm alarm
        -Phase currentPhase
        -boolean active
        +activate()
        +deactivate()
        +getCurrentPhase(int) Phase
        +simulatePhase(int) String
    }

    class Phase {
        <<enumeration>>
        LIGHT_SLEEP_PREP
        GENTLE_RAMP
        FULL_ALARM
    }

    class GeoAlarm {
        -Alarm alarm
        -double targetLatitude
        -double targetLongitude
        -double radiusMeters
        -boolean enabled
        +shouldTrigger(double, double) boolean
        -haversineDistance(double, double, double, double) double
    }

    class SleepStatistics {
        -List~SleepRecord~ records
        +addRecord(SleepRecord)
        +getTotalSnoozeCount() int
        +getAverageSnoozeCount() double
        +getPunctualityRate() double
        +getAverageDelayMinutes() double
        +getSummaryReport() String
    }

    %% Relaciones
    Alarm "1" --> "1" SoundProfile : tiene
    Alarm "1" --> "1" AlarmCategory : clasificada por
    Alarm "1" --> "1" RepeatMode : modo repetición
    SoundProfile --> SoundType : usa

    AlarmManager "1" o-- "0..*" Alarm : gestiona
    SnoozeManager "1" o-- "0..*" SnoozeSession : mantiene
    SnoozeSession --> Alarm : referencia por id
    Scheduler --> AlarmManager : usa

    CircadianMode --> Alarm : configura
    CircadianMode --> Phase : estado
    GeoAlarm --> Alarm : envuelve

    SleepStatistics "1" o-- "0..*" SleepRecord : agrega
    SleepRecord --> Alarm : referencia por id

    MathChallenge --> Difficulty : nivel
    UserPreferences --> SoundType : default
    UserPreferences --> Difficulty : dificultad reto
```

---

## Justificación del diseño

### ¿Por qué existe `SoundProfile` como clase separada?

Si el sonido estuviera dentro de `Alarm`, la clase tendría demasiadas responsabilidades. `SoundProfile` puede evolucionar de forma independiente (añadir ecualizador, playlists, fuente de streaming) sin tocar `Alarm`. Además permite reutilizar el mismo perfil en múltiples alarmas si se desea.

### ¿Por qué `SnoozeSession` está separada de `Alarm`?

Una alarma no siempre está siendo pospuesta. Crear el objeto `SnoozeSession` solo cuando hace falta evita tener estado nulo (`snoozeCount = 0`, `snoozeUntil = null`) en todas las alarmas siempre. El `SnoozeManager` crea y destruye sesiones dinámicamente.

### ¿Por qué `AlarmManager` usa `Map<String, Alarm>`?

El acceso por ID es la operación más frecuente (activar, desactivar, buscar). `LinkedHashMap` garantiza O(1) para acceso por ID y mantiene el orden de inserción para listados predecibles.

### ¿Por qué `Scheduler` está separado de `AlarmManager`?

`AlarmManager` conoce el *estado* de las alarmas. `Scheduler` conoce el *tiempo*. Son responsabilidades diferentes. Esta separación permite sustituir el `Scheduler` por uno basado en `Timer`, `ScheduledExecutorService` o similar sin tocar `AlarmManager`.

### ¿Por qué `CircadianMode` y `GeoAlarm` son clases independientes y no flags en `Alarm`?

Son *decoradores de comportamiento*. Añadir sus atributos directamente en `Alarm` inflaría la clase con campos que la mayoría de alarmas nunca usan. Como clases separadas, el patrón es extensible: mañana podría añadirse `WeatherAlarm` o `CalendarAlarm` sin modificar `Alarm`.

### ¿Por qué `SleepRecord` es inmutable (salvo `recordWake`)?

Los registros de sueño representan hechos pasados. Una vez registrado el despertar, el registro no debe cambiar. La inmutabilidad parcial (solo se escribe una vez via `recordWake`) protege la integridad histórica.

### Encapsulación de `MathChallenge`

La respuesta (`answer`) es privada y no tiene getter. La única forma de interactuar es mediante `attempt(int)`. Esto impide que código cliente haga trampa comparando la respuesta directamente.

### Visibilidad

- Todos los campos son `private`
- Los getters exponen copias defensivas donde aplica (`getCustomDays()` devuelve `EnumSet.copyOf`)
- Los setters validan los invariantes del dominio (volumen 0-100, duración snooze 1-60, etc.)
- Métodos internos de cálculo (`haversineDistance`) son `private`
