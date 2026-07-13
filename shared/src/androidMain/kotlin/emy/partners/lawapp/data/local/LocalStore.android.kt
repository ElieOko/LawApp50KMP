package emy.partners.lawapp.data.local

import android.content.Context

actual fun createLocalStore(): LocalStore {
    val context = AndroidAppContext.getOrNull()
    return if (context != null) {
        AndroidLocalStore(context)
    } else {
        InMemoryLocalStore()
    }
}

private class AndroidLocalStore(
    context: Context,
) : LocalStore {
    private val prefs = context.getSharedPreferences("lawapp_auth_prefs", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }
}

private class InMemoryLocalStore : LocalStore {
    private val values = mutableMapOf<String, String>()

    override fun getString(key: String): String? = values[key]

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun remove(key: String) {
        values.remove(key)
    }

    override fun clear() {
        values.clear()
    }
}
