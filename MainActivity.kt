package com.example.jbrowsev1

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.StorageController
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job




class MainActivity : AppCompatActivity() {

    private val db by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "browser.db")
            .build()
    }

    companion object {
        private const val TAG = "Browser"
        private const val DEFAULT_TAB_TITLE = "..."
        private const val BOOKMARKS_KEY = "bookmarks"
        private var sRuntime: GeckoRuntime? = null
    }

    private var standart_suchmaschine_url = "https://searx.tiekoetter.com"

    private var urlwhere = "top"



    private lateinit var geckoView: GeckoView
    private lateinit var urlBar: EditText
    private lateinit var urlBar2: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var tabBar: TabLayout
    private lateinit var tablerow: View
    private lateinit var topurl: LinearLayout
    private lateinit var bottomBar: LinearLayout
    private lateinit var bookmark: Button
    private lateinit var cookiebutton: Button
    private lateinit var Cookieside: LinearLayout
    private lateinit var alleButtons: List<Button>


    private data class Bookmark(val title: String, val url: String)

    private val bookmarkList = mutableListOf<Bookmark>()
    private val historyList = mutableListOf<GeckoSession.HistoryDelegate.HistoryList>()
    private var buttonColor: Int? = null
    private lateinit var bookmarkSide: LinearLayout
    private lateinit var bookmarkTitle: EditText
    private lateinit var bookmarkUrl: EditText
    private lateinit var bookmarkListContainer: LinearLayout
    private lateinit var bookmarkEmpty: TextView
    private lateinit var historySide: LinearLayout
    private lateinit var historyListContainer: LinearLayout
    private lateinit var historyEmpty: TextView
    private var historyJob: Job? = null

    private class BrowserTab(
        var url: String,
        var title: String = DEFAULT_TAB_TITLE
    ) {
        lateinit var session: GeckoSession
        var fullTitle = ""
        var canGoBack = false
        var canGoForward = false
    }

    private val tabs = mutableListOf<BrowserTab>()
    private var currentIndex = -1
    private var ignoreSelection = false

    private val currentTab: BrowserTab?
        get() = tabs.getOrNull(currentIndex)



    private fun getRuntime(): GeckoRuntime {
        return sRuntime ?: GeckoRuntime.getDefault(applicationContext).also { sRuntime = it }
    }



    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isFirstRun = sharedPref.getBoolean("isFirstRun", true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout() or
                        WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        if (isFirstRun) {
            startActivity(Intent(this, Welcome::class.java))
            sharedPref.edit().putBoolean("isFirstRun", false).apply()
        }



        cookiebutton = findViewById(R.id.cookie)
        geckoView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)
        urlBar2 = findViewById(R.id.urlBar2)
        progressBar = findViewById(R.id.progressBar)
        tabBar = findViewById(R.id.tabbar)
        tablerow = findViewById(R.id.tablerow)
        topurl = findViewById(R.id.urltop)
        bottomBar = findViewById(R.id.bottomBar1)
        bookmark = findViewById(R.id.bookmarks)


        getRuntime()

        urlBar.setOnClickListener { urlBar.setText("") }
        urlBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadInput(urlBar.text.toString())
                true
            } else false
        }

        urlBar2.setOnClickListener { urlBar2.setText("") }
        urlBar2.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadInput(urlBar2.text.toString())
                true
            } else false
        }


        val buttongit = findViewById<Button>(R.id.github)
        val buttonBack = findViewById<Button>(R.id.btnBack)
        val buttonReload = findViewById<Button>(R.id.btnReload)
        val buttonHome = findViewById<Button>(R.id.home)
        val buttonForward = findViewById<Button>(R.id.btnForward)
        val buttonSettings = findViewById<Button>(R.id.settings)
        val buttonhide = findViewById<Button>(R.id.hide)
        val buttonhide2 = findViewById<Button>(R.id.hide2)
        val buttonNewTab = findViewById<Button>(R.id.newtab)
        val buttonNewTab2 = findViewById<Button>(R.id.newtab2)
        val bookmarkdone = findViewById<Button>(R.id.bookmarkdone)
        val bookmarkadd = findViewById<Button>(R.id.bookmarkadd)
        val Cookiedone = findViewById<Button>(R.id.cookiedone)
        val historyclear = findViewById<Button>(R.id.resetcookies)
        val buttonhistory = findViewById<Button>(R.id.historybutton)
        val historydone = findViewById<Button>(R.id.historydone)


        buttonhistory.setOnClickListener { setHistoryMode(true) }
        historydone.setOnClickListener { setHistoryMode(false) }


        Cookieside = findViewById(R.id.cookieside)
        bookmarkSide = findViewById(R.id.bookmarkside)
        bookmarkTitle = findViewById(R.id.bookmarktitle)
        bookmarkUrl = findViewById(R.id.bookmarkurl)
        bookmarkListContainer = findViewById(R.id.bookmarklist)
        bookmarkEmpty = findViewById(R.id.bookmarkempty)
        historyEmpty = findViewById<TextView>(R.id.historyempty)
        loadBookmarks()

        bookmark.setOnClickListener { setBookmarkMode(true) }
        bookmarkdone.setOnClickListener { setBookmarkMode(false) }
        bookmarkadd.setOnClickListener { saveBookmarkFromInputs() }



        cookiebutton.setOnClickListener {
            tablerow.visibility = View.GONE
            tabBar.visibility = View.GONE
            urlBar.visibility = View.GONE
            geckoView.visibility = View.GONE
            Cookieside.visibility = View.VISIBLE
            buttonNewTab.visibility = View.GONE
            buttonhide.visibility = View.GONE
            buttonhide2.visibility = View.GONE
            buttonNewTab2.visibility = View.GONE
        }
        Cookiedone.setOnClickListener {
            tablerow.visibility = View.VISIBLE
            tabBar.visibility = View.VISIBLE
            urlBar.visibility = View.VISIBLE
            geckoView.visibility = View.VISIBLE
            Cookieside.visibility = View.GONE
            buttonNewTab.visibility = View.VISIBLE
            buttonhide.visibility = View.VISIBLE
            buttonhide2.visibility = View.VISIBLE
            buttonNewTab2.visibility = View.VISIBLE
        }

        alleButtons = listOf(
            buttonBack, buttonReload, buttonHome, buttonForward,
            buttonSettings, buttonhide, buttonNewTab, buttonhide2,
            buttonNewTab2, buttongit, bookmarkdone, bookmarkadd, bookmark
        )

        buttongit.setOnClickListener {
            loadInput("github.com/Joshi7479/Porta-Browser")
            tablerow.visibility = View.GONE
        }
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

        val toggleTabRow = View.OnClickListener {
            tablerow.visibility =
                if (tablerow.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        buttonhide.setOnClickListener(toggleTabRow)
        buttonhide2.setOnClickListener(toggleTabRow)

        buttonNewTab.setOnClickListener { addTab(standart_suchmaschine_url) }
        buttonNewTab2.setOnClickListener { addTab(standart_suchmaschine_url) }

        setupTabBar()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val tab = currentTab
                when {
                    bookmarkSide.visibility == View.VISIBLE -> setBookmarkMode(false)
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
        historySide = findViewById(R.id.history)
        historyListContainer = findViewById(R.id.historylayout)

        findViewById<Button>(R.id.historybutton).setOnClickListener { setHistoryMode(true) }
        historyclear.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) { db.historyDao().clear() }
        }
    }

    override fun onResume() {
        super.onResume()
        if (tabs.isNotEmpty()) {
            lifecycleScope.launch { applySettings() }
        }
    }

    private fun setHistoryMode(show: Boolean) {
        val browserVis = if (show) View.GONE else View.VISIBLE
        geckoView.visibility = browserVis
        urlBar.visibility = browserVis
        urlBar2.visibility = browserVis
        tablerow.visibility = View.GONE
        tabBar.visibility = browserVis
        findViewById<Button>(R.id.hide).visibility = browserVis
        findViewById<Button>(R.id.newtab).visibility = browserVis
        findViewById<Button>(R.id.newtab2).visibility = browserVis
        historySide.visibility = if (show) View.VISIBLE else View.GONE

        historyJob?.cancel()
        if (show) {
            historyJob = lifecycleScope.launch {
                db.historyDao().observeAll().collect { entries ->
                    renderHistory(entries.take(200))
                }
            }
        }
    }

    private suspend fun applySettings() {
        try {
            val preferences = dataStore.data.first()

            val gespeicherteUrl = preferences[MEIN_STRING_KEY]
            if (!gespeicherteUrl.isNullOrEmpty() && gespeicherteUrl.startsWith("http")) {
                standart_suchmaschine_url = gespeicherteUrl
            }

            val toporbottom = preferences[USER_URL_BAR]
            if (toporbottom == "top" || toporbottom == "down") {
                urlwhere = toporbottom
            }
            updateUrlBarPosition()

            val gespeicherteFarbe = preferences[USER_COLOR_KEY]
            if (!gespeicherteFarbe.isNullOrEmpty()) {
                val parsedColor = Color.parseColor(gespeicherteFarbe)
                buttonColor = parsedColor
                alleButtons.forEach { it.backgroundTintList = ColorStateList.valueOf(parsedColor) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "applySettings failed", e)
        }
    }

    private fun updateUrlBarPosition() {
        val top = urlwhere == "top"
        topurl.visibility = if (top) View.VISIBLE else View.GONE
        bottomBar.visibility = if (top) View.GONE else View.VISIBLE
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


    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun loadBookmarks() {
        bookmarkList.clear()
        try {
            val json = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                .getString(BOOKMARKS_KEY, "[]") ?: "[]"
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                bookmarkList.add(Bookmark(o.getString("title"), o.getString("url")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "loading Bookmarks failed", e)
        }
    }



    private fun saveBookmarks() {
        val arr = JSONArray()
        bookmarkList.forEach { arr.put(JSONObject().put("title", it.title).put("url", it.url)) }
        getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit().putString(BOOKMARKS_KEY, arr.toString()).apply()
    }


    private fun setBookmarkMode(show: Boolean) {
        val browserVis = if (show) View.GONE else View.VISIBLE
        geckoView.visibility = browserVis
        urlBar.visibility = browserVis
        urlBar2.visibility = browserVis
        tablerow.visibility = View.GONE
        tabBar.visibility = browserVis
        findViewById<Button>(R.id.hide).visibility = browserVis
        findViewById<Button>(R.id.newtab).visibility = browserVis
        findViewById<Button>(R.id.newtab2).visibility = browserVis
        bookmarkSide.visibility = if (show) View.VISIBLE else View.GONE

        if (show) {
            val tab = currentTab
            bookmarkUrl.setText(tab?.url ?: "")
            bookmarkTitle.setText(
                tab?.fullTitle?.takeIf { it.isNotBlank() } ?: tab?.url ?: ""
            )
            refreshBookmarkList()
        } else {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(bookmarkTitle.windowToken, 0)
        }
    }



    private fun saveBookmarkFromInputs() {
        var url = bookmarkUrl.text.toString().trim()
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter an URL", Toast.LENGTH_SHORT).show()
            return
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "https://$url"
        val title = bookmarkTitle.text.toString().trim().ifEmpty { url }

        val existing = bookmarkList.indexOfFirst { it.url == url }
        if (existing >= 0) bookmarkList[existing] = Bookmark(title, url)
        else bookmarkList.add(Bookmark(title, url))

        saveBookmarks()
        refreshBookmarkList()
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
    }

    private fun refreshBookmarkList() {
        bookmarkListContainer.removeAllViews()
        bookmarkEmpty.visibility = if (bookmarkList.isEmpty()) View.VISIBLE else View.GONE

        bookmarkList.forEach { bm ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(4) }
            }

            val openBtn = Button(this).apply {
                text = bm.title
                isAllCaps = false
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                layoutParams = LinearLayout.LayoutParams(0, dp(50), 1f)

                setOnClickListener {
                    setBookmarkMode(false)
                    loadInput(bm.url)
                }

                setOnLongClickListener {
                    setBookmarkMode(false)
                    addTab(bm.url)
                    true
                }
            }

            val deleteBtn = Button(this).apply {
                text = "✕"
                layoutParams = LinearLayout.LayoutParams(dp(60), dp(50))
                setOnClickListener {
                    bookmarkList.remove(bm)
                    saveBookmarks()
                    refreshBookmarkList()
                }
            }

            buttonColor?.let {
                val tint = ColorStateList.valueOf(it)
                openBtn.backgroundTintList = tint
                deleteBtn.backgroundTintList = tint
            }

            row.addView(openBtn)
            row.addView(deleteBtn)
            bookmarkListContainer.addView(row)
        }
    }



    private fun renderHistory(entries: List<HistoryEntry>) {
        historyListContainer.removeAllViews()
        historyEmpty.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE

        val dateFormat = java.text.DateFormat.getDateTimeInstance(
            java.text.DateFormat.SHORT, java.text.DateFormat.SHORT
        )

        entries.forEach { entry ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(4) }
            }

            val openBtn = Button(this).apply {
                val name = entry.title?.takeIf { it.isNotBlank() } ?: entry.url
                text = "$name\n${dateFormat.format(java.util.Date(entry.visitedAt))}"
                isAllCaps = false
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                setOnClickListener {
                    setHistoryMode(false)
                    loadInput(entry.url)
                }
                setOnLongClickListener {
                    setHistoryMode(false)
                    addTab(entry.url)
                    true
                }
            }

            val deleteBtn = Button(this).apply {
                text = "✕"
                layoutParams = LinearLayout.LayoutParams(dp(60), LinearLayout.LayoutParams.MATCH_PARENT)
                setOnClickListener {
                    lifecycleScope.launch(Dispatchers.IO) { db.historyDao().delete(entry.id) }
                }
            }

            buttonColor?.let {
                val tint = ColorStateList.valueOf(it)
                openBtn.backgroundTintList = tint
                deleteBtn.backgroundTintList = tint
            }

            row.addView(openBtn)
            row.addView(deleteBtn)
            historyListContainer.addView(row)
        }
    }

    private fun setUrlBars(url: String) {
        urlBar.setText(url)
        urlBar2.setText(url)
    }

    private fun createSession(tab: BrowserTab): GeckoSession {
        val session = GeckoSession()

        session.historyDelegate = BrowserHistoryDelegate(db.historyDao(), lifecycleScope)

        session.navigationDelegate = object : GeckoSession.NavigationDelegate {

            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                if (url == null) return
                tab.url = url
                if (tab === currentTab) setUrlBars(url)
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
                tab.fullTitle = title ?: ""
                tab.title = (title ?: "").ifBlank { DEFAULT_TAB_TITLE }.take(12)
                val index = tabs.indexOf(tab)
                if (index >= 0) tabBar.getTabAt(index)?.text = tab.title

                if (!title.isNullOrBlank()) {
                    val url = tab.url
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.historyDao().updateLatestTitle(url, title)
                    }
                }
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
        setUrlBars(tab.url)
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

        val newTab = tabBar.newTab().setText(DEFAULT_TAB_TITLE)
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
        setUrlBars(url)
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