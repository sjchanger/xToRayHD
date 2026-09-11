package app.xtorayhd.core

import android.util.Base64
import java.net.URI
import java.net.URLDecoder
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

object SubscriptionParser {
    fun parse(input: String): List<ProxyProfile> {
        val text = input.trim()
        if (text.startsWith("{")) return parseJson(text)
        val decoded = decodeMaybeBase64(text)
        val lines = decoded.split(Regex("\\r?\\n")).map { it.trim() }.filter { it.isNotEmpty() }
        val candidates = if (lines.size > 1) lines else listOf(text)
        return candidates.mapNotNull { parseUri(it) }
    }

    private fun decodeMaybeBase64(s: String): String {
        if (s.contains("://")) return s
        return try {
            val normalized = s.replace("\n", "").replace("\r", "").trim()
            String(Base64.decode(normalized, Base64.DEFAULT), Charsets.UTF_8)
        } catch (_: Exception) { s }
    }

    private fun parseJson(s: String): List<ProxyProfile> {
        val root = JSONObject(s)
        val arr = when {
            root.has("profiles") -> root.getJSONArray("profiles")
            root.has("outbounds") -> root.getJSONArray("outbounds")
            else -> JSONArray()
        }
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.getJSONObject(i)
            val protocol = o.optString("protocol").ifBlank { "unknown" }
            val server = o.optString("server")
            val port = o.optInt("port", 0)
            if (server.isBlank() || port == 0) null else ProxyProfile(
                UUID.randomUUID().toString(), o.optString("name", "$protocol • $server"),
                protocol, server, port, o.toString(), o.toString()
            )
        }
    }

    private fun parseUri(uri: String): ProxyProfile? {
        return try {
            when {
                uri.startsWith("vless://", true) -> parseVless(uri)
                uri.startsWith("vmess://", true) -> parseVmess(uri)
                uri.startsWith("trojan://", true) -> parseTrojan(uri)
                uri.startsWith("ss://", true) -> parseShadowsocks(uri)
                else -> null
            }
        } catch (_: Exception) { null }
    }

    private fun parseVless(s: String): ProxyProfile {
        val u = URI(s)
        val q = java.net.URLDecoder.decode(u.rawQuery ?: "", "UTF-8")
        val query = q.split("&").mapNotNull {
            val p = it.split("=", limit = 2); if (p.size == 2) p[0] to p[1] else null
        }.toMap()
        val uuid = u.userInfo
        val name = decode(u.fragment).ifBlank { "VLESS • ${u.host}" }
        val security = query["security"] ?: "none"
        val type = query["type"] ?: "tcp"
        val flow = query["flow"] ?: ""
        val stream = JSONObject().apply {
            put("network", type)
            put("security", security)
            if (query["sni"] != null) put("tlsSettings", JSONObject().put("serverName", query["sni"]))
            if (query["fp"] != null) put("tlsSettings", JSONObject().put("fingerprint", query["fp"]).put("serverName", query["sni"] ?: u.host))
            if (query["path"] != null) put("wsSettings", JSONObject().put("path", query["path"]).put("headers", JSONObject().put("Host", query["host"] ?: u.host)))
            if (query["serviceName"] != null) put("grpcSettings", JSONObject().put("serviceName", query["serviceName"]))
        }
        val user = JSONObject().put("id", uuid).put("encryption", "none")
        if (flow.isNotBlank()) user.put("flow", flow)
        val out = JSONObject().put("protocol","vless")
            .put("settings", JSONObject().put("vnext", JSONArray().put(JSONObject().put("address",u.host).put("port",u.port).put("users",JSONArray().put(user)))))
            .put("streamSettings", stream)
        return ProxyProfile(UUID.randomUUID().toString(), name, "VLESS", u.host, u.port, s, out.toString())
    }

    private fun parseVmess(s: String): ProxyProfile {
        val payload = s.removePrefix("vmess://")
        val o = JSONObject(String(Base64.decode(payload, Base64.DEFAULT), Charsets.UTF_8))
        val server = o.optString("add")
        val port = o.optInt("port")
        val uuid = o.optString("id")
        val stream = JSONObject().put("network", o.optString("net","tcp")).put("security", o.optString("tls","none").ifBlank{"none"})
        if (o.optString("host").isNotBlank() || o.optString("path").isNotBlank()) {
            stream.put("wsSettings", JSONObject().put("path", o.optString("path","/")).put("headers", JSONObject().put("Host",o.optString("host",server))))
        }
        if (o.optString("sni").isNotBlank()) stream.put("tlsSettings", JSONObject().put("serverName",o.optString("sni")))
        val user = JSONObject().put("id",uuid).put("alterId",0).put("security",o.optString("scy","auto"))
        val out = JSONObject().put("protocol","vmess").put("settings",JSONObject().put("vnext",JSONArray().put(JSONObject().put("address",server).put("port",port).put("users",JSONArray().put(user))))).put("streamSettings",stream)
        return ProxyProfile(UUID.randomUUID().toString(), o.optString("ps").ifBlank{"VMess • $server"}, "VMess", server, port, s, out.toString())
    }

    private fun parseTrojan(s: String): ProxyProfile {
        val u = URI(s)
        val q = parseQuery(u.rawQuery ?: "")
        val stream = JSONObject().put("network",q["type"] ?: "tcp").put("security",q["security"] ?: "tls")
        if (q["sni"] != null) stream.put("tlsSettings",JSONObject().put("serverName",q["sni"]))
        if (q["path"] != null) stream.put("wsSettings",JSONObject().put("path",q["path"]).put("headers",JSONObject().put("Host",q["host"] ?: u.host)))
        val out = JSONObject().put("protocol","trojan").put("settings",JSONObject().put("servers",JSONArray().put(JSONObject().put("address",u.host).put("port",u.port).put("password",u.userInfo)))).put("streamSettings",stream)
        return ProxyProfile(UUID.randomUUID().toString(),decode(u.fragment).ifBlank{"Trojan • ${u.host}"},"Trojan",u.host,u.port,s,out.toString())
    }

    private fun parseShadowsocks(s: String): ProxyProfile {
        val u = URI(s)
        val decoded = try { String(Base64.decode(u.userInfo, Base64.DEFAULT), Charsets.UTF_8) } catch (_: Exception) { u.userInfo }
        val idx = decoded.indexOf(':')
        val method = if (idx > 0) decoded.substring(0,idx) else "aes-128-gcm"
        val password = if (idx > 0) decoded.substring(idx+1) else ""
        val out = JSONObject().put("protocol","shadowsocks").put("settings",JSONObject().put("servers",JSONArray().put(JSONObject().put("address",u.host).put("port",u.port).put("method",method).put("password",password))))
        return ProxyProfile(UUID.randomUUID().toString(),decode(u.fragment).ifBlank{"Shadowsocks • ${u.host}"},"Shadowsocks",u.host,u.port,s,out.toString())
    }

    private fun parseQuery(q: String) = q.split("&").mapNotNull { p -> p.split("=",limit=2).takeIf{it.size==2}?.let{it[0] to URLDecoder.decode(it[1],"UTF-8")} }.toMap()
    private fun decode(s: String?) = try { URLDecoder.decode(s ?: "", "UTF-8") } catch (_: Exception) { s ?: "" }
}
