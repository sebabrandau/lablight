package cl.umayor.lablight

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.umayor.lablight.databinding.ActivityMeasureBinding
import cl.umayor.lablight.databinding.ItemMeasurementBinding
import java.util.Locale
import kotlin.math.log10
import kotlin.math.max

/**
 * Paso 3 · Medición en curso.
 * - Muestra la iluminancia en vivo (indicador circular) y avisos automáticos
 *   (sensor saturado, luz muy débil).
 * - Mide la luz de fondo con la fuente apagada.
 * - El estudiante fija la distancia y guarda una medición: promedio y desviación
 *   estándar de 2 s de lecturas (≈ 40 muestras, una cada 50 ms).
 * - Cada cambio se guarda automáticamente en "Mis experimentos".
 */
class MeasureActivity : AppCompatActivity() {

    companion object {
        const val SAMPLE_DURATION_MS = 2000L   // ventana de cada medición
        const val SAMPLE_INTERVAL_MS = 50L     // período de muestreo → 20 Hz
        const val STEP_CM = 5.0                // lo que suman/restan los botones ±5
        const val MIN_DISTANCES = 3            // mínimo de distancias distintas para ver resultados
        const val WEAK_LUX = 5.0               // bajo esto, la señal es demasiado débil
    }

    private lateinit var b: ActivityMeasureBinding
    private lateinit var exp: Experiment
    private lateinit var repo: ExperimentRepository
    private lateinit var source: LuxSource
    private val handler = Handler(Looper.getMainLooper())
    private var sampling = false
    private var samplingBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMeasureBinding.inflate(layoutInflater)
        setContentView(b.root)
        Ui.topbar(b.topbar, getString(R.string.title_measuring)) { finish() }
        Ui.stepper(b.stepper, 3)

        val current = ExperimentStore.current
        if (current == null) { finish(); return }
        exp = current
        repo = ExperimentRepository(this)

        source = if (exp.mode == SourceMode.SIMULATION) {
            b.txtSimBadge.visibility = View.VISIBLE
            SimulatedLuxSource(exp.simPowerLux10cm, { readDistanceQuiet() }) { onLux(it) }
        } else {
            SensorLuxSource(this) { onLux(it) }
        }

        b.inputDistance.setText(formatCm(exp.lastDistanceCm))
        b.btnMinus.setOnClickListener { changeDistance(-STEP_CM) }
        b.btnPlus.setOnClickListener { changeDistance(STEP_CM) }

        b.btnBackground.setOnClickListener {
            measure(background = true) { mean, _, _ ->
                exp.backgroundLux = mean
                persist()
                renderBackground()
            }
        }

        b.btnSave.setOnClickListener {
            val d = readDistance() ?: return@setOnClickListener
            measure(background = false) { mean, std, n ->
                exp.measurements.add(Measurement(d, mean, std, n))
                exp.lastDistanceCm = d
                persist()
                renderMeasurements()
                warnIfBadPoint(d, mean)
            }
        }

        b.btnFinish.setOnClickListener {
            persist()
            startActivity(Intent(this, ResultsActivity::class.java))
        }

        renderBackground()
        renderMeasurements()
    }

    override fun onResume() {
        super.onResume()
        if (!::source.isInitialized) return
        source.start()
        renderMeasurements() // por si se volvió desde Resultados
    }

    override fun onPause() {
        super.onPause()
        if (!::source.isInitialized) return
        source.stop()
        if (sampling) {
            handler.removeCallbacksAndMessages(null)
            finishSampling()
            Toast.makeText(this, R.string.measurement_cancelled, Toast.LENGTH_SHORT).show()
        }
    }

    private fun persist() {
        repo.save(exp)
    }

    // ---------- Lectura en vivo ----------

    private fun onLux(lux: Float) {
        b.txtLux.text = if (lux >= 100f) String.format(Locale.US, "%.0f", lux)
                        else String.format(Locale.US, "%.1f", lux)
        // Indicador en escala logarítmica: así se ve el cambio tanto con 5 como con 5000 lux
        val maxR = max(source.maxRange, 10f)
        val pct = (log10(lux + 1.0) / log10(maxR + 1.0) * 100).toInt().coerceIn(0, 100)
        b.gauge.setProgressCompat(pct, false)
        renderTip(lux)
    }

    private fun renderTip(lux: Float) {
        val saturated = source.maxRange > 0 && lux >= source.maxRange * 0.98f
        val weak = !samplingBackground && lux < WEAK_LUX
        when {
            saturated -> {
                b.boxTip.setBackgroundResource(R.drawable.bg_warn)
                b.txtTip.setText(R.string.tip_saturated)
            }
            weak -> {
                b.boxTip.setBackgroundResource(R.drawable.bg_warn)
                b.txtTip.setText(R.string.tip_weak)
            }
            else -> {
                b.boxTip.setBackgroundResource(R.drawable.bg_tip)
                b.txtTip.setText(R.string.tip_default)
            }
        }
    }

    // ---------- Medición ----------

    /** Toma una muestra cada 50 ms durante 2 s y entrega promedio, desviación estándar y N. */
    private fun measure(background: Boolean, onDone: (mean: Double, std: Double, samples: Int) -> Unit) {
        if (sampling) return
        if (source.lastLux.isNaN()) {
            Toast.makeText(this, R.string.sensor_not_ready, Toast.LENGTH_SHORT).show()
            return
        }

        sampling = true
        samplingBackground = background
        (source as? SimulatedLuxSource)?.sourceOn = !background
        setButtonsEnabled(false)
        b.progressSampling.progress = 0
        b.progressSampling.visibility = View.VISIBLE
        b.txtStatus.visibility = View.VISIBLE

        val samples = mutableListOf<Double>()
        val start = SystemClock.elapsedRealtime()
        // En simulación se espera un instante para que la "fuente" cambie de estado
        val delay = if (source is SimulatedLuxSource) 150L else 0L

        handler.postDelayed(object : Runnable {
            override fun run() {
                val v = source.lastLux
                if (!v.isNaN()) samples.add(v.toDouble())
                val elapsed = SystemClock.elapsedRealtime() - start
                val pct = ((elapsed * 100) / SAMPLE_DURATION_MS).toInt().coerceIn(0, 100)
                b.progressSampling.progress = pct
                b.txtStatus.text = getString(R.string.measuring_format, pct)

                if (elapsed >= SAMPLE_DURATION_MS) {
                    finishSampling()
                    if (samples.isNotEmpty()) {
                        onDone(Analysis.mean(samples), Analysis.std(samples), samples.size)
                    }
                } else {
                    handler.postDelayed(this, SAMPLE_INTERVAL_MS)
                }
            }
        }, delay)
    }

    private fun finishSampling() {
        sampling = false
        samplingBackground = false
        (source as? SimulatedLuxSource)?.sourceOn = true
        setButtonsEnabled(true)
        b.progressSampling.visibility = View.GONE
        b.txtStatus.visibility = View.GONE
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        b.btnBackground.isEnabled = enabled
        b.btnSave.isEnabled = enabled
        b.btnMinus.isEnabled = enabled
        b.btnPlus.isEnabled = enabled
        b.btnFinish.isEnabled = enabled && distinctDistances() >= MIN_DISTANCES
    }

    private fun distinctDistances() = exp.measurements.map { it.distanceCm }.distinct().size

    /** Avisa si el punto recién guardado probablemente no sirve. */
    private fun warnIfBadPoint(d: Double, meanLux: Double) {
        val saturated = source.maxRange > 0 && meanLux >= source.maxRange * 0.98
        val corrected = Analysis.corrected(meanLux, exp.backgroundLux)
        val msg = when {
            saturated -> getString(R.string.warn_saved_saturated)
            corrected < WEAK_LUX -> getString(R.string.warn_saved_weak)
            else -> getString(R.string.measurement_saved_format, formatCm(d), Ui.f1(meanLux))
        }
        Toast.makeText(this, msg, if (saturated || corrected < WEAK_LUX) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }

    // ---------- Distancia ----------

    private fun readDistanceQuiet(): Double? =
        b.inputDistance.text?.toString()?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it > 0 }

    private fun readDistance(): Double? {
        val d = readDistanceQuiet()
        if (d == null) {
            b.inputDistanceLayout.error = getString(R.string.error_distance)
            return null
        }
        b.inputDistanceLayout.error = null
        return d
    }

    private fun changeDistance(delta: Double) {
        val current = readDistanceQuiet() ?: 0.0
        b.inputDistance.setText(formatCm(max(1.0, current + delta)))
        b.inputDistanceLayout.error = null
    }

    // ---------- Dibujo de la pantalla ----------

    private fun renderBackground() {
        val bg = exp.backgroundLux
        if (bg == null) {
            b.txtBackground.text = getString(R.string.background_none)
            b.btnBackground.setText(R.string.btn_background)
        } else {
            b.txtBackground.text = getString(R.string.background_format, bg)
            b.btnBackground.setText(R.string.btn_background_again)
        }
    }

    private fun renderMeasurements() {
        val list = exp.measurements
        b.txtCount.text = getString(R.string.saved_count_format, list.size)
        b.containerMeasurements.removeAllViews()

        if (list.isEmpty()) {
            val empty = TextView(this)
            empty.setText(R.string.empty_measurements)
            empty.setTextColor(getColor(R.color.ink_soft))
            empty.textSize = 13f
            b.containerMeasurements.addView(empty)
        }

        list.forEachIndexed { index, m ->
            val row = ItemMeasurementBinding.inflate(layoutInflater, b.containerMeasurements, false)
            row.txtDistance.text = "${formatCm(m.distanceCm)} cm"
            row.txtValue.text = String.format(
                Locale.US, "%.1f ± %.1f lux  (%d muestras)", m.meanLux, m.stdLux, m.samples
            )
            row.btnDelete.setOnClickListener {
                if (sampling) return@setOnClickListener
                exp.measurements.removeAt(index)
                persist()
                renderMeasurements()
            }
            b.containerMeasurements.addView(row.root)
        }

        val enough = distinctDistances() >= MIN_DISTANCES
        b.btnFinish.isEnabled = enough && !sampling
        b.txtResultsHint.visibility = if (enough) View.GONE else View.VISIBLE
    }
}
