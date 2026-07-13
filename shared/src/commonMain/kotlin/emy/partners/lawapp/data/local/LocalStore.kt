package emy.partners.lawapp.data.local

expect fun createLocalStore(): LocalStore

interface LocalStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
    fun clear()
}
