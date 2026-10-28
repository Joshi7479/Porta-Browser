package com.example.jbrowsev1

import android.se.omapi.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession

class BrowserHistoryDelegate(
    private val dao: HistoryDao,
    private val scope: CoroutineScope
) : GeckoSession.HistoryDelegate {
    @Volatile
    var lastUrl: String? = null

    override fun onVisited(
        session: GeckoSession,
        url: String,
        lastVisitedURL: String?,
        flags: Int
    ): GeckoResult<Boolean>? {
        val isTopLevel =
            (flags and GeckoSession.HistoryDelegate.VISIT_TOP_LEVEL) != 0
        val isRedirectSource =
            (flags and GeckoSession.HistoryDelegate.VISIT_REDIRECT_SOURCE) != 0 ||
                    (flags and GeckoSession.HistoryDelegate.VISIT_REDIRECT_SOURCE_PERMANENT) != 0
        val isError =
            (flags and GeckoSession.HistoryDelegate.VISIT_UNRECOVERABLE_ERROR) != 0
        val shuoldRecord = isTopLevel && !isRedirectSource && !isError
        if (shuoldRecord) {
            lastUrl = url
            scope.launch(Dispatchers.IO) {
                dao.insert(HistoryEntry(url = url))
            }
        }
        return GeckoResult.fromValue(shuoldRecord)

    }

    override fun getVisited(
        session: GeckoSession,
        urls: Array<String>
    ): GeckoResult<BooleanArray>? {
        val result = GeckoResult<BooleanArray>()
        scope.launch(Dispatchers.IO) {
            val visited = urls.toList().chunked(500)
                .flatMap { dao.findVisited(it) }
                .toSet()
            result.complete(BooleanArray(urls.size) { urls[it] in visited })
        }
        return result


    }
}








