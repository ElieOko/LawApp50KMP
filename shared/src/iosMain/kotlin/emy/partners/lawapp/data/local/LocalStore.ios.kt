package emy.partners.lawapp.data.local

import platform.Foundation.NSUserDefaults

actual fun createLocalStore(): LocalStore = IosLocalStore()

private class IosLocalStore : LocalStore {
    private val defaults = NSUserDefaults.standardUserDefaults
    private val knownKeys = listOf("lawapp_auth_session")

    override fun getString(key: String): String? = defaults.stringForKey(key)

    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }

    override fun clear() {
        knownKeys.forEach { defaults.removeObjectForKey(it) }
    }
}
