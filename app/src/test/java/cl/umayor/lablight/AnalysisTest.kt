package cl.umayor.lablight

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.pow

/**
 * Pruebas automáticas del motor de datos.
 * En Android Studio: clic derecho sobre este archivo → Run 'AnalysisTest'.
 */
class AnalysisTest {

    private val d = listOf(10.0, 20.0, 30.0, 40.0, 50.0)

    @Test
    fun inversoDelCuadradoPerfecto_daN2() {
        val i = d.map { 100_000.0 / (it * it) }
        val fit = Analysis.fitPowerLaw(d, i)!!
        assertEquals(2.0, fit.exponentN, 1e-9)
        assertEquals(100_000.0, fit.k, 1e-6)
        assertEquals(1.0, fit.r2, 1e-9)
        assertEquals(5, fit.pointsUsed)
    }

    @Test
    fun otroExponente_seRecupera() {
        val i = d.map { 5_000.0 / it.pow(1.5) }
        assertEquals(1.5, Analysis.fitPowerLaw(d, i)!!.exponentN, 1e-9)
    }

    @Test
    fun puntosConIntensidadCero_seIgnoran() {
        val i = listOf(1000.0, 250.0, 0.0, 62.5, 0.0)
        val fit = Analysis.fitPowerLaw(d, i)!!
        assertEquals(3, fit.pointsUsed)
        assertEquals(2.0, fit.exponentN, 1e-9)
    }

    @Test
    fun mismaDistancia_noSePuedeAjustar() {
        assertNull(Analysis.fitPowerLaw(listOf(20.0, 20.0, 20.0), listOf(5.0, 6.0, 7.0)))
    }

    @Test
    fun constanteInversoCuadrado() {
        val i = d.map { 100_000.0 / (it * it) }
        assertEquals(100_000.0, Analysis.fitInverseSquare(d, i)!!, 1e-6)
    }

    @Test
    fun desviacionEstandarMuestral() {
        val xs = listOf(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0)
        assertEquals(5.0, Analysis.mean(xs), 1e-12)
        assertEquals(2.13809, Analysis.std(xs), 1e-5)
        assertEquals(0.0, Analysis.std(listOf(3.0)), 0.0)
    }

    @Test
    fun correccionPorFondo_nuncaNegativa() {
        assertEquals(95.0, Analysis.corrected(100.0, 5.0), 1e-12)
        assertEquals(0.0, Analysis.corrected(3.0, 5.0), 1e-12)
        assertEquals(100.0, Analysis.corrected(100.0, null), 1e-12)
    }

    @Test
    fun errorRelativo() {
        assertEquals(5.0, Analysis.relativeErrorPercent(2.1, 2.0), 1e-9)
    }

    @Test
    fun repetibilidad_agrupaPorDistancia() {
        val dist = listOf(10.0, 20.0, 20.0, 20.0, 30.0)
        val e = listOf(900.0, 240.0, 250.0, 260.0, 110.0)
        val stats = Analysis.repeatStats(dist, e)
        assertEquals(1, stats.size)
        assertEquals(20.0, stats[0].distanceCm, 0.0)
        assertEquals(3, stats[0].count)
        assertEquals(250.0, stats[0].mean, 1e-9)
        assertEquals(10.0, stats[0].std, 1e-9)
        assertEquals(4.0, stats[0].cvPercent, 1e-9)
    }
}
