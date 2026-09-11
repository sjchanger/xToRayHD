package app.xtorayhd.core

import android.content.Context
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

object XrayEngine {
    private lateinit var app: Context
    private var controller: Any? = null
    private val running = AtomicBoolean(false)

    fun init(context: Context) {
        app = context.applicationContext
        try {
            libv2ray.InitCoreEnv(app.filesDir.absolutePath, "")
        } catch (e: Throwable) {
            Log.e("xToRayHD", "Xray environment init failed", e)
        }
    }

    fun start(config: String, tunFd: Int): Result<Unit> = runCatching {
        val callback = object : libv2ray.CoreCallbackHandler {
            override fun startup(): Long = 0L
            override fun shutdown(): Long = 0L
            override fun onEmitStatus(p0: Long, p1: String?): Long = 0L
        }
        val c = libv2ray.NewCoreController(callback)
        val method = c.javaClass.methods.firstOrNull { it.name.equals("startLoop", true) && it.parameterTypes.size == 2 }
        if (method != null) method.invoke(c, config, tunFd)
        else error("Xray AAR does not expose Android TUN startLoop(config, tunFd)")
        controller = c
        running.set(true)
    }

    fun stop() {
        try {
            controller?.javaClass?.methods?.firstOrNull { it.name.equals("stopLoop", true) }?.invoke(controller)
        } catch (e: Throwable) {
            Log.e("xToRayHD", "Xray stop failed", e)
        } finally {
            controller = null
            running.set(false)
        }
    }

    fun isRunning() = running.get()
}
