package cl.umayor.lablight

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import java.util.Random
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Instrumentación: una fuente de lecturas de iluminancia en lux.
 * Hay dos implementaciones con la misma "forma", así el resto de la app no
 * necesita saber si los datos vienen del sensor real o de la simulación.
 */
interface LuxSource {
    val name: String
    /** Máximo que puede medir (lux). Sobre este valor la lectura se "satura". */
    val maxRange: Float
    /** Menor cambio que puede detectar (lux). */
    val resolution: Float
    /** Última lectura en lux (NaN mientras no haya llegado ninguna). */
    val lastLux: Float
    fun start()
    fun stop()
}

/**
 * Sensor de luz ambiental (ALS) del teléfono, leído con la API de sensores de Android.
 *
 * El ALS es un sensor "on-change": si la luz no cambia, no llegan eventos nuevos.
 * Por eso la pantalla de medición no cuenta eventos, sino que toma una muestra de
 * [lastLux] cada 50 ms.
 */
class SensorLuxSource(
    context: Context,
    private val onLux: (Float) -> Unit
) : LuxSource, SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    val available: Boolean get() = sensor != null
    override val name: String get() = sensor?.name ?: "Sin sensor de luz"
    val vendor: String get() = sensor?.vendor ?: ""
    override val maxRange: Float get() = sensor?.maximumRange ?: 0f
    override val resolution: Float get() = sensor?.resolution ?: 0f

    private var last: Float = Float.NaN
    override val lastLux: Float get() = last

    override fun start() {
        val s = sensor ?: return
        sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        last = event.values[0]
        onLux(last)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

/**
 * Motor de simulación (plan de contingencia y modo demostración).
 * Genera lecturas con el modelo E = E₁₀ · (10 cm / d)² + fondo, con un ruido
 * aleatorio pequeño y saturación, como un sensor real. La distancia se lee de
 * la pantalla en cada momento.
 *
 * Los datos simulados siempre se marcan como tales en resultados y en el CSV.
 */
class SimulatedLuxSource(
    private val powerLux10cm: Double,
    private val distanceProvider: () -> Double?,
    private val onLux: (Float) -> Unit
) : LuxSource {

    companion object {
        const val BACKGROUND_LUX = 2.0
        const val NOISE_FRACTION = 0.02   // 2 % de ruido proporcional
        const val NOISE_ABS_LUX = 0.3     // ruido de fondo del "sensor"
        const val UPDATE_MS = 50L
    }

    override val name = "Sensor simulado (modelo 1/d²)"
    override val maxRange = 40000f
    override val resolution = 1f

    /** false mientras se mide la luz de fondo (fuente "apagada"). */
    var sourceOn = true

    private var last: Float = Float.NaN
    override val lastLux: Float get() = last

    private val random = Random()
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            last = value().toFloat()
            onLux(last)
            handler.postDelayed(this, UPDATE_MS)
        }
    }

    /** Iluminancia "verdadera" del modelo, sin ruido. */
    fun modelLux(d: Double?): Double {
        val src = if (sourceOn && d != null && d > 0) powerLux10cm * (10.0 / d).pow(2) else 0.0
        return src + BACKGROUND_LUX
    }

    private fun value(): Double {
        val ideal = modelLux(distanceProvider())
        val noisy = ideal * (1 + NOISE_FRACTION * random.nextGaussian()) + NOISE_ABS_LUX * random.nextGaussian()
        val clipped = min(max(noisy, 0.0), maxRange.toDouble())
        return floor(clipped / resolution) * resolution   // cuantización como un sensor real
    }

    override fun start() {
        if (running) return
        running = true
        handler.post(tick)
    }

    override fun stop() {
        running = false
        handler.removeCallbacks(tick)
    }
}
