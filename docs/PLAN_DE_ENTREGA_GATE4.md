# Plan de entrega · Gate 4 (MVP integrado)

**Entrega:** martes 17 de noviembre de 2026, 23:59 · Un único PDF (Informe Técnico de Avance) subido por una persona.
En la **primera página** del PDF van: link a la interfaz activa (el APK en *Releases*) y link al repositorio.

> ⚠️ Lo que más baja nota: link roto o repositorio privado · commits de una sola persona · app que solo funciona con ayuda del equipo · guía que no coincide con las pantallas · gráficos sin unidades.

---

## Calendario sugerido

| Día | Qué | Quién |
|---|---|---|
| Mar 10 – Mié 11 | Crear cuentas y repositorio público, invitar colaboradores | Sebastián + todos |
| Mié 11 | Abrir la app nueva en Android Studio, cambiar `repo_url`, probar en el celular | Sebastián |
| Mié 11 – Jue 12 | Subir cada parte con su propia cuenta (commits por rol) | Cada una/o |
| Jue 12 | **Medición real** completa (fondo + 7 distancias + 5 repeticiones a 20 cm), capturas de todas las pantallas, CSV a `datos/` | Todo el equipo, en sala oscura |
| Vie 13 | Prueba con 1–2 personas externas (que lo hagan solas, solo observar) | Catalina |
| Vie 13 | Generar APK y publicar *Release v0.2.0* | Sebastián / Martina |
| Sáb 14 – Lun 16 | Completar el informe con capturas y links; revisión cruzada | Martina + todos |
| Mar 17 | Exportar a PDF, **probar los links en modo incógnito**, subir | Martina |

---

## Paso 0 · Repositorio (Sebastián, 10 min)

1. Cada integrante crea su cuenta en <https://github.com> (con su correo).
2. Sebastián: **New repository** → nombre `LabLight` → **Public** → marcar *Add a README* (se reemplaza después) → *Add .gitignore: Android* → **Create**.
3. **Settings → Collaborators → Add people** → agregar a Catalina, Martina y Valentina. Cada una acepta la invitación que le llega por correo.

## Paso 1 · Cada integrante sube su parte con su propia cuenta

El paquete `LabLight_por_integrante` tiene una carpeta por persona, con los archivos ya ubicados en las rutas correctas.

**Cómo subir (sin instalar nada):** entrar al repositorio con tu cuenta → **Add file → Upload files** → arrastrar **el contenido** de tu carpeta (las carpetas `app`, `hardware`, `docs`, etc., no la carpeta con tu nombre) → escribir el mensaje del commit → **Commit changes**. GitHub junta las carpetas con lo que ya existe.

Hazlo en **dos subidas** (dos commits), como indica la tabla, y **revisa y entiende** los archivos de tu área: en la defensa pueden preguntarle a cualquiera.

| Orden | Integrante | Subida 1 (mensaje del commit) | Subida 2 (mensaje del commit) |
|---|---|---|---|
| 1 | **Sebastián** (software/UX) | `app/src/main/res` + `AndroidManifest.xml` + archivos de Gradle → *"Interfaz según prototipo Figma: pantallas, estilos e íconos"* | `app/src/main/java/...` (pantallas, `Experiment.kt`, `ExperimentRepository.kt`, `Ui.kt`) → *"Flujo de 4 pasos, guardado de experimentos y navegación"* |
| 2 | **Valentina** (hardware) | `hardware/` → *"Montaje experimental, caracterización del sensor y plan B BH1750"* | `LuxSource.kt` → *"Instrumentación: sensor de luz y modo simulación"* |
| 3 | **Catalina** (ciencia/pedagogía) | `Analysis.kt` + `app/src/test/` → *"Motor de datos: ajuste E = k/d^n, repetibilidad y pruebas"* | `LearnActivity.kt` + `docs/guia_estudiante/` + `datos/README.md` → *"Contenidos de Aprender y guía del estudiante"* |
| 4 | **Martina** (PO) | `README.md` → *"README: instalación, uso, arquitectura y roles"* | `docs/` (informe, diagramas, bitácora) → *"Informe técnico Gate 4 y diagramas"* |

Después de la medición real (jueves): **Valentina** completa `hardware/caracterizacion_sensor.md` con los datos de sus teléfonos y **Catalina** sube los CSV a `datos/`. Son commits con trabajo propio y suman en el gráfico de contribuciones.

> Sebastián, si prefieres, usa **GitHub Desktop** (clonar → copiar archivos → *Commit* → *Push*). Lo importante es que cada persona haga sus commits con su propia cuenta.

## Paso 2 · App lista para entregar (Sebastián)

1. Abrir la carpeta `LabLight_completo` en Android Studio (si sale el error de Java, elegir Gradle JDK 21 como la vez anterior).
2. En `app/src/main/res/values/strings.xml`, cambiar `https://github.com/sebabrandau/lablight` por la URL real del repositorio.
3. Probar en el celular el recorrido completo: Bienvenida → Nuevo experimento → pasos 1 a 4 → Mis experimentos → Aprender → Proyecto. Probar también el **modo Simulación**.
4. Si Android Studio muestra un error, copiar el texto completo y pedir ayuda antes de cambiar cosas.

## Paso 3 · Link a la interfaz activa (Release con el APK)

1. Android Studio: **Build → Generate App Bundles or APKs → Generate APKs** → *locate* → renombrar `app-debug.apk` a `Bragolito-v0.2.0.apk`.
2. GitHub → **Releases → Draft a new release** → *Choose a tag*: `v0.2.0` → título *"Bragolito v0.2.0 · Gate 4 MVP integrado"* → arrastrar el APK → **Publish release**.
3. Links para la portada del informe:
   - Interfaz activa (APK): `https://github.com/sebabrandau/lablight/releases/latest`
   - Repositorio y bitácora: `https://github.com/sebabrandau/lablight`
4. **Probar ambos links en una ventana de incógnito** (sin sesión iniciada). Si piden login, el repositorio está privado: *Settings → General → Danger Zone → Change visibility → Public*.

## Paso 4 · Evidencias para el informe

- **Capturas de pantalla** (en el celular, con datos REALES, no simulados): Bienvenida, Inicio, Paso 1, Paso 2, Paso 3 midiendo, Paso 4 (exponente + gráfica lineal + gráfica logarítmica + tabla), Mis experimentos, Aprender. Guardarlas en `docs/capturas/`.
- **Gráfico de contribuciones:** repositorio → **Insights → Contributors** → captura.
- **Estructura de carpetas:** captura de la página principal del repositorio.
- **README:** captura de cómo se ve en GitHub.

## Paso 5 · Informe

Abrir `Informe_Tecnico_Gate4_LabLight.docx`, reemplazar todo lo marcado en **amarillo** (links, capturas, resultados reales), revisar y **Archivo → Guardar como → PDF**.
