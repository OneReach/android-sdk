package ai.onereach.sdk.persistent

/**
 * Created by vostopolets on 2019-10-07.
 */
public interface PersistentRepository {

    suspend fun saveCookies(cookiesData: Set<String>?)
    suspend fun getCookies(): Set<String>?
    suspend fun saveLocalStorage(localStorageData: Map<String, String>?)
    suspend fun getLocalStorage(): Map<String, String>?

}