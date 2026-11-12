package cl.umayor.lablight

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import cl.umayor.lablight.databinding.ActivitySetupBinding
import java.util.Locale

/**
 * Paso 1 · Configuración: nombre del experimento, fuente de luz y modo de medición
 * (sensor real o simulación). Muestra el estado del sensor y una lectura en vivo,
 * para que el estudiante compruebe que su celular puede medir antes de empezar.
 */
class SetupActivity : AppCompatActivity() {

    private lateinit var b: ActivitySetupBinding
    private lateinit var sensor: SensorLuxSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(b.root)
        Ui.topbar(b.topbar, getString(R.string.title_new_experiment)) { finish() }
        Ui.stepper(b.stepper, 1)

        sensor = SensorLuxSource(this) { lux ->
            b.txtLiveLux.text = getString(R.string.live_lux_format, lux)
        }

        // Fuentes de luz sugeridas
        val sources = listOf("Linterna de otro celular", "Ampolleta LED", "Lámpara de escritorio", "Otra")
        b.spinnerSource.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sources)

        b.inputName.setText("Medición " + Ui.date(System.currentTimeMillis()))

        // Estado del sensor
        if (sensor.available) {
            b.txtSensorName.text = sensor.name
            b.txtSensorDetail.text = getString(
                R.string.sensor_detail_format, sensor.vendor, sensor.maxRange, sensor.resolution
            )
            b.toggleMode.check(R.id.btnModeSensor)
        } else {
            // Sin sensor: se ofrece la simulación para que la experiencia igual se pueda completar
            b.txtSensorName.text = getString(R.string.no_sensor_title)
            b.txtSensorDetail.text = getString(R.string.no_sensor_detail)
            b.txtLiveLux.visibility = View.GONE
            b.txtLiveHint.visibility = View.GONE
            b.btnModeSensor.isEnabled = false
            b.toggleMode.check(R.id.btnModeSim)
        }

        b.toggleMode.addOnButtonCheckedListener { _, _, _ -> renderMode() }
        b.sliderPower.addOnChangeListener { _, value, _ -> renderPower(value) }
        renderPower(b.sliderPower.value)
        renderMode()

        b.btnNext.setOnClickListener {
            val name = b.inputName.text?.toString()?.trim().orEmpty()
                .ifBlank { "Medición " + Ui.date(System.currentTimeMillis()) }
            val mode = if (isSim()) SourceMode.SIMULATION else SourceMode.SENSOR
            val source = if (isSim()) "Fuente simulada" else b.spinnerSource.selectedItem.toString()
            ExperimentStore.startNew(name, source, mode, b.sliderPower.value.toDouble())
            startActivity(Intent(this, PredictionActivity::class.java))
        }
    }

    private fun isSim() = b.toggleMode.checkedButtonId == R.id.btnModeSim

    private fun renderMode() {
        val sim = isSim()
        b.cardSim.visibility = if (sim) View.VISIBLE else View.GONE
        // La tarjeta del sensor se muestra siempre si no hay sensor (para explicar por qué)
        b.cardSensor.visibility = if (!sim || !sensor.available) View.VISIBLE else View.GONE
        b.spinnerSource.isEnabled = !sim
    }

    private fun renderPower(value: Float) {
        b.txtSimPower.text = String.format(
            Locale.US, "%s: %.0f lux", getString(R.string.sim_power_label), value
        )
    }

    override fun onResume() {
        super.onResume()
        sensor.start()
    }

    override fun onPause() {
        super.onPause()
        sensor.stop()
    }
}
