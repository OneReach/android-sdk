package ai.onereach.sdk.widget

import ai.onereach.sdk.core.EventHandler
import ai.onereach.sdk.core.JavaScriptInterface
import ai.onereach.sdk.persistent.PersistentRepository
import ai.onereach.sdk.persistent.WebkitCookieManagerProxy
import ai.onereach.sdk.persistent.WebkitLocalStorageManager
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.nio.charset.Charset

class OneReachWebView : WebView {

    private val eventHandlers = mutableMapOf<String, EventHandler>()
    lateinit var jsInterface: JavaScriptInterface

    private var webViewOkHttpClient: OkHttpClient? = null
    private var localStorageManager: WebkitLocalStorageManager? = null

    var persistentRepository: PersistentRepository? = null
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

    private val activityLifecycle: LifecycleObserver by lazy {
        object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStopActivity() {
                Log.d("TAG_CHECK_PERS", "Lifecycle.Event.ON_STOP")
                localStorageManager?.jsSaveWebViewLocalStorage(this@OneReachWebView)
            }
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
        bindParentLifecycle()

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

        addJavascriptInterface(jsInterface, JavaScriptInterface.INTERFACE_NAME)

        webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? =
                webViewOkHttpClient
                    ?.let { okHttpClient ->
                        request
                            ?.url
                            ?.toString()
                            ?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
                            ?.run {
                                try {
                                    val okHttpRequest = Request.Builder()
                                        .url(this)
                                        .build()
                                    val response = okHttpClient
                                        .newCall(okHttpRequest)
                                        .execute()

                                    val responseBody = response.body() ?: return null

                                    val mimeType =
                                        responseBody
                                            .contentType()
                                            ?.run { "${type()}/${subtype()}" }
                                            ?: "text/html"

                                    val encoding =
                                        responseBody
                                            .contentType()
                                            ?.charset(Charset.defaultCharset())
                                            ?.name()
                                            ?: "utf-8"

                                    return WebResourceResponse(
                                        mimeType,
                                        encoding,
                                        responseBody.byteStream()
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

    override fun loadUrl(url: String?) {
        localStorageManager
            ?.let { storageManager ->
                url?.let { targetUrl ->
                    storageManager.loadAndRestoreWebViewLocalStorage(this, targetUrl)
                }
            } ?: super.loadUrl(url)
    }

    override fun loadUrl(url: String?, additionalHttpHeaders: MutableMap<String, String>?) {
        localStorageManager
            ?.let { storageManager ->
                url?.let { targetUrl ->
                    storageManager.loadAndRestoreWebViewLocalStorage(this, targetUrl)
                }
            } ?: super.loadUrl(url, additionalHttpHeaders)
    }

    override fun onDetachedFromWindow() {
        removeJavascriptInterface(JavaScriptInterface.INTERFACE_NAME)
        super.onDetachedFromWindow()
    }

    /**
     * Bind Activity Lifecycle for handle onStop() of Activity
     */
    private fun bindParentLifecycle() {
        (context as? LifecycleOwner)?.run { lifecycle.addObserver(activityLifecycle) }
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