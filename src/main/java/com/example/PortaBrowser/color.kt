package com.example.PortaBrowser

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ColorActivity : AppCompatActivity() {
    private lateinit var colorPreview: View
    private var selectedColorHex: String = "#6750A4"
    private var selectedtextcolorhex: String = "#FFFFFF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.color)

        colorPreview = findViewById(R.id.colorPreview)
        val applyButton = findViewById<Button>(R.id.applyColorButton)
        val reset = findViewById<Button>(R.id.reset)
        val choosecolor = findViewById<Button>(R.id.choosecolor)
        val choosetextcolor = findViewById<Button>(R.id.choosetextcolor)

        choosecolor.setOnClickListener {
            com.github.dhaval2404.colorpicker.ColorPickerDialog
                .Builder(this)
                .setTitle("Buttons")
                .setColorListener { color, colorHex ->
                    selectedColorHex = colorHex
                    colorPreview.setBackgroundColor(color)
                }
                .show()
        }

        choosetextcolor.setOnClickListener {
            com.github.dhaval2404.colorpicker.ColorPickerDialog
                .Builder(this)
                .setTitle("Text")
                .setColorListener { color, colorHex2 ->
                    selectedtextcolorhex = colorHex2
                }
                .show()
        }


        lifecycleScope.launch {
            try {
                val preferences = dataStore.data.first()
                selectedColorHex = preferences[USER_COLOR_KEY] ?: "#448AFF"
                selectedtextcolorhex = preferences[USER_TEXT_COLOR_THEME] ?: "#FFFFFF"

                val colorInt = AndroidColor.parseColor(selectedColorHex)
                colorPreview.setBackgroundColor(colorInt)
            } catch (e: Exception) {
                colorPreview.setBackgroundColor(AndroidColor.GRAY)
            }
        }

        applyButton.setOnClickListener {
            try {
                val colorInt = AndroidColor.parseColor(selectedColorHex)
                colorPreview.setBackgroundColor(colorInt)
                saveTextColorToDataStore(selectedtextcolorhex)
                saveColorToDataStore(selectedColorHex)

            } catch (e: IllegalArgumentException) {
                Toast.makeText(this, "Sorry! But Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }

        reset.setOnClickListener {
            val defaultHex = "#448AFF"
            val defaulttextHex = "#FFFFFF"
            selectedColorHex = defaultHex
            selectedtextcolorhex = defaulttextHex
            val colorInt = AndroidColor.parseColor(defaultHex)
            colorPreview.setBackgroundColor(colorInt)
            saveColorToDataStore(defaultHex)
            saveTextColorToDataStore(defaulttextHex)
        }

        findViewById<Button>(R.id.close).setOnClickListener {
            finish()
        }
    }

    private fun saveColorToDataStore(color: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[USER_COLOR_KEY] = color
                }
                Toast.makeText(this@ColorActivity, "Color saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ColorActivity, "Something went wrong...", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@ColorActivity, "Something went wrong...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
