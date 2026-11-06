package com.example.PortaBrowser

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.graphics.Color as AndroidColor
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
import android.hardware.input.InputManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.flow.first
import android.app.Activity
import android.graphics.Color
import androidx.core.view.WindowInsetsControllerCompat

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
        val urltextedit = findViewById<EditText>(R.id.searchengine)
        urltextedit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                hideKeyboard()
                saveSearchEngine(urltextedit.text.toString())
                true
            } else false
        }



        val startporta = findViewById<Slider>(R.id.startporta)
        startporta.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (slider.value < 1.0f) {
                    animatesliderback(slider)
                } else {
                    val defaultHex = "#448AFF"
                    val defaulttextHex = "#FFFFFF"
                    val defaultHex2 = "#00F0F0"
                    val defaultHex3 = "#F00000"
                    saveColorToDataStore(defaultHex)
                    saveColor2ToDataStore(defaultHex2)
                    saveColor3ToDataStore(defaultHex3)
                    saveTextColorToDataStore(defaulttextHex)
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

    private fun saveColorToDataStore(color: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[USER_COLOR_KEY] = color
                }
                Toast.makeText(this@Welcome, "Saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@Welcome, "Internal service: saveColorToDataStore failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveColor2ToDataStore(color: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[USER_COLOR2_KEY] = color
                }
                Toast.makeText(this@Welcome, "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@Welcome, "Internal service: saveColor2ToDataStore failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveColor3ToDataStore(color: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[USER_COLOR3_KEY] = color
                }
                Toast.makeText(this@Welcome, "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@Welcome, "Internal service: saveColor3ToDataStore failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveTextColorToDataStore(textcolor : String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[USER_TEXT_COLOR_THEME] = textcolor
                }

            } catch (e: Exception) {
                Toast.makeText(this@Welcome, "Internal service: saveTextColorToDataStore failed", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun normalizeUrl(input: String): String {
        var url = input.trim().trimEnd('/')
        if (url.isNotEmpty() && !url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        return url
    }

    private fun saveSearchEngine(input: String) {
        val url = normalizeUrl(input)
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            try {
                dataStore.edit { it[MEIN_STRING_KEY] = url }
                Toast.makeText(this@Welcome, "Search engine saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@Welcome, "Something went wrong...", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun Activity.hideKeyboard() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.ime())
    }


}
