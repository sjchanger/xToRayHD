package app.xtorayhd.core

import android.content.Context
import java.lang.reflect.Method

class XrayEngine(private val context: Context) {
    private var controller: Any? = null
    private var running = false

    fun start(config: String): Boolean {
        if (running) return true

        return try {
            val coreClass = Class.forName("libv2ray.CoreCallbackHandler")

            try {
                val initMethod = findMethod(
                    coreClass,
                    "InitCoreEnv",
                    String::class.java,
                    String::class.java
                )

                if (initMethod != null) {
                    initMethod.invoke(
                        null,
                        context.filesDir.absolutePath,
                        context.cacheDir.absolutePath
                    )
                }
            } catch (_: Throwable) {
            }

            val controllerClass = Class.forName("libv2ray.CoreController")

            val factory = findMethod(
                controllerClass,
                "NewCoreController",
                String::class.java
            )

            if (factory != null) {
                controller = factory.invoke(null, config)
            } else {
                val constructors = controllerClass.constructors
                if (constructors.isNotEmpty()) {
                    controller = constructors.first().newInstance(config)
                }
            }

            invokeIfAvailable(controller, "StartLoop")
            running = true
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun stop() {
        try {
            invokeIfAvailable(controller, "StopLoop")
            invokeIfAvailable(controller, "Stop")
        } catch (_: Throwable) {
        }

        controller = null
        running = false
    }

    fun isRunning(): Boolean = running

    private fun findMethod(
        clazz: Class<*>,
        name: String,
        vararg parameterTypes: Class<*>
    ): Method? {
        return try {
            clazz.getMethod(name, *parameterTypes)
        } catch (_: NoSuchMethodException) {
            clazz.methods.firstOrNull {
                it.name == name && it.parameterTypes.size == parameterTypes.size
            }
        }
    }

    private fun invokeIfAvailable(target: Any?, methodName: String) {
        if (target == null) return

        try {
            val method = target.javaClass.methods.firstOrNull {
                it.name == methodName && it.parameterTypes.isEmpty()
            }
            method?.invoke(target)
        } catch (_: Throwable) {
        }
    }
}
