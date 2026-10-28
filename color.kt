package com.example.jbrowsev1

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor // Eindeutiger Alias gegen Namenskonflikte
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch




class ColorActivity : AppCompatActivity() {

    private lateinit var colorPreview: View
    private lateinit var colorInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.color)

        colorPreview = findViewById(R.id.colorPreview)
        colorInput = findViewById(R.id.colorInput)
        val applyButton = findViewById<Button>(R.id.applyColorButton)


        lifecycleScope.launch {
            try {
                val preferences = dataStore.data.first()
                val savedColorString = preferences[USER_COLOR_KEY] ?: "#6750A4"

                val colorInt = AndroidColor.parseColor(savedColorString)
                colorPreview.setBackgroundColor(colorInt)
                colorInput.setText(savedColorString)
            } catch (e: Exception) {
                colorPreview.setBackgroundColor(AndroidColor.GRAY)
            }
        }


        applyButton.setOnClickListener {
            val hexCode = colorInput.text.toString().trim()
            val formattedHex = if (!hexCode.startsWith("#")) "#$hexCode" else hexCode

            try {

                val colorInt = AndroidColor.parseColor(formattedHex)
                colorPreview.setBackgroundColor(colorInt)


                saveColorToDataStore(formattedHex)

            } catch (e: IllegalArgumentException) {
                colorInput.error = "Wrong Hex-Code!"
                Toast.makeText(this, "Please use an real Hex-Code!", Toast.LENGTH_SHORT).show()
            }
        }


        findViewById<Button>(R.id.close).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.reset).setOnClickListener {
            val resetcolor = AndroidColor.parseColor("#800080")
            saveColorToDataStore("800080")
            colorPreview.setBackgroundColor(800080)
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
}
