package ai.onereach.sdk

import ai.onereach.sdk.core.JavaScriptInterface
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val BASE_URL = "file:///android_asset/webview.html"
    lateinit var jsInterface: JavaScriptInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        jsInterface = JavaScriptInterface(webView)

        initWebView();

        btnSendToWeb.setOnClickListener {
            jsInterface.pushEvent("updateFromAndroid", mapOf("message" to "${editToWeb.text}"))
        }
    }

    private fun injectJavaScriptFunction() {
        webView.loadUrl("javascript: " +
                "window.androidObj.textToAndroid = function(message) { " +
                jsInterface.INTERFACE_NAME + ".callEvent(message) }")
    }

    private fun initWebView() {
        if (0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(jsInterface, jsInterface.INTERFACE_NAME)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (url == BASE_URL) {
                    injectJavaScriptFunction()
                }
            }
        }
        webView.loadUrl(BASE_URL)
    }

    override fun onDestroy() {
        webView.removeJavascriptInterface(jsInterface.INTERFACE_NAME)
        super.onDestroy()
    }
}
