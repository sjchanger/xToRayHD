package app.xtorayhd.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import app.xtorayhd.R
import app.xtorayhd.core.ConfigBuilder
import app.xtorayhd.core.ProxyProfile
import app.xtorayhd.core.XrayEngine

class XrayVpnService : VpnService() {
    companion object {
        const val ACTION_START = "app.xtorayhd.START"
        const val ACTION_STOP = "app.xtorayhd.STOP"
        const val EXTRA_CONFIG = "config"
    }

    private var vpn: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTunnel(intent.getStringExtra(EXTRA_CONFIG) ?: return START_NOT_STICKY)
            ACTION_STOP -> stopTunnel()
        }
        return START_STICKY
    }

    private fun startTunnel(config: String) {
        createChannel()
        startForeground(7, notification("Connecting…"))
        stopTunnel(false)
        val prepared = prepare(this)
        if (prepared != null) return
        vpn = Builder()
            .setSession("xToRayHD")
            .setMtu(1500)
            .addAddress("10.0.0.2", 16)
            .addAddress("fd00::2", 64)
            .addRoute("0.0.0.0", 0)
            .addRoute("::", 0)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")
            .addDisallowedApplication(packageName)
            .establish()
        val fd = vpn?.fd ?: return stopTunnel()
        XrayEngine.start(config, fd).onFailure { stopTunnel() }
    }

    private fun stopTunnel(notify: Boolean = true) {
        XrayEngine.stop()
        vpn?.close()
        vpn = null
        if (notify) stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        stopTunnel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("vpn","xToRayHD",NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun notification(text: String): Notification =
        NotificationCompat.Builder(this,"vpn")
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("xToRayHD")
            .setContentText(text)
            .setOngoing(true)
            .build()
}
