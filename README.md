# LabLight · Bragolito

**Laboratorio en el bolsillo para verificar la ley del inverso del cuadrado de la luz con el sensor de luminosidad del celular.**

App Android para estudiantes de 3° y 4° medio (Física, formación diferenciada). El estudiante configura el experimento, hace una predicción, mide la iluminancia a distintas distancias de una fuente de luz y obtiene el exponente *n* del modelo **E = k / dⁿ**, que compara con el valor teórico **n = 2**.

Proyecto de Innovación Tecnológica · Universidad Mayor · 2026 · Prof. Ana G. E. Jorquera F.

| | |
|---|---|
| **Versión** | 0.2.0 (Gate 4 · MVP integrado) |
| **Plataforma** | Android 8.0 o superior (iOS no expone el sensor de luz) |
| **Descargar la app** | Sección [**Releases**](../../releases/latest) → `Bragolito-v0.2.0.apk` |
| **Lenguaje** | Kotlin · Android Studio · MPAndroidChart |

---

## Equipo y roles

| Integrante | Rol | Área principal en el repositorio |
|---|---|---|
| Martina Lineros | Product Owner / Líder de proyecto | `README.md`, `docs/` (informes), releases |
| Catalina González | Responsable científica y pedagógica | `Analysis.kt`, pruebas, `docs/guia_estudiante/`, contenidos de *Aprender* |
| Sebastián Brandau | Responsable de software/datos y UX/UI | Pantallas (`*Activity.kt`, `res/layout/`), guardado de datos |
| Valentina Torres | Responsable de hardware e instrumentación | `LuxSource.kt`, `hardware/` (montaje, plan B, caracterización) |

---

## Instalación (usuario)

1. Abre la sección **Releases** de este repositorio desde el celular y descarga `Bragolito-v0.2.0.apk`.
2. Ábrelo. Android pedirá permitir **"instalar apps de fuentes desconocidas"** → *Configuración → Permitir desde esta fuente* → vuelve atrás → **Instalar**.
3. Si aparece un aviso de *Play Protect*, toca **Más detalles → Instalar de todas formas** (sale porque la app no viene de Play Store).
4. Abre **Bragolito**.

La app no necesita internet ni permisos especiales: solo lee el sensor de luz.

## Uso (resumen)

Materiales: celular con la app, una fuente de luz pequeña (linterna de otro celular), una regla o huincha y una sala oscura.

1. **Bienvenida → Comenzar → Nuevo experimento.**
2. **Paso 1 · Configuración:** nombre, fuente de luz y modo (*Sensor del celular* o *Simulación*). Revisa que la lectura en vivo cambie al tapar la parte superior de la pantalla.
3. **Paso 2 · Predicción:** ¿qué pasa con la intensidad si duplicas la distancia?
4. **Paso 3 · Medición:** mide la luz de fondo con la fuente apagada; luego guarda una medición a 10, 15, 20, 25, 30, 40 y 50 cm (botón +5). Repite 5 veces a 20 cm. Sigue los avisos rojos si aparecen (sensor saturado / luz muy débil).
5. **Paso 4 · Resultados:** exponente *n*, R², error relativo, gráfica con unidades (lineal y logarítmica), tabla con E·d², comparación con tu predicción y repetibilidad. **Compartir** envía los datos en CSV.

Todo queda guardado en **Mis experimentos**. La sección **Aprender** explica los conceptos y el uso paso a paso. La guía completa está en [`docs/guia_estudiante/`](docs/guia_estudiante/).

---

## Compilar desde el código (desarrolladores)

1. Instala [Android Studio](https://developer.android.com/studio).
2. *File → Open* → selecciona esta carpeta (la que contiene `settings.gradle.kts`) → *Trust Project*.
3. **Importante:** el proyecto usa Gradle 8.7, que funciona hasta Java 21. Si aparece *"Incompatible Gradle JVM version"*: *File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK → Download JDK… → versión 21, JetBrains Runtime* → OK → Sync.
4. No aceptes el "AGP Upgrade Assistant" ni actualizar Kotlin.
5. Conecta un celular con *Depuración USB* activada y presiona **▶ Run**.
6. Para generar el instalador: *Build → Generate App Bundles or APKs → Generate APKs*. Queda en `app/build/outputs/apk/debug/app-debug.apk`.

**Pruebas automáticas** del motor de datos: clic derecho sobre `app/src/test/java/cl/umayor/lablight/AnalysisTest.kt` → *Run 'AnalysisTest'* (9 pruebas).

---

## Arquitectura

```
Instrumentación              Motor de datos                    Interfaz
──────────────────           ─────────────────────────         ──────────────────────────
Sensor ALS del celular  ┐    Muestreo 20 Hz · ventana 2 s      Bienvenida · Inicio
  (SensorLuxSource)     ├──▶ Promedio y desviación estándar ──▶ Pasos 1-2-3-4
Simulación 1/d² + ruido ┘    Corrección por luz de fondo        Gráfica · Tabla · Avisos
  (SimulatedLuxSource)       Ajuste E = k/dⁿ (regresión ln-ln)  Mis experimentos · Aprender
                             Error relativo · repetibilidad     Compartir CSV
                             Guardado JSON (ExperimentRepository)
```

Diagramas: [`docs/diagramas/`](docs/diagramas/).

## Estructura del repositorio

```
LabLight/
├── README.md                      ← este archivo
├── app/                           ← aplicación Android
│   └── src/
│       ├── main/java/cl/umayor/lablight/
│       │   ├── LuxSource.kt            Instrumentación: sensor real y simulación
│       │   ├── Analysis.kt             Motor de datos: estadística y ajuste E = k/dⁿ
│       │   ├── Experiment.kt           Modelo de datos (experimento, mediciones, predicción)
│       │   ├── ExperimentRepository.kt Guardado en el teléfono (JSON)
│       │   ├── WelcomeActivity.kt      Bienvenida
│       │   ├── HomeActivity.kt         Inicio (menú)
│       │   ├── SetupActivity.kt        Paso 1 · Configuración
│       │   ├── PredictionActivity.kt   Paso 2 · Predicción
│       │   ├── MeasureActivity.kt      Paso 3 · Medición
│       │   ├── ResultsActivity.kt      Paso 4 · Resultados
│       │   ├── HistoryActivity.kt      Mis experimentos
│       │   ├── LearnActivity.kt        Aprender
│       │   ├── AboutActivity.kt        Información del proyecto
│       │   └── Ui.kt                   Barra superior, indicador de pasos, navegación
│       ├── main/res/                   Pantallas (layout), textos, colores e íconos
│       └── test/.../AnalysisTest.kt    Pruebas automáticas
├── hardware/                      ← montaje, caracterización del sensor, plan B (BH1750 + ESP32)
├── datos/                         ← CSV exportados desde la app
└── docs/                          ← informes, guía del estudiante, diagramas, capturas
```

## Historial de versiones

| Versión | Gate | Cambios |
|---|---|---|
| 0.1.0 | Gate 3 · Factibilidad | Lectura del sensor, medición por distancia, ajuste y gráfico |
| 0.2.0 | Gate 4 · MVP integrado | Interfaz según prototipo Figma (bienvenida, inicio, pasos 1-4, aprender, información), predicción, avisos de saturación/luz débil, guardado de experimentos, modo simulación, unidades en gráficos, repetibilidad, E·d² |

## Limitaciones conocidas

- Solo Android; el rango y la resolución del sensor dependen del modelo de teléfono.
- A menos de ~8 cm la fuente no se comporta como puntual y el modelo 1/d² pierde validez.
- Los datos del modo simulación son generados por el modelo y se marcan como tales.
