package emy.partners.lawapp.data.local

import android.content.Context

object AndroidAppContext {
    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun getOrNull(): Context? = appContext

    fun require(): Context =
        appContext ?: error("AndroidAppContext non initialise. Appeler AndroidAppContext.init() au demarrage.")
}
