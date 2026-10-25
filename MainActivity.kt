package com.example.jbrowsev1

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.content.Context
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private var standart_suchmaschine_url = "https://searx.tiekoetter.com"

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var tabBar: TabLayout
    private lateinit var tablerow: View
    private lateinit var alleButtons: List<Button>

    private class BrowserTab(
        var url: String,
        var title: String = "Neu",
        var state: Bundle? = null
    )

    private val tabs = mutableListOf<BrowserTab>()
    private var currentIndex = -1
    private var ignoreSelection = false





    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isFirstRun = sharedPref.getBoolean("isFirstRun", true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        if (isFirstRun) {
            startActivity(Intent(this, Welcome::class.java))


            sharedPref.edit().putBoolean("isFirstRun", false).apply()
        }


        webView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)
        progressBar = findViewById(R.id.progressBar)
        tabBar = findViewById(R.id.tabbar)
        tablerow = findViewById(R.id.tablerow)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, urlnow: String?, favicon: Bitmap?) {
                super.onPageStarted(view, urlnow, favicon)
                urlBar.setText(urlnow ?: "")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val tab = tabs.getOrNull(currentIndex) ?: return
                if (url != null) tab.url = url
                tab.title = (view?.title ?: "").ifBlank { "Neu" }.take(12)
                tabBar.getTabAt(currentIndex)?.text = tab.title
            }
        }

        urlBar.setOnClickListener {
            urlBar.setText("")
        }
        urlBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadInput(urlBar.text.toString())
                true
            } else false
        }

        val buttonBack = findViewById<Button>(R.id.btnBack)
        val buttonReload = findViewById<Button>(R.id.btnReload)
        val buttonHome = findViewById<Button>(R.id.home)
        val buttonForward = findViewById<Button>(R.id.btnForward)
        val buttonSettings = findViewById<Button>(R.id.settings)
        val buttonhide = findViewById<Button>(R.id.hide)
        val buttonNewTab = findViewById<Button>(R.id.newtab)

        alleButtons = listOf(
            buttonBack, buttonReload, buttonHome, buttonForward,
            buttonSettings, buttonhide, buttonNewTab
        )

        buttonBack.setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        buttonReload.setOnClickListener { webView.reload() }
        buttonHome.setOnClickListener {
            webView.loadUrl(standart_suchmaschine_url)
            tablerow.visibility = View.GONE
        }
        buttonForward.setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        buttonSettings.setOnClickListener { startActivity(Intent(this, Settings::class.java)) }
        buttonhide.setOnClickListener {
            tablerow.visibility =
                if (tablerow.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        buttonNewTab.setOnClickListener { addTab(standart_suchmaschine_url) }

        setupTabBar()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    webView.canGoBack() -> webView.goBack()
                    tabs.size > 1 -> closeTab(currentIndex)
                    else -> {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })


        lifecycleScope.launch {
            applySettings()
            addTab(standart_suchmaschine_url)
        }
    }

    override fun onResume() {
        super.onResume()

        if (tabs.isNotEmpty()) {
            lifecycleScope.launch { applySettings() }
        }
    }

    private suspend fun applySettings() {
        try {
            val preferences = dataStore.data.first()

            val gespeicherteUrl = preferences[MEIN_STRING_KEY]
            if (!gespeicherteUrl.isNullOrEmpty()) {
                standart_suchmaschine_url = gespeicherteUrl
            }

            val gespeicherteFarbe = preferences[USER_COLOR_KEY]
            if (!gespeicherteFarbe.isNullOrEmpty()) {
                val parsedColor = Color.parseColor(gespeicherteFarbe)
                alleButtons.forEach { it.backgroundTintList = ColorStateList.valueOf(parsedColor) }
            }
        } catch (e: Exception) {

        }
    }

    private fun loadInput(input: String) {
        val text = input.trim()
        if (text.isEmpty()) return
        val url = when {
            text.startsWith("http://") || text.startsWith("https://") -> text
            text.contains(".") && !text.contains(" ") -> "https://$text"
            else -> standart_suchmaschine_url + "/search?q=" + Uri.encode(text)
        }
        webView.loadUrl(url)
    }


    private fun setupTabBar() {
        tabBar.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (!ignoreSelection) switchToTab(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun saveCurrentTab() {
        val tab = tabs.getOrNull(currentIndex) ?: return
        val bundle = Bundle()
        webView.saveState(bundle)
        tab.state = bundle
        webView.url?.let { tab.url = it }
    }


    private fun showTab(index: Int) {
        val tab = tabs[index]
        val state = tab.state
        if (state != null) webView.restoreState(state) else webView.loadUrl(tab.url)
        urlBar.setText(tab.url)
    }

    private fun switchToTab(index: Int) {
        if (index == currentIndex || index !in tabs.indices) return
        saveCurrentTab()
        currentIndex = index
        showTab(index)
    }

    private fun addTab(url: String) {
        saveCurrentTab()

        tabs.add(BrowserTab(url))
        currentIndex = tabs.lastIndex

        val newTab = tabBar.newTab().setText("Neu")
        ignoreSelection = true
        tabBar.addTab(newTab, true)
        ignoreSelection = false


        newTab.view.setOnLongClickListener {
            closeTab(newTab.position)
            true
        }

        webView.loadUrl(url)
    }

    private fun closeTab(index: Int) {
        if (tabs.size <= 1 || index !in tabs.indices) return

        val closingCurrent = index == currentIndex

        tabs.removeAt(index)
        ignoreSelection = true
        tabBar.removeTabAt(index)
        ignoreSelection = false

        currentIndex = when {
            index < currentIndex -> currentIndex - 1
            closingCurrent -> index.coerceAtMost(tabs.lastIndex)
            else -> currentIndex
        }

        ignoreSelection = true
        tabBar.getTabAt(currentIndex)?.select()
        ignoreSelection = false

        if (closingCurrent) showTab(currentIndex)
    }
}