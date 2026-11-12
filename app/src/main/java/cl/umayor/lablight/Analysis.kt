package cl.umayor.lablight

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Resultado de ajustar los datos al modelo E = k / dⁿ.
 *
 * @property exponentN el exponente n medido (la teoría dice n = 2).
 * @property k la constante del modelo, en lux·cmⁿ.
 * @property r2 coeficiente de determinación del ajuste en escala log-log (1 = ajuste perfecto).
 * @property pointsUsed cuántos puntos entraron en el ajuste.
 */
data class FitResult(
    val exponentN: Double,
    val k: Double,
    val r2: Double,
    val pointsUsed: Int
)

/** Estadística de las repeticiones hechas a una misma distancia. */
data class RepeatStats(
    val distanceCm: Double,
    val count: Int,
    val mean: Double,
    val std: Double
) {
    /** Coeficiente de variación en %, (s / media)·100. */
    val cvPercent: Double get() = if (mean > 0) std / mean * 100.0 else 0.0
}

/**
 * Motor de datos: todos los cálculos del laboratorio. No depende de Android,
 * así que se prueba con pruebas automáticas (ver AnalysisTest.kt).
 */
object Analysis {

    fun mean(xs: List<Double>): Double =
        if (xs.isEmpty()) Double.NaN else xs.sum() / xs.size

    /** Desviación estándar muestral (divide por N − 1). Con menos de 2 datos devuelve 0. */
    fun std(xs: List<Double>): Double {
        if (xs.size < 2) return 0.0
        val m = mean(xs)
        return sqrt(xs.sumOf { (it - m) * (it - m) } / (xs.size - 1))
    }

    /** Iluminancia corregida: se resta la luz de fondo y nunca baja de 0. */
    fun corrected(meanLux: Double, backgroundLux: Double?): Double =
        max(meanLux - (backgroundLux ?: 0.0), 0.0)

    /**
     * Ajusta E = k / dⁿ con regresión lineal sobre ln E = ln k − n · ln d.
     * Solo usa puntos con d > 0 y E > 0 (el logaritmo de 0 no existe).
     * Devuelve null si no hay al menos 2 distancias distintas válidas.
     */
    fun fitPowerLaw(distances: List<Double>, intensities: List<Double>): FitResult? {
        val pts = distances.zip(intensities).filter { (d, i) -> d > 0 && i > 0 }
        if (pts.size < 2) return null

        val x = pts.map { ln(it.first) }
        val y = pts.map { ln(it.second) }
        val mx = mean(x)
        val my = mean(y)

        var sxx = 0.0
        var sxy = 0.0
        for (j in x.indices) {
            sxx += (x[j] - mx) * (x[j] - mx)
            sxy += (x[j] - mx) * (y[j] - my)
        }
        if (sxx == 0.0) return null // todas las distancias iguales

        val slope = sxy / sxx
        val intercept = my - slope * mx

        val ssTot = y.sumOf { (it - my) * (it - my) }
        val ssRes = x.indices.sumOf { j ->
            val pred = intercept + slope * x[j]
            (y[j] - pred) * (y[j] - pred)
        }
        val r2 = if (ssTot == 0.0) 1.0 else 1.0 - ssRes / ssTot

        return FitResult(exponentN = -slope, k = exp(intercept), r2 = r2, pointsUsed = pts.size)
    }

    /**
     * Mejor constante k para el modelo teórico E = k / d² (mínimos cuadrados):
     * k = Σ(E/d²) / Σ(1/d⁴). Devuelve null si no hay datos válidos.
     */
    fun fitInverseSquare(distances: List<Double>, intensities: List<Double>): Double? {
        val pts = distances.zip(intensities).filter { (d, _) -> d > 0 }
        if (pts.isEmpty()) return null
        val num = pts.sumOf { (d, i) -> i / (d * d) }
        val den = pts.sumOf { (d, _) -> 1.0 / (d * d * d * d) }
        return if (den == 0.0) null else num / den
    }

    /** Error relativo en porcentaje: |medido − esperado| / |esperado| · 100. */
    fun relativeErrorPercent(measured: Double, expected: Double): Double =
        abs(measured - expected) / abs(expected) * 100.0

    /** Agrupa por distancia y entrega la estadística de las distancias que tienen 2 o más mediciones. */
    fun repeatStats(distances: List<Double>, intensities: List<Double>): List<RepeatStats> =
        distances.zip(intensities)
            .groupBy { it.first }
            .filter { it.value.size >= 2 }
            .map { (d, pairs) ->
                val v = pairs.map { it.second }
                RepeatStats(d, v.size, mean(v), std(v))
            }
            .sortedBy { it.distanceCm }
}
