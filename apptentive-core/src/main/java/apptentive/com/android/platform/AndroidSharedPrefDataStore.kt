package apptentive.com.android.platform

import android.content.SharedPreferences
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface AndroidSharedPrefDataStore {
    fun getSharedPrefForSDK(file: String): SharedPreferences
    fun deleteSharedPrefForSDK(file: String, mode: Int)
    fun getString(file: String, keyEntry: String, defaultValue: String = ""): String
    fun getNullableString(file: String, keyEntry: String, defaultValue: String?): String?
    fun getBoolean(file: String, keyEntry: String, defaultValue: Boolean = false): Boolean
    fun getInt(file: String, keyEntry: String, defaultValue: Int = -1): Int
    fun putString(file: String, keyEntry: String, value: String?)
    fun putBoolean(file: String, keyEntry: String, value: Boolean)
    fun getLong(file: String, keyEntry: String, defaultValue: Long = 0): Long
    fun putLong(file: String, keyEntry: String, value: Long)
    fun containsKey(file: String, keyEntry: String): Boolean
    fun putInt(file: String, keyEntry: String, value: Int)
}
