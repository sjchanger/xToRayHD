package app.xtorayhd.core

import org.json.JSONArray
import org.json.JSONObject

object ConfigBuilder {
    fun build(profile: ProxyProfile): String {
        val out = JSONObject(profile.configJson)
        val direct = JSONObject().put("protocol","freedom").put("tag","direct")
        val block = JSONObject().put("protocol","blackhole").put("tag","block")
        val inbound = JSONObject()
            .put("tag","tun-in")
            .put("port",0)
            .put("protocol","tun")
            .put("settings",JSONObject()
                .put("name","xray0")
                .put("mtu",1500)
                .put("gateway",JSONArray().put("10.0.0.1/16").put("fd00::1/64"))
                .put("autoOutboundsInterface","auto")
                .put("userLevel",0))
        val routing = JSONObject()
            .put("domainStrategy","IPIfNonMatch")
            .put("rules",JSONArray()
                .put(JSONObject().put("type","field").put("protocol",JSONArray().put("bittorrent")).put("outboundTag","block")))
        return JSONObject()
            .put("log",JSONObject().put("loglevel","warning"))
            .put("dns",JSONObject().put("servers",JSONArray().put("1.1.1.1").put("8.8.8.8")))
            .put("inbounds",JSONArray().put(inbound))
            .put("outbounds",JSONArray().put(out).put(direct).put(block))
            .put("routing",routing)
            .put("policy",JSONObject().put("levels",JSONObject().put("0",JSONObject().put("statsUserUplink",true).put("statsUserDownlink",true))))
            .put("stats",JSONObject())
            .toString()
    }
}
