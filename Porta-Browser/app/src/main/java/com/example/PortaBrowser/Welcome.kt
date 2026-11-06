package com.example.PortaBrowser

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.content.ContentValues.TAG
import kotlinx.coroutines.flow.first

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

        val toporbottomswitch = findViewById<SwitchCompat>(R.id.welcomeurltobottom)


        val startporta = findViewById<Slider>(R.id.startporta)
        startporta.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (slider.value < 1.0f) {
                    animatesliderback(slider)
                } else {
                    if (toporbottomswitch.isChecked) {
                        saveUrlBarPosition("down")
                    } else {
                        saveUrlBarPosition("top")
                    }
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
    private fun saveUrlBarPosition(toporbottom: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { it[USER_URL_BAR] = toporbottom }
                Toast.makeText(this@Welcome, "Saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Internal Service: saveUrlBarPosition failed", e)
                Toast.makeText(this@Welcome, "Something went wrong...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
