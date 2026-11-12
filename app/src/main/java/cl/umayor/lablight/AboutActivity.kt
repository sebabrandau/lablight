package cl.umayor.lablight

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cl.umayor.lablight.databinding.ActivityAboutBinding
import cl.umayor.lablight.databinding.ItemLearnCardBinding

/** Información del proyecto: objetivo, pregunta, equipo con roles, versión y repositorio. */
class AboutActivity : AppCompatActivity() {

    private lateinit var b: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(b.root)
        Ui.topbar(b.topbar, getString(R.string.title_about), null)

        val version = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: ""
        } catch (e: Exception) { "" }

        add(R.drawable.ic_target, "Objetivo",
            "Permitir que estudiantes de 3° y 4° medio (Física, formación diferenciada) verifiquen experimentalmente la ley del inverso del cuadrado de la luz usando el sensor de luminosidad de su celular, sin necesidad de equipos de laboratorio costosos.")
        add(R.drawable.ic_bulb, "Pregunta de investigación",
            "¿Cómo varía la intensidad de la luz al aumentar la distancia entre una fuente luminosa y el sensor de luminosidad de un celular?")
        add(R.drawable.ic_info, "Equipo",
            "Martina Lineros · Product Owner / Líder de proyecto\n" +
            "Catalina González · Responsable científica y pedagógica\n" +
            "Sebastián Brandau · Responsable de software/datos y UX/UI\n" +
            "Valentina Torres · Responsable de hardware e instrumentación\n\n" +
            "Docente: Prof. Ana G. E. Jorquera F.\nProyecto de Innovación Tecnológica · Universidad Mayor · 2026")
        add(R.drawable.ic_chart, "Versión y código fuente",
            "Bragolito versión $version (Android 8.0 o superior).\nCódigo y documentación: ${getString(R.string.repo_url)}\n\nToca esta tarjeta para abrir el repositorio.") {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.repo_url))))
            } catch (e: Exception) {
                // Sin navegador instalado: no se hace nada
            }
        }
        add(R.drawable.ic_book, "Privacidad",
            "La app solo lee el sensor de luz. No usa la cámara, internet ni datos personales. Los experimentos se guardan únicamente en este teléfono.")
    }

    override fun onResume() {
        super.onResume()
        Ui.bottomNav(this, b.bottomNav, R.id.nav_about)
    }

    private fun add(icon: Int, title: String, body: String, onClick: (() -> Unit)? = null) {
        val card = ItemLearnCardBinding.inflate(layoutInflater, b.containerCards, false)
        card.imgIcon.setImageResource(icon)
        card.txtTitle.text = title
        card.txtBody.text = body
        card.txtBody.visibility = View.VISIBLE
        card.imgChevron.visibility = View.GONE
        if (onClick != null) card.root.setOnClickListener { onClick() } else card.root.isClickable = false
        b.containerCards.addView(card.root)
    }
}
