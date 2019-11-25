package ai.onereach.sdk.core

import ai.onereach.sdk.widget.OneReachWebView
import android.util.Log
import android.webkit.JavascriptInterface
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types.newParameterizedType


class JavaScriptInterface(private val webView: OneReachWebView) {

    /**
     * All of the communication should be handled by a single receiver function.
     * On native side received communication will be treated as events
     * with a eventName (String) and context (arbitrary data in form of a JSON object <String, Any>)
     *
     * Calls from the JS code for the specific eventName.
     */
    @JavascriptInterface
    fun callEvent(eventName: String, paramsJson: String?) {
        Log.d(TAG, "callEvent: eventName = $eventName ; paramsJson = $paramsJson")

        val params = jsonParamsToMap(paramsJson)
        Log.d(TAG, "callEvent: eventName = $eventName ; params = $params")

        webView.onEventReceived(eventName, params)

        // The received events should be broadcast locally via LocalBroadcastManager
        // sendBroadcastMessage(eventName, params)

    }

    fun sendBroadcastMessage(eventName: String, params: Map<String, Any>?) {
        //TODO: create and set value to LiveData for external subscribe to events
    }

    /**
     * All of the communication should be handled by a single sender function
     * Calls to JS code for the specific eventName.
     */
    fun pushEvent(eventName: String, params: Map<String, Any>?) {
        Log.d(TAG, "pushEvent: eventName = $eventName ; params = $params")

        val jsonParams = mapParamsToJson(params)
        val script = "javascript: handleEvent('$eventName', $jsonParams)"

        Log.d(TAG, "pushEvent script: $script")

        // WebView method may called on thread 'JavaBridge', all methods must be called on the same thread.
        webView.post { webView.evaluateJavascript(script, null) }
    }

    /**
     * JavascriptInterface works only with primitive types and Strings,
     * so 'params' should be returned as JSON string and will be converted to Map on Android side.
     *
     * Convert JSON params string to Map
     */
    fun jsonParamsToMap(paramsJson: String?): Map<String, Any>? {
        return paramsJson?.let { json ->
            val type = newParameterizedType(
                Map::class.java,
                String::class.java,
                Any::class.java
            )
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter<Map<String, Any>>(type)
            return try {
                adapter.fromJson(json)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * JavascriptInterface works only with primitive types and Strings,
     * so Map of 'params' should be passed as JSON string to script
     * and will be converted to JSON string on Android side before.
     *
     * Convert Map params to JSON string
     */
    fun mapParamsToJson(params: Map<String, Any>?): String? {
        return params?.let { paramsMap ->
            val type = newParameterizedType(
                Map::class.java,
                String::class.java,
                Any::class.java
            )
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter<Map<String, Any>>(type)
            return try {
                adapter.toJson(paramsMap)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    companion object {
        private const val TAG = "JavaScriptInterface"
        const val INTERFACE_NAME = "AndroidOneReachInterface"
    }
}