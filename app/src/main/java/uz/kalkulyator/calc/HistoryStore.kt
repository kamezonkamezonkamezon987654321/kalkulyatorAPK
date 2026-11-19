package uz.kalkulyator.calc

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class HistoryEntry(val expression: String, val result: String)

class HistoryStore(private val prefs: SharedPreferences) {

    fun load(): List<HistoryEntry> = try {
        val array = JSONArray(prefs.getString(KEY, "[]"))
        List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            HistoryEntry(obj.getString("e"), obj.getString("r"))
        }
    } catch (e: JSONException) {
        emptyList()
    }

    fun save(entries: List<HistoryEntry>) {
        val array = JSONArray()
        entries.forEach { array.put(JSONObject().put("e", it.expression).put("r", it.result)) }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    companion object {
        private const val KEY = "history"
        const val MAX_ENTRIES = 100
    }
}
