package cl.umayor.lablight

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.view.View
import cl.umayor.lablight.databinding.ViewStepperBinding
import cl.umayor.lablight.databinding.ViewTopbarBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Utilidades de interfaz compartidas por todas las pantallas. */
object Ui {

    /** Barra superior: título centrado y flecha atrás opcional (como en el prototipo). */
    fun topbar(tb: ViewTopbarBinding, title: String, onBack: (() -> Unit)?) {
        tb.txtTitle.text = title
        if (onBack == null) {
            tb.btnBack.visibility = View.INVISIBLE
        } else {
            tb.btnBack.visibility = View.VISIBLE
            tb.btnBack.setOnClickListener { onBack() }
        }
    }

    /** Indicador de pasos 1-2-3-4 del experimento. [current] va de 1 a 4. */
    fun stepper(s: ViewStepperBinding, current: Int) {
        val circles = listOf(s.step1, s.step2, s.step3, s.step4)
        val labels = listOf(s.label1, s.label2, s.label3, s.label4)
        val lines = listOf(s.line1, s.line2, s.line3)
        circles.forEachIndexed { i, c ->
            val step = i + 1
            c.setBackgroundResource(
                when {
                    step < current -> R.drawable.bg_step_done
                    step == current -> R.drawable.bg_step_active
                    else -> R.drawable.bg_step_todo
                }
            )
            c.text = if (step < current) "✓" else step.toString()
            labels[i].setTypeface(null, if (step == current) Typeface.BOLD else Typeface.NORMAL)
            labels[i].setTextColor(c.context.getColor(if (step == current) R.color.ink else R.color.ink_soft))
        }
        lines.forEachIndexed { i, l ->
            l.setBackgroundColor(l.context.getColor(if (i + 1 < current) R.color.sun else R.color.line))
        }
    }

    /** Barra inferior de navegación (Inicio · Experimentos · Aprender · Proyecto). */
    fun bottomNav(activity: Activity, nav: BottomNavigationView, selectedId: Int) {
        nav.selectedItemId = selectedId
        nav.setOnItemSelectedListener { item ->
            if (item.itemId == selectedId) return@setOnItemSelectedListener true
            val target = when (item.itemId) {
                R.id.nav_home -> HomeActivity::class.java
                R.id.nav_history -> HistoryActivity::class.java
                R.id.nav_learn -> LearnActivity::class.java
                else -> AboutActivity::class.java
            }
            val intent = Intent(activity, target)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            activity.startActivity(intent)
            @Suppress("DEPRECATION")
            activity.overridePendingTransition(0, 0)
            true
        }
    }

    fun date(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))

    fun f1(v: Double): String = String.format(Locale.US, "%.1f", v)
    fun f2(v: Double): String = String.format(Locale.US, "%.2f", v)
}

/** 10.0 → "10", 12.5 → "12.5" */
fun formatCm(value: Double): String =
    if (value % 1.0 == 0.0) String.format(Locale.US, "%.0f", value)
    else String.format(Locale.US, "%.1f", value)
