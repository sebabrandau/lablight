# Datos experimentales

Aquí se guardan los archivos CSV exportados desde la app (**Resultados → Compartir**).

Nombre sugerido: `AAAA-MM-DD_telefono_fuente_intentoN.csv`, por ejemplo `2026-11-12_motoG_linterna_1.csv`.

## Formato del CSV

Encabezado con los metadatos del experimento:

```
experimento,<nombre>
fecha,<dd/MM/aaaa HH:mm>
modo,SENSOR | SIMULACION
fuente,<fuente de luz>
fondo_lux,<luz de fondo>
prediccion,<respuesta del paso 2>
exponente_n,<n ajustado>
k,<constante del ajuste>
r2,<R²>
error_rel_n_pct,<error relativo de n respecto de 2, en %>
```

Luego, una fila por medición:

| Columna | Unidad | Significado |
|---|---|---|
| `distancia_cm` | cm | Distancia fuente–sensor |
| `lux_promedio` | lux | Promedio de ~40 lecturas en 2 s |
| `lux_desv_est` | lux | Desviación estándar de esas lecturas |
| `lux_corregido` | lux | Promedio − luz de fondo |
| `E_por_d2_lux_cm2` | lux·cm² | Producto E·d² (constante si se cumple la ley) |
| `lux_ideal_inv_cuadrado` | lux | Valor del modelo k/d² ajustado |
| `error_rel_pct` | % | Diferencia entre medido e ideal |
| `muestras` | — | Número de lecturas promediadas |

> Los datos generados en **modo simulación** llevan `modo,SIMULACION` y no deben presentarse como mediciones reales.

## Medición real · 16/11/2026 (Moto G34 5G, linterna de celular)

Archivo: `2026-11-16_motoG34_linterna_1.csv` · modo SENSOR · fondo no medido.

| d (cm) | 10 | 15 | 20 | 25 | 30 | 40 | 50 |
|---|---|---|---|---|---|---|---|
| E (lux) | 58,34 | 64,69 | 37,93 | 25,45 | 16,32 | 7,34 | 1,97 |

**Análisis:**
- Ajuste con todos los puntos: n = 2,06, R² = 0,846, error relativo 3,0 % respecto de n = 2.
- Tramo 15–30 cm: n = 1,96, R² = 0,996, error 2,2 %; E·d² ≈ 15 080 ± 611 lux·cm² (4,1 %), casi constante, como predice la ley.
- El punto de 10 cm es anómalo: a esa distancia la fuente ya no se comporta como puntual y el sensor no queda bien alineado.
- El punto de 50 cm es anómalo porque la señal es muy débil (cerca de la luz de fondo, que no se midió).
- Repetibilidad a 20 cm (serie aparte, 5 repeticiones): 136,7 lux, s = 3,6 lux (2,7 %).

**Conclusión:** en el rango donde el montaje es válido (15–30 cm), los datos confirman la ley del inverso del cuadrado con un error menor al 3 %.
