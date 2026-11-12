package cl.umayor.lablight

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.umayor.lablight.databinding.ActivityResultsBinding
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

/**
 * Paso 4 · Resultados.
 * - Ajusta los datos a E = k / dⁿ y compara n con el valor teórico 2 (error relativo).
 * - Gráfica con unidades: puntos medidos, curva ajustada y curva ideal 1/d²,
 *   con opción de escala logarítmica.
 * - Tabla de datos (incluye E·d², que debe ser casi constante si se cumple la ley).
 * - Compara con la predicción del paso 2 y muestra la repetibilidad.
 * - Permite compartir los datos (CSV) y guardar el experimento.
 *
 * Se abre desde la medición (experimento en curso) o desde "Mis experimentos" (EXTRA_ID).
 */
class ResultsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "experiment_id"
    }

    private lateinit var b: ActivityResultsBinding
    private lateinit var repo: ExperimentRepository
    private lateinit var exp: Experiment
    private var fromHistory = false

    // Datos ordenados por distancia
    private lateinit var data: List<Measurement>
    private lateinit var distances: List<Double>
    private lateinit var corrected: List<Double>
    private var fit: FitResult? = null
    private var kInverseSquare: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(b.root)
        repo = ExperimentRepository(this)

        val id = intent.getLongExtra(EXTRA_ID, -1L)
        fromHistory = id != -1L
        val loaded = if (fromHistory) repo.find(id) else ExperimentStore.current
        if (loaded == null) { finish(); return }
        exp = loaded

        Ui.topbar(b.topbar, getString(R.string.results_title)) { finish() }
        if (fromHistory) b.stepper.root.visibility = View.GONE else Ui.stepper(b.stepper, 4)
        if (!fromHistory) repo.save(exp) // guardado automático

        compute()
        renderHeader()
        renderFit()
        renderPrediction()
        renderRepeat()
        renderTable()
        setupChart()
        drawChart(logScale = false)

        b.toggleView.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val chart = checkedId == R.id.btnTabChart
            b.cardChart.visibility = if (chart) View.VISIBLE else View.GONE
            b.cardTable.visibility = if (chart) View.GONE else View.VISIBLE
        }
        b.switchLog.setOnCheckedChangeListener { _, checked -> drawChart(checked) }
        b.btnShare.setOnClickListener { shareCsv() }
        b.btnStore.setOnClickListener {
            repo.save(exp)
            Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_SHORT).show()
        }
        b.btnMeasureMore.setOnClickListener {
            if (fromHistory) {
                ExperimentStore.current = exp
                startActivity(Intent(this, MeasureActivity::class.java))
            }
            finish()
        }
        b.btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }

    private fun compute() {
        val bg = exp.backgroundLux
        data = exp.measurements.sortedBy { it.distanceCm }
        distances = data.map { it.distanceCm }
        corrected = data.map { Analysis.corrected(it.meanLux, bg) }
        fit = Analysis.fitPowerLaw(distances, corrected)
        kInverseSquare = Analysis.fitInverseSquare(distances, corrected)
    }

    // ---------- Encabezado ----------

    private fun renderHeader() {
        val bg = exp.backgroundLux
        b.txtExpName.text = exp.name
        val mode = if (exp.mode == SourceMode.SIMULATION) "Datos SIMULADOS" else "Sensor del celular"
        b.txtExpInfo.text = String.format(
            Locale.US, "%s\n%s · %s · fondo %s · %d mediciones",
            Ui.date(exp.createdAt), mode, exp.lightSource,
            if (bg == null) "no medido" else "${Ui.f1(bg)} lux",
            data.size
        )
    }

    // ---------- Ajuste ----------

    private fun renderFit() {
        val f = fit
        if (f == null) {
            b.txtExponent.text = "n = —"
            b.txtFitDetail.setText(R.string.fit_not_possible)
            b.txtVerdict.visibility = View.GONE
            return
        }

        val err = Analysis.relativeErrorPercent(f.exponentN, 2.0)
        b.txtExponent.text = String.format(Locale.US, "n = %.2f", f.exponentN)
        b.txtFitDetail.text = String.format(
            Locale.US,
            "Modelo ajustado: E = %.0f / d^%.2f  (E en lux, d en cm)\nR² = %.3f · %d puntos usados\nValor teórico: n = 2 · Error relativo: %.1f %%",
            f.k, f.exponentN, f.r2, f.pointsUsed, err
        )

        val excluded = corrected.count { it <= 0.0 }
        val maxLux = corrected.maxOrNull() ?: 0.0
        b.txtVerdict.text = when {
            f.pointsUsed < 3 ->
                "⚠️ $excluded de ${corrected.size} mediciones quedaron en 0 lux y no sirven para el ajuste; con solo ${f.pointsUsed} puntos el resultado no es confiable. " +
                    "Acerca o apunta mejor la fuente al sensor y vuelve a medir."
            maxLux < 20.0 ->
                "⚠️ La luz que llega al sensor es muy débil (máx. ${Ui.f1(maxLux)} lux). " +
                    "Apunta la fuente directo al sensor (arriba de la pantalla) para obtener datos confiables."
            err <= 10.0 ->
                "✅ Tus datos son consistentes con la ley del inverso del cuadrado: al duplicar la distancia, la intensidad cae a la cuarta parte."
            err <= 25.0 ->
                "⚠️ Los datos se parecen a la ley del inverso del cuadrado, pero con una desviación notable. Revisa la luz de fondo y que el sensor apunte directo a la fuente."
            else ->
                "❌ Los datos se alejan de n = 2. Posibles causas: fuente muy cerca (no se comporta como fuente puntual), reflejos en paredes o mesa, luz ambiente variable o sensor saturado."
        }
        b.txtVerdict.visibility = View.VISIBLE
    }

    // ---------- Predicción del paso 2 ----------

    private fun renderPrediction() {
        val p = exp.prediction
        val f = fit
        if (p == null || p !in Prediction.options.indices) {
            b.cardPrediction.visibility = View.GONE
            return
        }
        val chosen = Prediction.options[p]
        if (f == null || f.pointsUsed < 3) {
            b.txtPrediction.text = "Elegiste: «$chosen». Faltan datos válidos para comparar."
            return
        }
        val factor = 2.0.pow(f.exponentN)
        val measured = String.format(
            Locale.US,
            "Según tus datos, al duplicar la distancia la intensidad se divide por 2ⁿ = %.1f (n = %.2f).",
            factor, f.exponentN
        )
        val expectedN = Prediction.expectedN[p]
        val verdict = when {
            expectedN == null -> "Ahora ya tienes la respuesta basada en evidencia."
            abs(f.exponentN - expectedN) <= 0.3 -> "✅ Tu predicción coincide con lo que mediste."
            else -> "❌ Tu predicción no coincide con lo que mediste. ¿Por qué crees que la luz disminuye así?"
        }
        b.txtPrediction.text = "Elegiste: «$chosen».\n$measured\n$verdict"
    }

    // ---------- Repetibilidad ----------

    private fun renderRepeat() {
        val stats = Analysis.repeatStats(distances, corrected)
        if (stats.isEmpty()) {
            b.txtRepeat.text = "Para evaluar la repetibilidad, guarda al menos 5 mediciones a una misma distancia (por ejemplo, 5 veces a 20 cm, volviendo a ubicar la fuente cada vez)."
            return
        }
        b.txtRepeat.text = stats.joinToString("\n") {
            String.format(
                Locale.US, "%s cm: %d mediciones · media %.1f lux · s = %.1f lux (%.1f %%)",
                formatCm(it.distanceCm), it.count, it.mean, it.std, it.cvPercent
            )
        }
    }

    // ---------- Tabla ----------

    private fun renderTable() {
        val table = b.tableData
        table.removeAllViews()
        table.addView(
            row(
                listOf("d (cm)", "E medida (lux)", "σ (lux)", "E corregida (lux)", "E·d² (lux·cm²)", "E ideal 1/d² (lux)", "Error (%)"),
                header = true
            )
        )

        val k2 = kInverseSquare
        data.forEachIndexed { idx, m ->
            val ideal = k2?.let { it / (m.distanceCm * m.distanceCm) }
            val err = if (ideal != null && ideal > 0) Analysis.relativeErrorPercent(corrected[idx], ideal) else null
            table.addView(
                row(
                    listOf(
                        formatCm(m.distanceCm),
                        Ui.f2(m.meanLux),
                        Ui.f2(m.stdLux),
                        Ui.f2(corrected[idx]),
                        String.format(Locale.US, "%.0f", corrected[idx] * m.distanceCm * m.distanceCm),
                        ideal?.let { Ui.f2(it) } ?: "—",
                        err?.let { Ui.f1(it) } ?: "—"
                    ),
                    header = false
                )
            )
        }
    }

    private fun row(cells: List<String>, header: Boolean): TableRow {
        val tr = TableRow(this)
        val pad = (8 * resources.displayMetrics.density).toInt()
        cells.forEach { text ->
            val tv = TextView(this)
            tv.text = text
            tv.setPadding(pad, pad / 2, pad, pad / 2)
            tv.setTextColor(getColor(if (header) R.color.ink else R.color.ink_soft))
            if (header) tv.setTypeface(null, Typeface.BOLD)
            tr.addView(tv)
        }
        return tr
    }

    // ---------- Gráfico ----------

    private fun setupChart() {
        val chart = b.chart
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.legend.isWordWrapEnabled = true
        chart.setNoDataText("Sin datos")
        chart.setDrawOrder(arrayOf(DrawOrder.LINE, DrawOrder.SCATTER))
    }

    /** @param logScale false → E vs d (curva); true → ln E vs ln d (recta de pendiente −n). */
    private fun drawChart(logScale: Boolean) {
        val chart = b.chart
        val sun = getColor(R.color.sun)
        val ink = getColor(R.color.ink)

        fun tx(d: Double) = if (logScale) ln(d) else d
        fun ty(i: Double) = if (logScale) ln(i) else i

        // Puntos medidos (MPAndroidChart necesita los puntos ordenados por x)
        val measured = ArrayList<Entry>()
        distances.indices.forEach { j ->
            val d = distances[j]
            val i = corrected[j]
            if (!logScale || (d > 0 && i > 0)) measured.add(Entry(tx(d).toFloat(), ty(i).toFloat()))
        }
        if (measured.isEmpty()) {
            chart.clear()   // sin puntos válidos: el gráfico muestra "Sin datos"
            return
        }
        val scatter = ScatterDataSet(measured, getString(R.string.legend_measured)).apply {
            setScatterShape(ScatterChart.ScatterShape.CIRCLE)
            scatterShapeSize = 18f
            color = sun
            setScatterShapeHoleColor(ink)
            scatterShapeHoleRadius = 2f
            setDrawValues(false)
        }

        // Curvas del modelo
        val lines = LineData()
        val dMin = distances.filter { it > 0 }.minOrNull()
        val dMax = distances.maxOrNull()
        if (dMin != null && dMax != null && dMax > dMin) {
            fun curve(k: Double, n: Double): ArrayList<Entry> {
                val pts = ArrayList<Entry>()
                val steps = 60
                for (s in 0..steps) {
                    val d = dMin + (dMax - dMin) * s / steps
                    val i = k / d.pow(n)
                    if (i > 0) pts.add(Entry(tx(d).toFloat(), ty(i).toFloat()))
                }
                return pts
            }

            fit?.let { f ->
                lines.addDataSet(
                    LineDataSet(curve(f.k, f.exponentN), getString(R.string.legend_fit)).apply {
                        color = getColor(R.color.model)
                        lineWidth = 2.5f
                        setDrawCircles(false)
                        setDrawValues(false)
                    }
                )
            }
            kInverseSquare?.let { k2 ->
                lines.addDataSet(
                    LineDataSet(curve(k2, 2.0), getString(R.string.legend_ideal)).apply {
                        color = getColor(R.color.ideal)
                        lineWidth = 2f
                        enableDashedLine(14f, 10f, 0f)
                        setDrawCircles(false)
                        setDrawValues(false)
                    }
                )
            }
        }

        val combined = CombinedData()
        combined.setData(ScatterData(scatter))
        if (lines.dataSetCount > 0) combined.setData(lines)

        if (logScale) {
            chart.axisLeft.resetAxisMinimum()
            chart.xAxis.resetAxisMinimum()
            b.txtAxisY.setText(R.string.axis_y_log)
            b.txtAxisX.setText(R.string.axis_x_log)
            b.txtAxisNote.visibility = View.VISIBLE
        } else {
            chart.axisLeft.axisMinimum = 0f
            chart.xAxis.axisMinimum = 0f
            b.txtAxisY.setText(R.string.axis_y_linear)
            b.txtAxisX.setText(R.string.axis_x_linear)
            b.txtAxisNote.visibility = View.GONE
        }

        chart.data = combined
        chart.notifyDataSetChanged()
        chart.invalidate()
        chart.animateX(400)
    }

    // ---------- Exportar ----------

    private fun shareCsv() {
        val bg = exp.backgroundLux
        val sb = StringBuilder()
        sb.append("experimento,").append(exp.name.replace(',', ' ')).append('\n')
        sb.append("fecha,").append(Ui.date(exp.createdAt)).append('\n')
        sb.append("modo,").append(if (exp.mode == SourceMode.SIMULATION) "SIMULACION" else "SENSOR").append('\n')
        sb.append("fuente,").append(exp.lightSource.replace(',', ' ')).append('\n')
        sb.append("fondo_lux,").append(bg?.let { Ui.f2(it) } ?: "").append('\n')
        exp.prediction?.let { p ->
            if (p in Prediction.options.indices) sb.append("prediccion,").append(Prediction.options[p]).append('\n')
        }
        fit?.let {
            sb.append(String.format(Locale.US, "exponente_n,%.4f\nk,%.4f\nr2,%.4f\nerror_rel_n_pct,%.2f\n",
                it.exponentN, it.k, it.r2, Analysis.relativeErrorPercent(it.exponentN, 2.0)))
        }
        sb.append("\ndistancia_cm,lux_promedio,lux_desv_est,lux_corregido,E_por_d2_lux_cm2,lux_ideal_inv_cuadrado,error_rel_pct,muestras\n")
        val k2 = kInverseSquare
        data.forEachIndexed { idx, m ->
            val ideal = k2?.let { it / (m.distanceCm * m.distanceCm) }
            val err = if (ideal != null && ideal > 0) Analysis.relativeErrorPercent(corrected[idx], ideal) else null
            sb.append(
                listOf(
                    formatCm(m.distanceCm), Ui.f2(m.meanLux), Ui.f2(m.stdLux), Ui.f2(corrected[idx]),
                    String.format(Locale.US, "%.1f", corrected[idx] * m.distanceCm * m.distanceCm),
                    ideal?.let { Ui.f2(it) } ?: "", err?.let { Ui.f2(it) } ?: "", m.samples.toString()
                ).joinToString(",")
            ).append('\n')
        }

        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "LabLight – ${exp.name}")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        startActivity(Intent.createChooser(send, getString(R.string.btn_share)))
    }
}
