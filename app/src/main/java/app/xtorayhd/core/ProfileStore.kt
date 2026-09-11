package app.xtorayhd.core

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("profiles", Context.MODE_PRIVATE)
    fun load(): List<ProxyProfile> {
        val a = JSONArray(prefs.getString("items","[]"))
        return (0 until a.length()).map { o ->
            val x=a.getJSONObject(o)
            ProxyProfile(x.getString("id"),x.getString("name"),x.getString("protocol"),x.getString("server"),x.getInt("port"),x.getString("rawUri"),x.getString("configJson"))
        }
    }
    fun save(items: List<ProxyProfile>) {
        val a=JSONArray()
        items.forEach { x -> a.put(JSONObject().put("id",x.id).put("name",x.name).put("protocol",x.protocol).put("server",x.server).put("port",x.port).put("rawUri",x.rawUri).put("configJson",x.configJson)) }
        prefs.edit().putString("items",a.toString()).apply()
    }
}
