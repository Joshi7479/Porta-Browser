package com.example.PortaBrowser

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import android.widget.Button

class Welcome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome)

        val portalogo = findViewById<ImageView>(R.id.portalogo)
        val portaschriftzug = findViewById<TextView>(R.id.portaschriftzug)

        portalogo.setOnClickListener {
            portalogo.visibility = View.GONE
            portaschriftzug.visibility = View.VISIBLE
        }
        portaschriftzug.setOnClickListener {
            portaschriftzug.visibility = View.GONE
            portalogo.visibility = View.VISIBLE
        }

        val welcomecolor = findViewById<Button>(R.id.welcomecolor)
        welcomecolor.setOnClickListener{
            startActivity(android.content.Intent(this, ColorActivity::class.java))
        }

        val startporta = findViewById<Slider>(R.id.startporta)
        startporta.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (slider.value < 1.0f) {
                    animatesliderback(slider)
                } else {
                    val intent = Intent(this@Welcome, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        })
    }

    private fun animatesliderback(slider: Slider) {
        val animator = ValueAnimator.ofFloat(slider.value, 0.0f)
        animator.duration = 300

        animator.addUpdateListener { animation ->
            slider.value = animation.animatedValue as Float
        }
        animator.start()
    }
}
