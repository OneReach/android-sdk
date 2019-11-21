package ai.onereach.sdk.widget

import ai.onereach.sdk.core.EventHandler
import ai.onereach.sdk.core.JavaScriptInterface
import ai.onereach.sdk.persistent.PersistentRepository
import ai.onereach.sdk.persistent.WebkitCookieManagerProxy
import ai.onereach.sdk.persistent.WebkitLocalStorageManager
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class OneReachWebView : WebView {

    private val eventHandlers = mutableMapOf<String, EventHandler>()
    lateinit var jsInterface: JavaScriptInterface

    private var webViewOkHttpClient: OkHttpClient? = null
    private var localStorageManager: WebkitLocalStorageManager? = null
    private var persistentRepository: PersistentRepository? = null
        set(value) {
            field = value
            value?.apply {
                webViewOkHttpClient =
                    OkHttpClient()
                        .newBuilder()
                        .cookieJar(WebkitCookieManagerProxy(this))
                        .build()
                localStorageManager = WebkitLocalStorageManager(this)
            }

        }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun init() {
        val cookieManager = CookieManager.getInstance()
        CookieManager.setAcceptFileSchemeCookies(true)
        cookieManager.setAcceptCookie(true)
        cookieManager.acceptCookie()
        cookieManager.setAcceptThirdPartyCookies(this, true)


        jsInterface = JavaScriptInterface(this)

        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.domStorageEnabled = true
        settings.builtInZoomControls = false
        settings.loadsImagesAutomatically = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = false
        settings.setSupportZoom(true)
        settings.setAppCacheEnabled(true)

        addJavascriptInterface(jsInterface, jsInterface.INTERFACE_NAME)

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(request?.url.toString())

                return true
            }

            override fun shouldInterceptRequest(
                view: WebView,
                url: String
            ): WebResourceResponse? =
                webViewOkHttpClient
                    ?.let { okHttpClient ->
                        url
                            .takeIf { it.startsWith("http://") || it.startsWith("https://") }
                            ?.run {
                                try {
                                    val okHttpRequest = Request.Builder()
                                        .url(this)
                                        .build()
                                    val response = okHttpClient
                                        .newCall(okHttpRequest)
                                        .execute()

                                    val mimeType = "text/html"
                                    val encoding = "utf-8"
                                    return WebResourceResponse(
                                        mimeType,
                                        encoding,
                                        response.body()?.byteStream()
                                    )
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }

                                return null
                            }
                    }

        }

        // set WebChromeClient for show alerts and other js features
        webChromeClient = object : WebChromeClient() {}
    }

    override fun onDetachedFromWindow() {
        removeJavascriptInterface(jsInterface.INTERFACE_NAME)
        localStorageManager?.jsSaveWebViewLocalStorage(this)
        super.onDetachedFromWindow()
    }

    /**
     * Js Interface
     */
    fun send(eventName: String, params: Map<String, Any>?) {
        jsInterface.pushEvent(eventName, params)
    }

    fun onEventReceived(eventName: String, params: Map<String, Any>?) {
        eventHandlers[eventName]?.onHandleEvent(params)
    }

    /**
     * Handlers
     */
    fun registerHandler(eventName: String, eventHandler: EventHandler) {
        eventHandlers[eventName] = eventHandler
    }

    fun unregisterHandler(eventName: String) {
        eventHandlers.remove(eventName)
    }
}