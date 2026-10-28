package com.example.jbrowsev1

import android.content.Context
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

val Context.dataStore by preferencesDataStore(name = "settings")
val MEIN_STRING_KEY = stringPreferencesKey("standart_suchmaschine")
val USER_COLOR_KEY = stringPreferencesKey("user_color")
val USER_URL_BAR = stringPreferencesKey("user_url_bar")

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
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        lifecycleScope.launch {
            val prefs = dataStore.data.first()
            prefs[MEIN_STRING_KEY]?.takeIf { it.startsWith("http") }?.let {
                searchEngineInput.setText(it)
            }
            urlwhere.isChecked = prefs[USER_URL_BAR] == "down"

            urlwhere.setOnCheckedChangeListener { _, isChecked ->
                saveUrlBarPosition(if (isChecked) "down" else "top")
            }
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
                // WICHTIG: eigener Key (USER_URL_BAR), nicht MEIN_STRING_KEY!
                dataStore.edit { it[USER_URL_BAR] = toporbottom }
                Toast.makeText(this@Settings, "Saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "saveUrlBarPosition failed", e)
                Toast.makeText(this@Settings, "Something went wrong...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}