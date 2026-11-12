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
