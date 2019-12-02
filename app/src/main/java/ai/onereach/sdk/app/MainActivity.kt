package ai.onereach.sdk.app

import ai.onereach.sdk.core.EventHandler
import ai.onereach.sdk.persistent.PersistentRepository
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        //const val BASE_URL = "file:///android_asset/TestPushAndPlatformAPI.html"
        const val BASE_URL =
            "https://sdkapi-staging.onereach.ai/http/b3166140-3e91-46c6-b496-af0f507e7172/mobile/sandboxing"
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
        webView.persistentRepository = object : PersistentRepository {
            override suspend fun saveCookies(cookiesData: Set<String>?) {
                // implement saving of Cookies to the app storage
            }

            override suspend fun getCookies(): Set<String>? {
                // fetch stored Cookies from the app storage
                return null
            }

            override suspend fun saveLocalStorage(localStorageData: String?) {
                // implement saving of LocalStorage to the app storage
            }

            override suspend fun getLocalStorage(): HashMap<String, String>? {
                // fetch stored LocalStorage data from the app storage
                return null
            }
        }
        webView.loadUrl(BASE_URL)
    }

    override fun onBackPressed() {
        minimizeApp()
    }

    private fun minimizeApp() {
        startActivity(Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}
