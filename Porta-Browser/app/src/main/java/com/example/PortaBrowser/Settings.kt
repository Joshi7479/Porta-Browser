package com.example.PortaBrowser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

val Context.dataStore by preferencesDataStore(name = "settings")
val MEIN_STRING_KEY = stringPreferencesKey("standart_suchmaschine")
val USER_COLOR_KEY = stringPreferencesKey("user_color")
val USER_URL_BAR = stringPreferencesKey("user_url_bar")
val USER_TEXT_COLOR_THEME = stringPreferencesKey("user_text_color_theme")
val USER_COLOR2_KEY = stringPreferencesKey("user_color2")
val USER_COLOR3_KEY = stringPreferencesKey("user_color3")
val USER_UBLOCK_KEY = stringPreferencesKey("ublock")

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
        val useublock = findViewById<SwitchCompat>(R.id.useublock)
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
            val accentcolor = prefs[USER_COLOR3_KEY] ?: "#F00000"
            val colorInt = AndroidColor.parseColor(selectedColorHex)
            val colorInt2 = AndroidColor.parseColor(selectedColorHex2)
            val TextColorInt = AndroidColor.parseColor(selectedtextcolorhex)
            val colorInt3 = AndroidColor.parseColor(accentcolor)
            rerunsetup.setBackgroundColor(colorInt)
            findViewById<TextView>(R.id.settingstitle).setTextColor(colorInt3)
            findViewById<Button>(R.id.save).setBackgroundColor(colorInt2)
            findViewById<Button>(R.id.keyboard).setBackgroundColor(colorInt2)
            findViewById<Button>(R.id.reset).setBackgroundColor(colorInt)
            findViewById<Button>(R.id.ddg).setBackgroundColor(colorInt)
            findViewById<Button>(R.id.color).setBackgroundColor(colorInt)
            findViewById<Button>(R.id.color).setTextColor(TextColorInt)
            useublock.setTextColor(TextColorInt)
            useublock.thumbTintList = ColorStateList.valueOf(colorInt3)
            useublock.trackTintList = ColorStateList.valueOf(colorInt)
            findViewById<SwitchCompat>(R.id.urlwhere).setTextColor(colorInt2)
            findViewById<Button>(R.id.save).setTextColor(TextColorInt)
            findViewById<Button>(R.id.keyboard).setTextColor(TextColorInt)
            findViewById<Button>(R.id.reset).setTextColor(TextColorInt)
            findViewById<Button>(R.id.ddg).setTextColor(TextColorInt)
            findViewById<Button>(R.id.keyboard).setTextColor(TextColorInt)
            findViewById<Button>(R.id.close).setTextColor(TextColorInt)
            findViewById<Button>(R.id.close).setBackgroundColor(colorInt3)
            findViewById<TextView>(R.id.madebyme).setTextColor(colorInt3)
            findViewById<SwitchCompat>(R.id.urlwhere).thumbTintList = ColorStateList.valueOf(colorInt3)
            findViewById<SwitchCompat>(R.id.urlwhere).trackTintList = ColorStateList.valueOf(colorInt)
            findViewById<EditText>(R.id.test).backgroundTintList = ColorStateList.valueOf(colorInt3)
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
                hideKeyboard()
                saveSearchEngine(searchEngineInput.text.toString())
                true
            } else false
        }
        searchEngineInput.setOnClickListener {
            searchEngineInput.setText("")
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

        useublock.setOnCheckedChangeListener { _, isChecked ->
            useublock(if (isChecked) "of" else "on")
        }
        findViewById<Button>(R.id.color).setOnClickListener {
            startActivity(android.content.Intent(this, ColorActivity::class.java))
        }
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
            Toast.makeText(this, getString(R.string.nourl), Toast.LENGTH_SHORT).show()
            return
        }
        if (url == "https://google.com") {
            Toast.makeText(this, getString(R.string.reallygoogle), Toast.LENGTH_SHORT).show()
        }
        if (url == "https://google.de") {
            Toast.makeText(this, getString(R.string.reallygoogle), Toast.LENGTH_SHORT).show()
        }
        lifecycleScope.launch {
            try {
                dataStore.edit { it[MEIN_STRING_KEY] = url }
                Toast.makeText(this@Settings, getString(R.string.saved), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "saveSearchEngine failed", e)
                Toast.makeText(this@Settings, getString(R.string.internalservice) + "saveSearchEngine" + getString(R.string.failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveUrlBarPosition(toporbottom: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { it[USER_URL_BAR] = toporbottom }
                Toast.makeText(this@Settings, getString(R.string.saved), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "saveUrlBarPosition failed", e)
                Toast.makeText(this@Settings, getString(R.string.internalservice) + "saveUrlBarPosition" + getString(R.string.failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun useublock(onorof: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { it[USER_UBLOCK_KEY] = onorof }
                Toast.makeText(this@Settings, getString(R.string.saved), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "useublock failed", e)
                Toast.makeText(this@Settings, getString(R.string.internalservice) + "useublock" + getString(R.string.failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun Activity.hideKeyboard() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.ime())
    }
}