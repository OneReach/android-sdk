package ai.onereach.sdk.core

import ai.onereach.sdk.widget.OneReachWebView
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.webkit.JavascriptInterface

class JavaScriptInterface(val webView: OneReachWebView) {

    val TAG = "JavaScriptInterface"
    val INTERFACE_NAME = "js_interface"

    /**
     * All of the communication should be handled by a single receiver function.
     * On native side received communication will be treated as events
     * with a eventName (String) and context (arbitrary data in form of a JSON object <String, Any>)
     *
     * Calls from the JS code for the specific eventName.
     */
    @JavascriptInterface
    fun callEvent(eventName: String, params: Map<String, Any>?) {
        Log.d(TAG, "callEvent: eventName = $eventName ; params = $params")
        webView.onEventReceived(eventName, params)

        // the received events should be
        // broadcasted locally via LocalBroadcastManager
        sendBroadcastMessage(eventName, params)

    }

    fun sendBroadcastMessage(eventName: String, params: Map<String, Any>?) {
        Intent().also { intent ->
            //TODO: set correct action for local broadcast
            //intent.setAction(MainActivity.brAction)
            LocalBroadcastManager.getInstance(webView.context).sendBroadcast(intent)
        }
    }

    /**
     * All of the communication should be handled by a single sender function
     * Calls to JS code for the specific eventName.
     */
    fun pushEvent(eventName: String, params: Map<String, Any>?) {
        Log.d(TAG, "pushEvent: eventName = $eventName ; params = $params")

        val script = "javascript: $eventName(\"${params!!["color"]}\")"
        Log.d(TAG, "pushEvent script: $script")
        webView.evaluateJavascript(script, null)
    }
}