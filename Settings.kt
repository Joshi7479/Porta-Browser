package com.example.jbrowsev1

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.view.inputmethod.InputMethodManager



val Context.dataStore by preferencesDataStore(name = "settings")
val MEIN_STRING_KEY = stringPreferencesKey("standart_suchmaschine")
val USER_COLOR_KEY = stringPreferencesKey("user_color")

class Settings : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        val standart_suchmaschine = findViewById<EditText>(R.id.test)
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        standart_suchmaschine.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                saveSearchEngine(standart_suchmaschine.text.toString())
                true
            } else false
        }
        findViewById<Button>(R.id.save).setOnClickListener {
            saveSearchEngine(standart_suchmaschine.text.toString())
        }
        findViewById<Button>(R.id.reset).setOnClickListener {
            saveSearchEngine("https://www.searx.tiekoetter.com")
        }

        findViewById<Button>(R.id.ddg).setOnClickListener {
            saveSearchEngine("https://www.duckduckgo.com")
        }

        findViewById<Button>(R.id.close).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.color).setOnClickListener {

            val intent = Intent(this, ColorActivity::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.keyboard).setOnClickListener {

            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        }

    }

    private fun saveSearchEngine(url: String) {
        lifecycleScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[MEIN_STRING_KEY] = url
                }
                Toast.makeText(
                    this@Settings,
                    "Search engine saved!",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(this@Settings, "Something went wrong...", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
