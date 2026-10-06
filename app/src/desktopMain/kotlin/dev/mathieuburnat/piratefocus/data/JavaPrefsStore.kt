package dev.mathieuburnat.piratefocus.data

import java.util.prefs.Preferences

class JavaPrefsStore : KeyValueStore {
    private val prefs = Preferences.userRoot().node("dev/mathieuburnat/piratefocus")
    override fun getString(key: String): String? = prefs.get(key, null)
    override fun putString(key: String, value: String) = prefs.put(key, value)
}
