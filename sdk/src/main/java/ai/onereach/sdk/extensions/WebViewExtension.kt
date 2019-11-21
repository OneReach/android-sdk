package ai.onereach.sdk.extensions

import android.webkit.ValueCallback
import android.webkit.WebView

fun WebView.jsInjection(jsScript: String, resultCallback: ValueCallback<String>?) {
    evaluateJavascript("javascript:$jsScript", resultCallback)
}

fun WebView.loadUrlWithJsInjection(url: String, jsScript: String) {
    val mimeType = "text/html"
    val encoding = "utf-8"
    val injection =
        "<script type='text/javascript'>" +
                "javascript:" +
                jsScript +
                "window.location.replace('$url');" +
                "</script>"
    loadDataWithBaseURL(url, injection, mimeType, encoding, null)
}

