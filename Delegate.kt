package com.example.PortaBrowser

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoSession

class BrowserContentDelegate(
    private val dao: HistoryDao,
    private val scope: CoroutineScope,
    private val historyDelegate: BrowserHistoryDelegate
) : GeckoSession.ContentDelegate {

    override fun onTitleChange(session: GeckoSession, title: String?) {
        val url = historyDelegate.lastUrl ?: return
        if (title.isNullOrBlank() || session.settings.usePrivateMode) return
        scope.launch(Dispatchers.IO) {
            dao.updateLatestTitle(url, title)
        }
    }
}





