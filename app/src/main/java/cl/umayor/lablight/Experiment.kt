package cl.umayor.lablight

import org.json.JSONArray
import org.json.JSONObject

/** De dónde vienen los datos: el sensor real del celular o el motor de simulación. */
enum class SourceMode { SENSOR, SIMULATION }

/**
 * Una medición guardada: a cierta distancia, el promedio y la desviación
 * estándar de las lecturas tomadas durante la ventana de muestreo (2 s).
 */
data class Measurement(
    val distanceCm: Double,
    val meanLux: Double,
    val stdLux: Double,
    val samples: Int
) {
    fun toJson(): JSONObject = JSONObject()
        .put("d", distanceCm).put("mean", meanLux).put("std", stdLux).put("n", samples)

    companion object {
        fun fromJson(o: JSONObject) = Measurement(
            o.getDouble("d"), o.getDouble("mean"), o.getDouble("std"), o.getInt("n")
        )
    }
}

/** Un experimento completo: configuración (paso 1), predicción (paso 2) y mediciones (paso 3). */
class Experiment(
    val id: Long,
    var name: String,
    var lightSource: String,
    var mode: SourceMode,
    /** Solo simulación: iluminancia que produce la fuente simulada a 10 cm (lux). */
    var simPowerLux10cm: Double,
    val createdAt: Long,
    /** Luz de fondo medida con la fuente apagada (null = no medida). */
    var backgroundLux: Double? = null,
    /** Índice de la respuesta elegida en el paso 2 (ver [Prediction]); null = sin responder. */
    var prediction: Int? = null,
    /** Última distancia usada, para proponerla en la siguiente medición. */
    var lastDistanceCm: Double = 10.0,
    val measurements: MutableList<Measurement> = mutableListOf()
) {
    fun toJson(): JSONObject {
        val o = JSONObject()
            .put("id", id).put("name", name).put("source", lightSource)
            .put("mode", mode.name).put("simPower", simPowerLux10cm)
            .put("createdAt", createdAt).put("lastD", lastDistanceCm)
        backgroundLux?.let { o.put("bg", it) }
        prediction?.let { o.put("prediction", it) }
        val arr = JSONArray()
        measurements.forEach { arr.put(it.toJson()) }
        o.put("measurements", arr)
        return o
    }

    companion object {
        fun fromJson(o: JSONObject): Experiment {
            val exp = Experiment(
                id = o.getLong("id"),
                name = o.optString("name", "Experimento"),
                lightSource = o.optString("source", ""),
                mode = if (o.optString("mode") == SourceMode.SIMULATION.name) SourceMode.SIMULATION else SourceMode.SENSOR,
                simPowerLux10cm = o.optDouble("simPower", 5000.0),
                createdAt = o.optLong("createdAt", o.getLong("id")),
                backgroundLux = if (o.has("bg") && !o.isNull("bg")) o.getDouble("bg") else null,
                prediction = if (o.has("prediction") && !o.isNull("prediction")) o.getInt("prediction") else null,
                lastDistanceCm = o.optDouble("lastD", 10.0)
            )
            val arr = o.optJSONArray("measurements") ?: JSONArray()
            for (i in 0 until arr.length()) exp.measurements.add(Measurement.fromJson(arr.getJSONObject(i)))
            return exp
        }
    }
}

/** Experimento en curso, compartido por las pantallas del flujo (pasos 1 a 4). */
object ExperimentStore {
    var current: Experiment? = null

    fun startNew(name: String, lightSource: String, mode: SourceMode, simPower: Double): Experiment {
        val now = System.currentTimeMillis()
        val exp = Experiment(now, name, lightSource, mode, simPower, now)
        current = exp
        return exp
    }
}

/**
 * Respuestas posibles de la predicción del paso 2:
 * "Si la distancia se duplica, la intensidad…".
 * [expectedN] es el exponente n que implica cada respuesta (I ∝ 1/dⁿ).
 */
object Prediction {
    val options = listOf(
        "Se mantiene igual",
        "Se reduce a la mitad",
        "Se reduce a la cuarta parte",
        "Se reduce a la octava parte",
        "No estoy seguro/a"
    )
    val expectedN: List<Double?> = listOf(0.0, 1.0, 2.0, 3.0, null)
}
