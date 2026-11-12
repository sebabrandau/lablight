package cl.umayor.lablight

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cl.umayor.lablight.databinding.ActivityHomeBinding
import cl.umayor.lablight.databinding.ItemMenuCardBinding

/** Inicio: "¿Qué quieres hacer hoy?" con las cuatro secciones de la app. */
class HomeActivity : AppCompatActivity() {

    private lateinit var b: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(b.root)
        Ui.topbar(b.topbar, getString(R.string.app_name), null)

        card(b.cardNew, R.drawable.ic_target, R.string.menu_new_title, R.string.menu_new_sub) {
            startActivity(Intent(this, SetupActivity::class.java))
        }
        card(b.cardHistory, R.drawable.ic_chart, R.string.menu_history_title, R.string.menu_history_sub) {
            b.bottomNav.selectedItemId = R.id.nav_history
        }
        card(b.cardLearn, R.drawable.ic_book, R.string.menu_learn_title, R.string.menu_learn_sub) {
            b.bottomNav.selectedItemId = R.id.nav_learn
        }
        card(b.cardAbout, R.drawable.ic_info, R.string.menu_about_title, R.string.menu_about_sub) {
            b.bottomNav.selectedItemId = R.id.nav_about
        }
    }

    override fun onResume() {
        super.onResume()
        Ui.bottomNav(this, b.bottomNav, R.id.nav_home)
    }

    private fun card(c: ItemMenuCardBinding, icon: Int, title: Int, sub: Int, onClick: () -> Unit) {
        c.imgIcon.setImageResource(icon)
        c.txtTitle.setText(title)
        c.txtSubtitle.setText(sub)
        c.root.setOnClickListener { onClick() }
    }
}
