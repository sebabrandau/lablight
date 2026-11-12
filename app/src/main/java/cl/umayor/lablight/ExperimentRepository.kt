package cl.umayor.lablight

import android.content.Context
import org.json.JSONArray
import java.io.File

/**
 * Guarda los experimentos en el teléfono, en un archivo JSON privado de la app
 * (files/experiments.json). Así no se pierden al cerrar la app y aparecen en
 * "Mis experimentos". No necesita internet ni permisos.
 */
class ExperimentRepository(context: Context) {

    private val file = File(context.filesDir, "experiments.json")

    fun loadAll(): MutableList<Experiment> {
        if (!file.exists()) return mutableListOf()
        return try {
            val arr = JSONArray(file.readText())
            val list = mutableListOf<Experiment>()
            for (i in 0 until arr.length()) list.add(Experiment.fromJson(arr.getJSONObject(i)))
            list.sortedByDescending { it.createdAt }.toMutableList()
        } catch (e: Exception) {
            // Archivo dañado: no bloquea la app, simplemente parte vacío
            mutableListOf()
        }
    }

    fun find(id: Long): Experiment? = loadAll().firstOrNull { it.id == id }

    /** Inserta o reemplaza (según el id). */
    fun save(exp: Experiment) {
        val list = loadAll()
        val idx = list.indexOfFirst { it.id == exp.id }
        if (idx >= 0) list[idx] = exp else list.add(exp)
        write(list)
    }

    fun delete(id: Long) {
        write(loadAll().filter { it.id != id })
    }

    private fun write(list: List<Experiment>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        file.writeText(arr.toString())
    }
}
