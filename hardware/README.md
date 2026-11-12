# Hardware e instrumentación

Responsable: **Valentina Torres** (hardware / instrumentación).

## Instrumento principal: sensor de luz ambiental (ALS) del celular

- Es un fotodiodo bajo la pantalla, normalmente junto a la cámara frontal.
- La app lo lee con la API de sensores de Android (`Sensor.TYPE_LIGHT`) y entrega la **iluminancia en lux**.
- El rango máximo y la resolución dependen de cada modelo de teléfono; la app los muestra en el **Paso 1 · Configuración**.
- Es un sensor *on-change*: solo envía un dato nuevo cuando la luz cambia. Por eso la app toma una muestra cada 50 ms (20 Hz) durante 2 s y calcula promedio y desviación estándar.

La caracterización de los teléfonos usados por el equipo está en [`caracterizacion_sensor.md`](caracterizacion_sensor.md).

## Montaje experimental recomendado

| Elemento | Detalle |
|---|---|
| Fuente | Linterna LED de otro celular (pequeña, casi puntual) con batería cargada |
| Escala | Huincha o regla de 50–100 cm, marcas cada 5 cm |
| Montaje A | Celular acostado, pantalla hacia arriba; fuente arriba apuntando hacia abajo |
| Montaje B | Celular parado (apoyado en libros) mirando a la fuente; la fuente se mueve sobre la regla |
| Ambiente | Sala oscura, superficies opacas, nadie entre la fuente y el sensor |

## Plan B: BH1750 + ESP32

Si un teléfono no tiene sensor de luz (o para comparar con un sensor de referencia), se usa un **BH1750** (sensor digital I2C, rango 1–65 535 lux, resolución 1 lux).

- Código: [`bh1750_esp32/bh1750_esp32.ino`](bh1750_esp32/bh1750_esp32.ino) (sin librerías externas).
- Salida: CSV por el Monitor Serie (`tiempo_ms,lux`), compatible con la planilla de la app.
- La app también incluye un **modo simulación**, que permite completar la experiencia sin ningún sensor.

## Seguridad

- Solo fuentes de baja potencia que no se calienten. No conectar nada a la red eléctrica de forma improvisada.
- El ESP32/Arduino se alimenta solo por USB (5 V).
