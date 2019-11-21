package ai.onereach.sdk.persistent

/**
 * Created by vostopolets on 2019-10-07.
 */
interface PersistentRepository {

    suspend fun saveCookies(cookiesData: Set<String>?)
    suspend fun getCookies(): Set<String>?
    suspend fun removeExpiredCookiesForBot()
    suspend fun saveLocalStorage(localStorageData: String?)
    suspend fun getLocalStorage(): HashMap<String, String>?

}