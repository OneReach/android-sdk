package ai.onereach.sdk.core

import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView

class JavaScriptInterface(val webView: WebView) {

    val INTERFACE_NAME = "js_interface"

    /**
     * All of the communication should be handled by a single receiver function.
     * On native side received communication will be treated as events
     * with a eventName (String) and context (arbitrary data in form of a JSON object <String, Any>)
     *
     * Calls from the JS code for the specific eventName.
     */
    @JavascriptInterface
    private fun callEvent(eventName: String, params: Map<String, Any>?) {
        Log.d("JavaScriptInterface", "callEvent: eventName = $eventName ; params = $params")
        // the received events should be
        // broadcasted locally via LocalBroadcastManager


    }

    @JavascriptInterface
    fun callEvent(eventName: String) {
        Log.d("JavaScriptInterface", "callEvent: eventName = $eventName")
    }

    /**
     * All of the communication should be handled by a single sender function
     * Calls to JS code for the specific eventName.
     */
    fun pushEvent(eventName: String, params: Map<String, Any>?) {
        Log.d("JavaScriptInterface", "pushEvent: eventName = $eventName ; params = $params")

        val script = "javascript: $eventName(\"${params!!["message"]}\")"
        webView.evaluateJavascript(script, null)
    }
}