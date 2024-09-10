package apptentive.com.android.platform

import android.content.Context
import android.content.SharedPreferences
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
class DefaultAndroidSharedPrefDataStore(val context: Context) : AndroidSharedPrefDataStore {
    override fun getSharedPrefForSDK(file: String): SharedPreferences =
        context.getSharedPreferences(file, Context.MODE_PRIVATE)

    override fun deleteSharedPrefForSDK(file: String, mode: Int) {
        if (context.getSharedPreferences(file, mode) != null) {
            context.getSharedPreferences(file, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }
    }

    override fun containsKey(file: String, keyEntry: String): Boolean =
        context.getSharedPreferences(file, Context.MODE_PRIVATE).contains(keyEntry)

    override fun getBoolean(file: String, keyEntry: String, defaultValue: Boolean): Boolean =
        context.getSharedPreferences(file, Context.MODE_PRIVATE).getBoolean(keyEntry, defaultValue)

    override fun getInt(file: String, keyEntry: String, defaultValue: Int): Int =
        context.getSharedPreferences(file, Context.MODE_PRIVATE).getInt(keyEntry, defaultValue)

    override fun getString(file: String, keyEntry: String, defaultValue: String): String =
        context.getSharedPreferences(file, Context.MODE_PRIVATE).getString(keyEntry, defaultValue) ?: ""

    override fun getNullableString(file: String, keyEntry: String, defaultValue: String?): String? =
        context.getSharedPreferences(file, Context.MODE_PRIVATE).getString(keyEntry, defaultValue)

    override fun getLong(file: String, keyEntry: String, defaultValue: Long): Long =
        context.getSharedPreferences(file, Context.MODE_PRIVATE).getLong(keyEntry, defaultValue)

    override fun putBoolean(file: String, keyEntry: String, value: Boolean) {
        context.getSharedPreferences(file, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(keyEntry, value)
            .apply()
    }

    override fun putString(file: String, keyEntry: String, value: String?) {
        context.getSharedPreferences(file, Context.MODE_PRIVATE)
            .edit()
            .putString(keyEntry, value)
            .apply()
    }

    override fun putLong(file: String, keyEntry: String, value: Long) {
        context.getSharedPreferences(file, Context.MODE_PRIVATE)
            .edit()
            .putLong(keyEntry, value)
            .apply()
    }

    override fun putInt(file: String, keyEntry: String, value: Int) {
        context.getSharedPreferences(file, Context.MODE_PRIVATE)
            .edit()
            .putInt(keyEntry, value)
            .apply()
    }
}
