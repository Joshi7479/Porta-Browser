package com.example.PortaBrowser

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor
import com.google.android.material.slider.Slider

val Context.dataStore by preferencesDataStore(name = "settings")
val MEIN_STRING_KEY = stringPreferencesKey("standart_suchmaschine")
val USER_COLOR_KEY = stringPreferencesKey("user_color")
val USER_URL_BAR = stringPreferencesKey("user_url_bar")
val USER_TEXT_COLOR_THEME = stringPreferencesKey("user_text_color_theme")
val USER_COLOR2_KEY = stringPreferencesKey("user_color2")
val USER_COLOR3_KEY = stringPreferencesKey("user_color3")

class Settings : AppCompatActivity() {

    companion object {
        private const val TAG = "Settings"
        private const val SEARX = "https://searx.tiekoetter.com"
        private const val DDG = "https://duckduckgo.com"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        val searchEngineInput = findViewById<EditText>(R.id.test)
        val urlwhere = findViewById<SwitchCompat>(R.id.urlwhere)
        val rerunsetup = findViewById<Button>(R.id.rerunsetup)
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        lifecycleScope.launch {
            val prefs = dataStore.data.first()
            prefs[MEIN_STRING_KEY]?.takeIf { it.startsWith("http") }?.let {
                searchEngineInput.setText(it)
            }
            val selectedColorHex = prefs[USER_COLOR_KEY] ?: "#448AFF"
            val selectedtextcolorhex = prefs[USER_TEXT_COLOR_THEME] ?: "#FFFFFF"
            val selectedColorHex2 = prefs[USER_COLOR2_KEY] ?: "#00F0F0"
            val colorInt = AndroidColor.parseColor(selectedColorHex)
            val colorInt2 = AndroidColor.parseColor(selectedColorHex2)
            val TextColorInt = AndroidColor.parseColor(selectedtextcolorhex)
            rerunsetup.setBackgroundColor(colorInt)
            findViewById<Button>(R.id.save).setBackgroundColor(colorInt2)
            findViewById<Button>(R.id.keyboard).setBackgroundColor(colorInt2)
            findViewById<Button>(R.id.reset).setBackgroundColor(colorInt)
            findViewById<Button>(R.id.ddg).setBackgroundColor(colorInt)
            findViewById<Button>(R.id.color).setTextColor(TextColorInt)
            findViewById<SwitchCompat>(R.id.urlwhere).setTextColor(TextColorInt)
            findViewById<Button>(R.id.save).setTextColor(TextColorInt)
            findViewById<Button>(R.id.keyboard).setTextColor(TextColorInt)
            findViewById<Button>(R.id.reset).setTextColor(TextColorInt)
            findViewById<Button>(R.id.ddg).setTextColor(TextColorInt)
            findViewById<Button>(R.id.keyboard).setTextColor(TextColorInt)
            rerunsetup.setTextColor(TextColorInt)
            urlwhere.isChecked = prefs[USER_URL_BAR] == "down"

            urlwhere.setOnCheckedChangeListener { _, isChecked ->
                saveUrlBarPosition(if (isChecked) "down" else "top")
            }
        }
        rerunsetup.setOnClickListener {
            startActivity(Intent(this, Welcome::class.java))
        }
        searchEngineInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                saveSearchEngine(searchEngineInput.text.toString())
                true
            } else false
        }
        findViewById<Button>(R.id.save).setOnClickListener {
            saveSearchEngine(searchEngineInput.text.toString())
        }
        findViewById<Button>(R.id.reset).setOnClickListener {
            searchEngineInput.setText(SEARX)
            saveSearchEngine(SEARX)
        }
        findViewById<Button>(R.id.ddg).setOnClickListener {
            searchEngineInput.setText(DDG)
            saveSearchEngine(DDG)
        }

        findViewById<Button>(R.id.close).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.color).setOnClickListener {
            startActivity(android.content.Intent(this, ColorActivity::class.java))
        }


        findViewById<Button>(R.id.keyboard).setOnClickListener {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
        lifecycleScope.launch {
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
                Toast.makeText(this@Settings, "Search engine saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "saveSearchEngine failed", e)
                Toast.makeText(this@Settings, "Something went wrong...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUrlBarPosition(toporbottom: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { it[USER_URL_BAR] = toporbottom }
                Toast.makeText(this@Settings, "Saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "saveUrlBarPosition failed", e)
                Toast.makeText(this@Settings, "Something went wrong...", Toast.LENGTH_SHORT).show()
            }
        }
    }

}