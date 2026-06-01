# SmartAlarm System

Sistema de alarmas inteligente implementado en Java puro, sin interfaz gráfica. Inspirado en los despertadores de smartphone modernos.

---

## Descripción del proyecto

SmartAlarm es una librería de lógica de negocio para gestión de alarmas. Permite crear, configurar y gestionar alarmas con comportamiento avanzado: repetición semanal, snooze con límites, modo circadiano, retos matemáticos, alarmas geolocalizadas y seguimiento de estadísticas de sueño.

No existe GUI. Toda la demostración de funcionamiento se realiza a través de `Main.java`.

---

## Objetivos

- Modelar un sistema real con diseño orientado a objetos limpio y mantenible
- Separar responsabilidades mediante clases cohesivas y desacopladas
- Implementar funcionalidades avanzadas de forma extensible
- Documentar el proceso de desarrollo incluyendo el uso de IA

---

## Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje principal |
| `java.time` API | Manejo de fechas, horas y duraciones |
| `java.util` | Colecciones, UUID, Optional |
| Claude (Anthropic) | Asistencia en diseño y generación de código |
| Git / GitHub | Control de versiones |

---

## Instalación y ejecución

### Requisitos

- Java 21+
- (Opcional) Maven o Gradle para proyectos más grandes

### Compilar

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
```

### Ejecutar

```bash
java -cp out com.smartalarm.Main
```

---

## Estructura del proyecto

```
smart-alarm/
├── src/
│   ├── main/java/com/smartalarm/
│   │   ├── Main.java                    # Punto de entrada y pruebas
│   │   ├── model/
│   │   │   ├── Alarm.java               # Entidad principal de alarma
│   │   │   ├── AlarmCategory.java       # Enum de categorías
│   │   │   ├── SoundProfile.java        # Configuración de sonido
│   │   │   ├── SnoozeSession.java       # Estado de una sesión de snooze
│   │   │   ├── SleepRecord.java         # Registro de una sesión de sueño
│   │   │   ├── MathChallenge.java       # Reto matemático para desactivar
│   │   │   └── UserPreferences.java     # Preferencias globales del usuario
│   │   ├── manager/
│   │   │   ├── AlarmManager.java        # CRUD y consultas de alarmas
│   │   │   ├── SnoozeManager.java       # Gestión de sesiones de snooze
│   │   │   └── Scheduler.java          # Lógica de temporización
│   │   ├── mode/
│   │   │   ├── CircadianMode.java       # Modo despertar progresivo
│   │   │   └── GeoAlarm.java           # Alarmas geolocalizadas (simuladas)
│   │   └── stats/
│   │       └── SleepStatistics.java     # Estadísticas y perfil de sueño
│   └── test/java/com/smartalarm/       # Tests (directorio preparado)
├── docs/
│   ├── class-diagram.md                # Diagrama UML de clases
│   └── use-cases.md                    # Especificación de casos de uso
└── README.md
```

---

## Diseño orientado a objetos

### Clases y responsabilidades

#### Capa `model` — Entidades del dominio

**`Alarm`**  
Entidad central. Encapsula toda la configuración de una alarma: hora, etiqueta, modo de repetición, perfil de sonido, snooze y categoría. Contiene la lógica de `getNextTriggerTime()` y `shouldRingOn()` para calcular cuándo debe dispararse.

**`SoundProfile`**  
Separada de `Alarm` para respetar el principio de responsabilidad única. Gestiona tipo de sonido, volumen, y la lógica de rampa gradual (`getEffectiveVolume`).

**`SnoozeSession`**  
Modela el estado de un snooze activo: cuántas veces se ha pospuesto, hasta cuándo, y si se ha agotado el límite. Desacoplada del manager para facilitar testing.

**`SleepRecord`**  
Registro inmutable de una sesión de sueño: hora programada, hora real, aplazamientos. Calcula automáticamente si el usuario fue puntual.

**`MathChallenge`**  
Genera y valida retos matemáticos de tres niveles. La respuesta correcta es privada; solo se accede mediante `attempt()` (encapsulación).

**`UserPreferences`**  
Preferencias globales que aplican a todas las alarmas por defecto. Separada de `Alarm` para no mezclar configuración individual con configuración de usuario.

**`AlarmCategory`**  
Enum que permite clasificar alarmas y filtrarlas (trabajo, deporte, medicina…).

#### Capa `manager` — Lógica de negocio

**`AlarmManager`**  
Repositorio y gestor de alarmas. Centraliza CRUD, activación/desactivación, detección de conflictos, modo vacaciones y consultas. Usa `Map<String, Alarm>` para acceso O(1) por ID.

**`SnoozeManager`**  
Gestiona sesiones de snooze activas. Delegada de `AlarmManager` para no sobrecargar su responsabilidad. Mantiene un mapa de sesiones activas indexado por ID de alarma.

**`Scheduler`**  
Determina qué alarmas deben dispararse en un instante dado. Permite simulación temporal pasando un `LocalDateTime` arbitrario, lo que facilita testing.

#### Capa `mode` — Funcionalidades avanzadas

**`CircadianMode`**  
Configura una alarma para despertar progresivo: activa sonidos de naturaleza y volumen gradual. Simula fases (preparación, rampa, alarma completa) según minutos restantes.

**`GeoAlarm`**  
Envuelve una alarma con condición de geolocalización. Usa la fórmula de Haversine para calcular distancia real entre coordenadas.

#### Capa `stats`

**`SleepStatistics`**  
Agrega `SleepRecord`s y calcula métricas: total de aplazamientos, puntualidad, retraso medio. Genera un informe textual del perfil de sueño.

---

## Diagramas UML

Ver [`docs/class-diagram.md`](docs/class-diagram.md) y [`docs/use-cases.md`](docs/use-cases.md).

---

## Funcionalidades avanzadas implementadas

1. **Despertar circadiano** (`CircadianMode`) — activación progresiva con sonidos de naturaleza y rampa de volumen
2. **Reto matemático** (`MathChallenge`) — la alarma no puede cerrarse sin resolver una operación (3 niveles de dificultad)
3. **Alarmas geolocalizadas** (`GeoAlarm`) — solo suenan si el usuario está en un radio definido (Haversine simulado)
4. **Perfil de sueño** (`SleepStatistics`) — estadísticas de puntualidad, aplazamientos y retrasos
5. **Modo vacaciones** (`AlarmManager.activateVacationMode`) — desactivación masiva temporal
6. **Detección de conflictos** (`AlarmManager.detectConflicts`) — detecta alarmas a menos de 5 minutos entre sí

---

## Reflexión técnica

### Decisiones de diseño

- **Separación en capas**: `model` / `manager` / `mode` / `stats` permite evolucionar cada capa independientemente.
- **`LocalTime` + `LocalDateTime`**: Se usa `LocalTime` para la hora configurada y `LocalDateTime` para calcular la siguiente disparo, evitando confusiones con fecha vs hora.
- **ID basado en UUID**: Garantiza unicidad sin necesidad de base de datos o secuencia global.
- **`getNextTriggerTime` en `Alarm`**: La lógica de cuándo suena es responsabilidad de la propia alarma, no del manager. El manager delega en ella.
- **`SnoozeSession` separada de `Alarm`**: Una alarma puede no estar en snooze en absoluto; crear este objeto solo cuando sea necesario evita estado innecesario.

### Patrones aplicados

- **Repository pattern** — `AlarmManager` actúa como repositorio en memoria
- **Factory method implícito** — `AlarmManager.createAlarm()` centraliza la creación
- **Encapsulación estricta** — atributos privados, setters con validación, respuesta de `MathChallenge` inaccesible directamente

### Problemas encontrados

- La duración del modo circadiano (15 min = 900s) excedía el límite de 300s del validador de `SoundProfile`. Se corrigió con `Math.min()` para respetar la restricción y documentar el límite.
- El cálculo de `getNextTriggerTime` para alarmas `NONE` (una sola vez) requirió tratamiento especial ya que no tienen un día de semana asociado.

### Deuda técnica

- Los tests unitarios están preparados en directorio pero no implementados (requieren JUnit 5)
- `Scheduler.isTriggering` usa ventana de 60s fija; debería ser configurable
- `GeoAlarm` no tiene persistencia de sesiones simuladas

### Mejoras futuras

- Exportar/importar configuración de alarmas en JSON
- Notificaciones push simuladas
- Integración con calendario (Google Calendar API)
- Test suite completa con JUnit 5 + Mockito

---

## Reflexión sobre IA

### Herramientas utilizadas

**Claude (Anthropic) — claude-sonnet-4-6**

### Uso y prompts reales

| Tarea | Prompt utilizado |
|---|---|
| Estructura inicial | "Diseña la arquitectura de clases para un sistema de alarmas inteligente en Java sin GUI. Explica responsabilidades y relaciones." |
| Clase `Alarm` | "Implementa la clase Alarm con todos sus atributos, getters validados, modo de repetición semanal y getNextTriggerTime" |
| Haversine en GeoAlarm | "Implementa la fórmula de Haversine en Java para calcular distancia entre dos coordenadas lat/lon" |
| Diagrama Mermaid | "Genera el diagrama de clases UML en Mermaid para este diseño" |

### Qué generó la IA vs qué se modificó

| Código | Origen |
|---|---|
| Estructura de paquetes | IA propuso, se adoptó íntegra |
| `Alarm.java` | IA generó borrador, se revisó validación de `getNextTriggerTime` |
| `haversineDistance` | IA generó, verificada matemáticamente |
| `Main.java` | Escrito manualmente como prueba de integración |
| Bug del límite de 300s en CircadianMode | Detectado en ejecución, corregido manualmente |

### Ventajas, limitaciones y validación

**Ventajas**: La IA acelera enormemente el scaffolding de clases repetitivas (getters, validación, toString). El código generado es estructuralmente correcto y sigue convenciones Java.

**Limitaciones**: La IA no detectó el conflicto entre la duración circadiana (900s) y el límite del validador de `SoundProfile` (300s). También tendió a sugerir arquitecturas más complejas de lo necesario para el scope del proyecto.

**Validación manual**: Todo el código fue compilado y ejecutado localmente. Se verificó la salida de cada sección de `Main.java` para confirmar comportamiento correcto. La fórmula Haversine se validó comparando con calculadoras online (Madrid-Barcelona ≈ 505 km ✓).

**Errores detectados en código de IA**: El bug de los 900s fue el único error de lógica. El resto requirió ajustes menores de estilo y coherencia con el resto del diseño.

---

## Autoevaluación

| Criterio | Puntuación | Justificación |
|---|---|---|
| Funcionalidades mínimas | 10/10 | Todas implementadas y verificadas |
| Funcionalidades avanzadas (mínimo 3) | 10/10 | 6 avanzadas implementadas |
| Diseño OO / SOLID | 9/10 | Buena separación; podría mejorarse con interfaces |
| Uso de IA documentado | 10/10 | Prompts reales, errores detectados, reflexión crítica |
| Calidad del código | 9/10 | Nombres claros, encapsulación, sin duplicidad |
| Documentación README | 10/10 | Completa con todas las secciones requeridas |
| Diagramas UML | 9/10 | Clase y casos de uso con explicación razonada |
| Git / GitHub | Pendiente | Requiere crear repo y hacer commits |
