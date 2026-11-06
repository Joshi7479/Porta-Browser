package com.example.PortaBrowser

import android.content.Intent
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
    private var selectedColorHex: String = "#448AFF"
    private var selectedColorHex2: String = "#00F0F0"
    private var selectedColorHex3: String = "#F00000"
    private var selectedtextcolorhex: String = "#FFFFFF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.color)

        colorPreview = findViewById(R.id.colorPreview)
        val applyButton = findViewById<Button>(R.id.applyColorButton)
        val reset = findViewById<Button>(R.id.reset)
        val choosecolor = findViewById<Button>(R.id.choosecolor)
        val choosecolor2 = findViewById<Button>(R.id.choosecolor2)
        val choosetextcolor = findViewById<Button>(R.id.choosetextcolor)
        val choosecolor3 = findViewById<Button>(R.id.choosecolor3)

        choosecolor.setOnClickListener {
            com.github.dhaval2404.colorpicker.ColorPickerDialog
                .Builder(this)
                .setTitle("Primary")
                .setColorListener { color, colorHex ->
                    selectedColorHex = colorHex
                    colorPreview.setBackgroundColor(color)
                    choosecolor.setBackgroundColor(color)
                    choosetextcolor.setBackgroundColor(color)
                }

                .show()
        }

        choosecolor2.setOnClickListener {
            com.github.dhaval2404.colorpicker.ColorPickerDialog
                .Builder(this)
                .setTitle("Secondary")
                .setColorListener { color, colorHex3 ->
                    selectedColorHex2 = colorHex3
                    choosecolor2.setBackgroundColor(color)
                }
                .show()

        }

        choosecolor3.setOnClickListener {
            com.github.dhaval2404.colorpicker.ColorPickerDialog
                .Builder(this)
                .setTitle("Accent")
                .setColorListener { color, colorHex4 ->
                    selectedColorHex3 = colorHex4
                    choosecolor3.setBackgroundColor(color)
                }
                .show()

        }

        choosetextcolor.setOnClickListener {
            com.github.dhaval2404.colorpicker.ColorPickerDialog
                .Builder(this)
                .setTitle("Text")
                .setColorListener { color, colorHex2 ->
                    selectedtextcolorhex = colorHex2
                    choosetextcolor.setTextColor(color)
                }
                .show()
        }


        lifecycleScope.launch {
            try {
                val preferences = dataStore.data.first()
                selectedColorHex = preferences[USER_COLOR_KEY] ?: "#448AFF"
                selectedtextcolorhex = preferences[USER_TEXT_COLOR_THEME] ?: "#FFFFFF"
                selectedColorHex2 = preferences[USER_COLOR2_KEY] ?: "#00F0F0"
                selectedColorHex3 = preferences[USER_COLOR3_KEY] ?: "#F00000"

                val colorInt = AndroidColor.parseColor(selectedColorHex)
                val colorInt2 = AndroidColor.parseColor(selectedColorHex2)
                val colorInt3 = AndroidColor.parseColor(selectedColorHex3)
                val TextColorInt = AndroidColor.parseColor(selectedtextcolorhex)
                findViewById<Button>(R.id.close).setBackgroundColor(colorInt3)
                choosecolor.setBackgroundColor(colorInt)
                choosetextcolor.setBackgroundColor(colorInt)
                choosetextcolor.setTextColor(TextColorInt)
                choosecolor2.setBackgroundColor(colorInt2)
                choosecolor3.setBackgroundColor(colorInt3)
                reset.setBackgroundColor(colorInt2)
                applyButton.setBackgroundColor(colorInt)
                colorPreview.setBackgroundColor(colorInt)
            } catch (e: Exception) {
                colorPreview.setBackgroundColor(AndroidColor.BLUE)
            }
        }

        applyButton.setOnClickListener {
            apply()
        }

        reset.setOnClickListener {
            val defaultHex = "#448AFF"
            val defaulttextHex = "#000000"
            val defaultHex2 = "#00F0F0"
            val defaultHex3 = "#F00000"
            selectedColorHex = defaultHex
            selectedColorHex2 = defaultHex2
            selectedColorHex3 = defaultHex3
            selectedtextcolorhex = defaulttextHex
            val TextcolorInt = AndroidColor.parseColor(defaulttextHex)
            val colorInt = AndroidColor.parseColor(defaultHex)
            val colorInt2 = AndroidColor.parseColor(defaultHex2)
            val colorInt3 = AndroidColor.parseColor(defaultHex3)
            choosetextcolor.setTextColor(TextcolorInt)
            findViewById<Button>(R.id.close).setBackgroundColor(colorInt3)
            colorPreview.setBackgroundColor(colorInt)
            applyButton.setBackgroundColor(colorInt2)
            reset.setBackgroundColor(colorInt)
            choosetextcolor.setBackgroundColor(colorInt)
            choosecolor.setBackgroundColor(colorInt)
            choosecolor2.setBackgroundColor(colorInt2)
            choosecolor3.setBackgroundColor(colorInt3)
            saveColorToDataStore(defaultHex)
            saveTextColorToDataStore(defaulttextHex)
        }

        findViewById<Button>(R.id.close).setOnClickListener {
            apply()
            startActivity(Intent(this, Settings::class.java))
        }
    }

    private fun apply() {
        try {
            val colorInt = AndroidColor.parseColor(selectedColorHex)
            colorPreview.setBackgroundColor(colorInt)
            saveTextColorToDataStore(selectedtextcolorhex)
            saveColorToDataStore(selectedColorHex)
            saveColor2ToDataStore(selectedColorHex2)
            saveColor3ToDataStore(selectedColorHex3)

        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, "Sorry! But Something went wrong", Toast.LENGTH_SHORT).show()
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
    private fun saveColor2ToDataStore(color: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[USER_COLOR2_KEY] = color
                }
                Toast.makeText(this@ColorActivity, "Color saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ColorActivity, "Something went wrong...", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveColor3ToDataStore(color: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[USER_COLOR3_KEY] = color
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
