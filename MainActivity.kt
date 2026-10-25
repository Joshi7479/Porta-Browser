package com.example.jbrowsev1

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

class MainActivity : AppCompatActivity() {

    companion object {

        private var sRuntime: GeckoRuntime? = null
    }

    private var standart_suchmaschine_url = "https://searx.tiekoetter.com"

    val USER_BOOKMARK_KEY = stringPreferencesKey("bookmarks")

    private lateinit var geckoView: GeckoView
    private lateinit var urlBar: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var tabBar: TabLayout
    private lateinit var tablerow: View
    private lateinit var alleButtons: List<Button>


    private class BrowserTab(
        var url: String,
        var title: String = "Neu"
    ) {
        lateinit var session: GeckoSession
        var canGoBack = false
        var canGoForward = false
    }

    private val tabs = mutableListOf<BrowserTab>()
    private var currentIndex = -1
    private var ignoreSelection = false

    private val currentTab: BrowserTab?
        get() = tabs.getOrNull(currentIndex)

    private fun getRuntime(): GeckoRuntime {
        return sRuntime ?: GeckoRuntime.create(applicationContext).also { sRuntime = it }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isFirstRun = sharedPref.getBoolean("isFirstRun", true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        if (isFirstRun) {
            startActivity(Intent(this, Welcome::class.java))
            sharedPref.edit().putBoolean("isFirstRun", false).apply()
        }

        geckoView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)
        progressBar = findViewById(R.id.progressBar)
        tabBar = findViewById(R.id.tabbar)
        tablerow = findViewById(R.id.tablerow)

        getRuntime()

        urlBar.setOnClickListener {
            urlBar.setText("")
        }

        urlBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                val input = urlBar.text.toString()
                loadInput(input)
                true
            } else false
        }

        val buttongit = findViewById<Button>(R.id.github)
        buttongit.setOnClickListener {
            val git = "github.com/Joshi7479/Porta-Browser"
            loadInput(git)
            tablerow.visibility = View.GONE
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

        buttonBack.setOnClickListener {
            currentTab?.let { if (it.canGoBack) it.session.goBack() }
        }
        buttonReload.setOnClickListener { currentTab?.session?.reload() }
        buttonHome.setOnClickListener {
            currentTab?.session?.loadUri(standart_suchmaschine_url)
            tablerow.visibility = View.GONE
        }
        buttonForward.setOnClickListener {
            currentTab?.let { if (it.canGoForward) it.session.goForward() }
        }
        buttonSettings.setOnClickListener { startActivity(Intent(this, Settings::class.java)) }
        buttonhide.setOnClickListener {
            tablerow.visibility =
                if (tablerow.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        buttonNewTab.setOnClickListener { addTab(standart_suchmaschine_url) }

        setupTabBar()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val tab = currentTab
                when {
                    tab != null && tab.canGoBack -> tab.session.goBack()
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
        currentTab?.session?.loadUri(url)
    }

    private fun createSession(tab: BrowserTab): GeckoSession {
        val session = GeckoSession()

        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                if (url == null) return
                tab.url = url
                if (tab === currentTab) urlBar.setText(url)
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                tab.canGoBack = canGoBack
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                tab.canGoForward = canGoForward
            }


            override fun onNewSession(
                session: GeckoSession,
                uri: String
            ): GeckoResult<GeckoSession>? {
                addTab(uri)
                return null
            }
        }

        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                if (tab === currentTab) {
                    progressBar.progress = 0
                    progressBar.visibility = View.VISIBLE
                }
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                if (tab === currentTab) progressBar.progress = progress
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                if (tab === currentTab) progressBar.visibility = View.GONE
            }
        }

        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                tab.title = (title ?: "").ifBlank { "Neu" }.take(12)
                val index = tabs.indexOf(tab)
                if (index >= 0) tabBar.getTabAt(index)?.text = tab.title
            }
        }

        return session
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

    private fun showTab(index: Int) {
        val tab = tabs[index]
        geckoView.releaseSession()
        geckoView.setSession(tab.session)
        urlBar.setText(tab.url)
        progressBar.visibility = View.GONE
    }

    private fun switchToTab(index: Int) {
        if (index == currentIndex || index !in tabs.indices) return
        currentIndex = index
        showTab(index)
    }

    private fun addTab(url: String) {
        val tab = BrowserTab(url)
        val session = createSession(tab)
        tab.session = session
        session.open(getRuntime())

        tabs.add(tab)
        currentIndex = tabs.lastIndex

        val newTab = tabBar.newTab().setText("new")
        ignoreSelection = true
        tabBar.addTab(newTab, true)
        ignoreSelection = false

        newTab.view.setOnLongClickListener {
            closeTab(tabs.indexOf(tab))
            true
        }

        geckoView.releaseSession()
        geckoView.setSession(session)
        session.loadUri(url)
        urlBar.setText(url)
    }

    private fun closeTab(index: Int) {
        if (tabs.size <= 1 || index !in tabs.indices) return

        val closingCurrent = index == currentIndex
        val tab = tabs[index]

        if (closingCurrent) geckoView.releaseSession()
        tab.session.close()

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