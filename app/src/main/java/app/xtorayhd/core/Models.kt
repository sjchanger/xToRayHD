package app.xtorayhd.core

data class ProxyProfile(
    val id: String,
    val name: String,
    val protocol: String,
    val server: String,
    val port: Int,
    val rawUri: String,
    val configJson: String
)

data class AppState(
    val profiles: List<ProxyProfile> = emptyList(),
    val activeId: String? = null,
    val connected: Boolean = false,
    val trafficUp: Long = 0,
    val trafficDown: Long = 0,
    val status: String = "Ready"
)
