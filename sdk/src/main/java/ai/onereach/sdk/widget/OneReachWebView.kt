package ai.onereach.sdk.widget

import ai.onereach.sdk.core.EventHandler
import ai.onereach.sdk.core.JavaScriptInterface
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.WebView

class OneReachWebView : WebView {

    private val eventHandlers = mutableMapOf<String, EventHandler>()

    lateinit var jsInterface: JavaScriptInterface

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
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
    }

    override fun onDetachedFromWindow() {
        removeJavascriptInterface(jsInterface.INTERFACE_NAME)
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