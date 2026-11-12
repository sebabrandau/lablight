package cl.umayor.lablight

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cl.umayor.lablight.databinding.ActivityLearnBinding
import cl.umayor.lablight.databinding.ItemLearnCardBinding

/** Aprender: conceptos clave (pestaña 1) e instrucciones de uso paso a paso (pestaña 2). */
class LearnActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TAB_HOW = "tab_how"

        /** Pestaña "Conceptos": título → explicación. */
        val CONCEPTS = listOf(
            Triple(R.drawable.ic_bulb, "¿Qué es la luz?",
                "La luz es una forma de energía que viaja en línea recta desde la fuente, como ondas electromagnéticas. Una fuente pequeña la reparte en todas direcciones."),
            Triple(R.drawable.ic_target, "¿Qué es un sensor de luminosidad?",
                "Es un fotodiodo ubicado bajo la pantalla, cerca de la cámara frontal. Convierte la luz que recibe en una señal eléctrica, y el celular la entrega como iluminancia en lux. El teléfono lo usa para ajustar el brillo de la pantalla; esta app lo usa como instrumento de medición."),
            Triple(R.drawable.ic_chart, "Iluminancia (E) y lux",
                "La iluminancia es la cantidad de luz que llega a cada metro cuadrado de una superficie. Se mide en lux (1 lx = 1 lumen/m²). Como referencia: una sala iluminada tiene unos 300 lux y un día soleado más de 30 000 lux."),
            Triple(R.drawable.ic_ruler, "¿Por qué la intensidad varía con la distancia?",
                "La luz de una fuente pequeña se reparte sobre una esfera de área 4πd². Si te alejas al doble, el área es 4 veces mayor y a cada punto le llega 1/4 de la luz. Por eso E = I/d²: la ley del inverso del cuadrado."),
            Triple(R.drawable.ic_chart, "¿Cómo se calcula el exponente n?",
                "La app ajusta tus datos al modelo E = k/dⁿ. Al aplicar logaritmo queda ln E = ln k − n·ln d, que es una recta. La pendiente de esa recta es −n. Si se cumple la ley, n ≈ 2. El error relativo es |n − 2| / 2 × 100 %."),
            Triple(R.drawable.ic_info, "Luz de fondo, saturación y errores",
                "La luz de fondo es la que llega al sensor con la fuente apagada: se resta de cada medición. Si la luz supera el máximo del sensor, la lectura se queda pegada (saturación). Reflejos, mala alineación y medir muy cerca de la fuente también producen errores.")
        )

        /** Pestaña "Cómo funciona": pasos de uso, iguales a los de la guía del estudiante. */
        val HOW = listOf(
            Triple(R.drawable.ic_info, "Antes de empezar",
                "Busca una sala oscura, una fuente de luz pequeña (por ejemplo la linterna de otro celular) y una regla o huincha. Ubica el sensor: tapa con el dedo la parte superior de la pantalla; donde la lectura baje casi a 0 está el sensor."),
            Triple(R.drawable.ic_target, "Paso 1 · Configuración",
                "En Inicio toca \"Nuevo experimento\". Escribe un nombre, elige la fuente de luz y el modo: \"Sensor del celular\" para medir de verdad o \"Simulación\" si tu celular no tiene sensor. Toca Siguiente."),
            Triple(R.drawable.ic_bulb, "Paso 2 · Predicción",
                "Responde qué crees que pasará con la intensidad si duplicas la distancia. Al final la app comparará tu predicción con tus datos."),
            Triple(R.drawable.ic_ruler, "Paso 3 · Medición",
                "Con la fuente apagada toca \"Medir luz de fondo\". Enciende la fuente, ubícala a 10 cm apuntando al sensor, y toca \"Guardar medición\" (no muevas nada durante 2 s). Usa +5 para avanzar a 15, 20, 25, 30, 40 y 50 cm, guardando en cada una. Si aparece un aviso rojo, sigue su indicación."),
            Triple(R.drawable.ic_target, "Repetibilidad",
                "Guarda 5 mediciones a 20 cm, retirando y volviendo a ubicar la fuente cada vez. La app calculará la media y la desviación estándar."),
            Triple(R.drawable.ic_chart, "Paso 4 · Resultados",
                "Toca \"Finalizar experimento\". Revisa el exponente n, el R² y el error relativo; mira la gráfica (prueba la escala logarítmica) y la tabla. Toca Compartir para enviar el CSV. Todo queda guardado en \"Mis experimentos\".")
        )
    }

    private lateinit var b: ActivityLearnBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLearnBinding.inflate(layoutInflater)
        setContentView(b.root)
        Ui.topbar(b.topbar, getString(R.string.title_learn), null)

        b.toggleTab.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) render(if (checkedId == R.id.btnTabHow) HOW else CONCEPTS, checkedId == R.id.btnTabHow)
        }
        if (intent.getBooleanExtra(EXTRA_TAB_HOW, false)) b.toggleTab.check(R.id.btnTabHow)
        else render(CONCEPTS, false)
    }

    override fun onResume() {
        super.onResume()
        Ui.bottomNav(this, b.bottomNav, R.id.nav_learn)
    }

    private fun render(items: List<Triple<Int, String, String>>, expanded: Boolean) {
        b.containerCards.removeAllViews()
        items.forEach { (icon, title, body) ->
            val card = ItemLearnCardBinding.inflate(layoutInflater, b.containerCards, false)
            card.imgIcon.setImageResource(icon)
            card.txtTitle.text = title
            card.txtBody.text = body
            setExpanded(card, expanded)
            card.root.setOnClickListener { setExpanded(card, card.txtBody.visibility != View.VISIBLE) }
            b.containerCards.addView(card.root)
        }
    }

    private fun setExpanded(card: ItemLearnCardBinding, expanded: Boolean) {
        card.txtBody.visibility = if (expanded) View.VISIBLE else View.GONE
        card.imgChevron.rotation = if (expanded) 180f else 0f
    }
}
