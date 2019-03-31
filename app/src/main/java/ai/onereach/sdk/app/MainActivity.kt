package ai.onereach.sdk.app

import ai.onereach.sdk.core.EventHandler
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val BASE_URL = "file:///android_asset/webview.html"
        const val brAction = "js_event_broadcast"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initWebView()

        btnRegister.setOnClickListener {
            webView.registerHandler("showToast", object : EventHandler() {
                override fun onHandleEvent(params: Map<String, Any>?) {
                    Toast.makeText(this@MainActivity, "Empty string", LENGTH_LONG)?.show()
                }
            })
        }
        btnUnregister.setOnClickListener {
            webView.unregisterHandler("myTestHandler")
        }
        btnSendToWeb.setOnClickListener {
            val rnd = Random()
            val color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            val hexColor = String.format("#%06X", 0xFFFFFF and color)
            webView.send("changeColorFromAndroid", mapOf("color" to hexColor))
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
