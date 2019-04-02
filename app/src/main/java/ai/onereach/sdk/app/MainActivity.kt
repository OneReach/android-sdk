package ai.onereach.sdk.app

import ai.onereach.sdk.core.EventHandler
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val BASE_URL = "file:///android_asset/TestPushAndPlatformAPI.html"
        const val brAction = "js_event_broadcast"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initWebView()

        btnRegister.setOnClickListener {
            webView.apply {
                registerHandler("getPushToken", object : EventHandler() {
                    override fun onHandleEvent(params: Map<String, Any>?) {
                        webView.send("handlePushToken", mapOf("token" to "Test SDK Push Token"))
                    }

                })
                registerHandler("getDeviceInfo", object : EventHandler() {
                    override fun onHandleEvent(params: Map<String, Any>?) {
                        webView.send("handleDeviceInfo", mapOf("platform" to "Android"))
                    }

                })
            }
        }
        btnUnregister.setOnClickListener {
            webView.apply {
                unregisterHandler("getPushToken")
                unregisterHandler("getDeviceInfo")
            }
        }
    }

    private fun initWebView() {
        webView.loadUrl(BASE_URL)
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(br, filter)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br)
    }

    val br: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("BroadcastReceiver", "onReceive")
        }

    }
    val filter = IntentFilter(brAction)
}
