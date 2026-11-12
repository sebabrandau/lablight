package cl.umayor.lablight

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cl.umayor.lablight.databinding.ActivityHistoryBinding
import cl.umayor.lablight.databinding.ItemExperimentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

/** Mis experimentos: lista de experimentos guardados en el teléfono. */
class HistoryActivity : AppCompatActivity() {

    private lateinit var b: ActivityHistoryBinding
    private lateinit var repo: ExperimentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(b.root)
        Ui.topbar(b.topbar, getString(R.string.title_history), null)
        repo = ExperimentRepository(this)
        b.btnNew.setOnClickListener { startActivity(Intent(this, SetupActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        Ui.bottomNav(this, b.bottomNav, R.id.nav_history)
        render()
    }

    private fun render() {
        val list = repo.loadAll()
        b.containerList.removeAllViews()
        b.txtEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        b.txtHint.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE

        list.forEach { exp ->
            val row = ItemExperimentBinding.inflate(layoutInflater, b.containerList, false)
            row.txtName.text = exp.name
            val mode = if (exp.mode == SourceMode.SIMULATION) " · simulación" else ""
            row.txtDate.text = Ui.date(exp.createdAt) + mode

            val corrected = exp.measurements.map { Analysis.corrected(it.meanLux, exp.backgroundLux) }
            val fit = Analysis.fitPowerLaw(exp.measurements.map { it.distanceCm }, corrected)
            row.txtSummary.text = if (fit != null && fit.pointsUsed >= 3) {
                String.format(Locale.US, "%d mediciones · n = %.2f · R² = %.3f",
                    exp.measurements.size, fit.exponentN, fit.r2)
            } else {
                "${exp.measurements.size} mediciones · faltan datos para el ajuste"
            }

            row.root.setOnClickListener {
                startActivity(Intent(this, ResultsActivity::class.java).putExtra(ResultsActivity.EXTRA_ID, exp.id))
            }
            row.root.setOnLongClickListener {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.delete_confirm_title)
                    .setMessage(getString(R.string.delete_confirm_body, exp.name))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        repo.delete(exp.id)
                        if (ExperimentStore.current?.id == exp.id) ExperimentStore.current = null
                        render()
                    }
                    .show()
                true
            }
            b.containerList.addView(row.root)
        }
    }
}
