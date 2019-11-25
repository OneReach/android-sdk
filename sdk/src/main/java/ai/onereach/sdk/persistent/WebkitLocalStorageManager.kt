package ai.onereach.sdk.persistent

import ai.onereach.sdk.extensions.jsInjection
import ai.onereach.sdk.extensions.loadUrlWithJsInjection
import android.webkit.ValueCallback
import android.webkit.WebView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by vostopolets on 2019-11-21.
 */
class WebkitLocalStorageManager(private val persistentRepository: PersistentRepository) {

    fun loadAndRestoreWebViewLocalStorage(webView: WebView, loadUrl: String) = runBlocking {
        persistentRepository.getLocalStorage()
            .takeIf { !it.isNullOrEmpty() }
            ?.let { storedLocalStorage ->
                val script =
                    StringBuilder("localStorage.clear();")
                        .apply {
                            storedLocalStorage
                                .forEach { (key, value) ->
                                    append("localStorage.setItem('$key','$value');")
                                }
                        }.toString()
                webView.loadUrlWithJsInjection(loadUrl, script)
            } ?: webView.loadUrlWithJsInjection(loadUrl, "localStorage.clear();")
    }

    fun jsSaveWebViewLocalStorage(webView: WebView) = runBlocking {
        webView
            .jsInjection(
                "Object.entries(localStorage);",
                ValueCallback {
                    GlobalScope.launch { persistentRepository.saveLocalStorage(it) }
                })
    }

}