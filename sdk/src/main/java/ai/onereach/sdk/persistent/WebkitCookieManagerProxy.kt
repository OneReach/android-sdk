package ai.onereach.sdk.persistent

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.CookieStore
import java.net.URI
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.concurrent.withLock

/**
 * Created by vostopolets on 2019-11-13.
 */
class WebkitCookieManagerProxy(private val persistentRepository: PersistentRepository) :
    CookieManager(null, CookiePolicy.ACCEPT_ALL), CookieJar {

    // Lock and condition
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private var removeAllCookiesDone = false

    private val webkitCookieManager = android.webkit.CookieManager.getInstance()

    init {
        android.webkit.CookieManager.getInstance().setAcceptCookie(true)
        java.net.CookieHandler.setDefault(this)

        webkitCookieManager.removeAllCookies {
            webkitCookieManager.flush()

            lock.withLock {
                removeAllCookiesDone = true
                condition.signalAll()
            }
        }
    }

    override fun put(uri: URI?, responseHeaders: Map<String, MutableList<String>>?) {
        // make sure our args are valid
        if (uri == null || responseHeaders == null) return
        val url = uri.toString()

        responseHeaders
            .filter { it.key.equals("Set-Cookie", ignoreCase = true) }
            .takeIf { it.isNotEmpty() }
            ?.values
            ?.flatten()
            ?.toSet()
            ?.onEach { webkitCookieManager.setCookie(url, it) } // set cookies to CookieManager
            ?.let { saveWebCookies(it) }
    }

    override fun get(
        uri: URI?,
        requestHeaders: MutableMap<String, MutableList<String>>?
    ): Map<String, MutableList<String>> {
        // make sure our args are valid
        if (uri == null || requestHeaders == null) return emptyMap()
        val url = uri.toString()

        restoreWebCookies()
            ?.onEach { webkitCookieManager.setCookie(url, it) }

        return webkitCookieManager.getCookie(url)
            ?.let { cookies ->
                mutableMapOf<String, MutableList<String>>()
                    .apply {
                        this["Cookie"] = mutableListOf(cookies)
                    }
            } ?: emptyMap()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        try {
            put(
                url.uri(),
                mutableMapOf<String, MutableList<String>>()
                    .apply {
                        this["Set-Cookie"] = cookies
                            .map { it.toString() }
                            .toMutableList()
                    })
        } catch (e: IOException) {
            Log.e(TAG, "Error adding cookies through okhttp", e)
        }
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        lock.withLock {
            while (!removeAllCookiesDone) {
                try {
                    condition.await()
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                }
            }
        }

        val cookieArrayList = ArrayList<Cookie>()
        try {
            get(url.uri(), HashMap())
                .values
                .flatten()
                .forEach { cookieValue ->
                    // Format here looks like: "Cookie":["cookie1=val1;cookie2=val2;"]
                    cookieValue
                        .split(";".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                        .mapNotNull { Cookie.parse(url, it) }
                        .run { cookieArrayList.addAll(this) }
                }
        } catch (e: IOException) {
            Log.e(TAG, "Error making cookie!", e)
        }

        return cookieArrayList
    }

    override fun getCookieStore(): CookieStore {
        // we don't want anyone to work with this cookie store directly
        throw UnsupportedOperationException()
    }

    /**
     * If new cookies are exists, add them (update) to stored cookies if there are exists.
     * Also, check all stored cookies fox expiration
     */
    private fun saveWebCookies(cookiesData: Set<String>?) = runBlocking {
        cookiesData
            ?.let { newCookies ->
                mutableSetOf<String>()
                    .apply {
                        removeExpiredCookies()
                        persistentRepository.getCookies()
                            ?.let { storedCookies ->
                                addAll(storedCookies)
                            }
                    }
                    .apply { addAll(newCookies) }
                    .run { persistentRepository.saveCookies(this) }
            }
    }

    private fun restoreWebCookies(): Set<String>? = runBlocking {
        persistentRepository.getCookies()
    }

    private suspend fun removeExpiredCookies() {
        val currentTime = System.currentTimeMillis()
        //HttpUrl.parse(session.botUrl)

        // For parsing Cookie need to create valid HttpUrl
        HttpUrl.parse("http://www.google.com")
            ?.let { httpUrl ->
                persistentRepository.getCookies()
                    ?.mapNotNull { Cookie.parse(httpUrl, it) }
                    ?.filter { !it.persistent() || (it.expiresAt() > currentTime) }
                    ?.map { it.toString() }
                    ?.toSet()
                    ?.run { persistentRepository.saveCookies(this) }
            }
    }

    companion object {
        private const val TAG = "CookieManagerProxy"
    }
}