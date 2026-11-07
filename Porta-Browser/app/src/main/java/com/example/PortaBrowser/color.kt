package com.example.PortaBrowser

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ColorActivity : AppCompatActivity() {
    private var selectedColorHex: String = "#448AFF"
    private var selectedColorHex2: String = "#00F0F0"
    private var selectedColorHex3: String = "#F00000"
    private var selectedtextcolorhex: String = "#FFFFFF"
    private var colorchanged: String = "False"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.color)
        val applyButton = findViewById<Button>(R.id.applyColorButton)
        val reset = findViewById<Button>(R.id.reset)
        val choosecolor = findViewById<Button>(R.id.choosecolor)
        val choosecolor2 = findViewById<Button>(R.id.choosecolor2)
        val choosetextcolor = findViewById<Button>(R.id.choosetextcolor)
        val choosecolor3 = findViewById<Button>(R.id.choosecolor3)

        choosecolor.setOnClickListener {
            colorchanged = "True"
            com.github.dhaval2404.colorpicker.ColorPickerDialog
                .Builder(this)
                .setTitle("Primary")
                .setColorListener { color, colorHex ->
                    selectedColorHex = colorHex
                    choosecolor.setBackgroundColor(color)
                    choosetextcolor.setBackgroundColor(color)
                    applyButton.setBackgroundColor(color)
                }

                .show()
        }

        choosecolor2.setOnClickListener {
            colorchanged = "True"
            com.github.dhaval2404.colorpicker.ColorPickerDialog
                .Builder(this)
                .setTitle("Secondary")
                .setColorListener { color, colorHex3 ->
                    selectedColorHex2 = colorHex3
                    choosecolor2.setBackgroundColor(color)
                    reset.setBackgroundColor(color)
                }
                .show()

        }

        choosecolor3.setOnClickListener {
            colorchanged = "True"
            com.github.dhaval2404.colorpicker.ColorPickerDialog
                .Builder(this)
                .setTitle("Accent")
                .setColorListener { color, colorHex4 ->
                    selectedColorHex3 = colorHex4
                    choosecolor3.setBackgroundColor(color)
                    findViewById<Button>(R.id.close).setBackgroundColor(color)
                }
                .show()

        }

        choosetextcolor.setOnClickListener {
            colorchanged = "True"
            com.github.dhaval2404.colorpicker.ColorPickerDialog
                .Builder(this)
                .setTitle("Text")
                .setColorListener { color, colorHex2 ->
                    selectedtextcolorhex = colorHex2
                    choosetextcolor.setTextColor(color)
                    choosecolor.setTextColor(color)
                    choosecolor2.setTextColor(color)
                    choosecolor3.setTextColor(color)
                    applyButton.setTextColor(color)
                    reset.setTextColor(color)
                    findViewById<Button>(R.id.close).setTextColor(color)
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
                val textColorInt = AndroidColor.parseColor(selectedtextcolorhex)
                findViewById<Button>(R.id.close).setBackgroundColor(colorInt3)
                choosecolor.setBackgroundColor(colorInt)
                choosetextcolor.setBackgroundColor(colorInt)
                choosetextcolor.setTextColor(textColorInt)
                choosecolor2.setBackgroundColor(colorInt2)
                choosecolor3.setBackgroundColor(colorInt3)
                reset.setBackgroundColor(colorInt)
                applyButton.setBackgroundColor(colorInt2)
                findViewById<Button>(R.id.close).setTextColor(textColorInt)
                choosecolor.setTextColor(textColorInt)
                choosecolor2.setTextColor(textColorInt)
                choosecolor3.setTextColor(textColorInt)
                reset.setTextColor(textColorInt)
                applyButton.setTextColor(textColorInt)
            } catch (e: Exception) {
            }
        }
        applyButton.setOnClickListener {
            apply()
        }
        reset.setOnClickListener {
            colorchanged = "True"
            val defaultHex = "#448AFF"
            val defaulttextHex = "#000000"
            val defaultHex2 = "#00F0F0"
            val defaultHex3 = "#F00000"
            selectedColorHex = defaultHex
            selectedColorHex2 = defaultHex2
            selectedColorHex3 = defaultHex3
            selectedtextcolorhex = defaulttextHex
            val textcolorInt = AndroidColor.parseColor(defaulttextHex)
            val colorInt = AndroidColor.parseColor(defaultHex)
            val colorInt2 = AndroidColor.parseColor(defaultHex2)
            val colorInt3 = AndroidColor.parseColor(defaultHex3)
            choosetextcolor.setTextColor(textcolorInt)
            findViewById<Button>(R.id.close).setBackgroundColor(colorInt3)
            applyButton.setBackgroundColor(colorInt2)
            reset.setBackgroundColor(colorInt)
            choosetextcolor.setBackgroundColor(colorInt)
            choosecolor.setBackgroundColor(colorInt)
            choosecolor2.setBackgroundColor(colorInt2)
            choosecolor3.setBackgroundColor(colorInt3)
            saveColorToDataStore(defaultHex)
            saveTextColorToDataStore(defaulttextHex)
            choosecolor.setTextColor(textcolorInt)
            choosecolor2.setTextColor(textcolorInt)
            choosecolor3.setTextColor(textcolorInt)
            applyButton.setTextColor(textcolorInt)
            reset.setTextColor(textcolorInt)
            findViewById<Button>(R.id.close).setTextColor(textcolorInt)
        }
        findViewById<Button>(R.id.close).setOnClickListener {
            apply()
            if (colorchanged == "True") {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                finish()
            }
        }
    }
    private fun apply() {
        try {
            saveTextColorToDataStore(selectedtextcolorhex)
            saveColorToDataStore(selectedColorHex)
            saveColor2ToDataStore(selectedColorHex2)
            saveColor3ToDataStore(selectedColorHex3)

        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, getString(R.string.internalservice) + "apply" + getString(R.string.failed), Toast.LENGTH_SHORT).show()
        }
    }
    private fun saveColorToDataStore(color: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[USER_COLOR_KEY] = color
                }
            } catch (e: Exception) {
                Toast.makeText(this@ColorActivity, getString(R.string.internalservice) + "saveColorToDataStore" + getString(R.string.failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveColor2ToDataStore(color: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[USER_COLOR2_KEY] = color
                }
            } catch (e: Exception) {
                Toast.makeText(this@ColorActivity, getString(R.string.internalservice) + "saveColor2ToDataStore" + getString(R.string.failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveColor3ToDataStore(color: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[USER_COLOR3_KEY] = color
                }
                Toast.makeText(this@ColorActivity, getString(R.string.saved), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ColorActivity, getString(R.string.internalservice) + "saveColor3ToDataStore" + getString(R.string.failed), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@ColorActivity, getString(R.string.internalservice) + "saveTextColortoDataStore" + getString(R.string.failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
