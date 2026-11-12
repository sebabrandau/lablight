package cl.umayor.lablight

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.umayor.lablight.databinding.ActivityPredictionBinding

/**
 * Paso 2 · Predicción: el estudiante elige qué cree que pasará al duplicar la distancia.
 * Esta hipótesis se compara con el resultado medido en el paso 4.
 */
class PredictionActivity : AppCompatActivity() {

    private lateinit var b: ActivityPredictionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPredictionBinding.inflate(layoutInflater)
        setContentView(b.root)
        Ui.topbar(b.topbar, getString(R.string.title_new_experiment)) { finish() }
        Ui.stepper(b.stepper, 2)

        val exp = ExperimentStore.current
        if (exp == null) { finish(); return }

        val pad = (12 * resources.displayMetrics.density).toInt()
        Prediction.options.forEachIndexed { i, text ->
            val rb = RadioButton(this)
            rb.id = 1000 + i
            rb.text = text
            rb.textSize = 16f
            rb.setPadding(pad, pad, pad, pad)
            rb.setTextColor(getColor(R.color.ink))
            b.radioPrediction.addView(
                rb, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            )
        }
        exp.prediction?.let { b.radioPrediction.check(1000 + it) }

        b.btnNext.setOnClickListener {
            val checked = b.radioPrediction.checkedRadioButtonId
            if (checked == -1) {
                Toast.makeText(this, R.string.prediction_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            exp.prediction = checked - 1000
            startActivity(Intent(this, MeasureActivity::class.java))
        }
    }
}
