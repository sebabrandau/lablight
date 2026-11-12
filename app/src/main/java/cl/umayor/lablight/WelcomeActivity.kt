package cl.umayor.lablight

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cl.umayor.lablight.databinding.ActivityWelcomeBinding

/** Pantalla de bienvenida: presenta el laboratorio y lleva al inicio o a las instrucciones. */
class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnStart.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        b.btnInstructions.setOnClickListener {
            startActivity(
                Intent(this, LearnActivity::class.java).putExtra(LearnActivity.EXTRA_TAB_HOW, true)
            )
        }
    }
}
