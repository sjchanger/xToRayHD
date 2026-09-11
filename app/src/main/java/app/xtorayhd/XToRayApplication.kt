package app.xtorayhd

import android.app.Application
import app.xtorayhd.core.XrayEngine

class XToRayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        XrayEngine.init(this)
    }
}
