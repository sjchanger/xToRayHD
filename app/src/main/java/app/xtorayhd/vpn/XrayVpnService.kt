package app.xtorayhd.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import app.xtorayhd.R

class XrayVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    companion object {
        private const val CHANNEL_ID = "xtorayhd_vpn"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        establishVpn()
        return START_STICKY
    }

    private fun establishVpn() {
        if (vpnInterface != null) return

        vpnInterface = Builder()
            .setSession("xToRayHD")
            .setMtu(1500)
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")
            .establish()
    }

    override fun onDestroy() {
        vpnInterface?.close()
        vpnInterface = null
        super.onDestroy()
    }

    override fun onRevoke() {
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
        super.onRevoke()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "xToRayHD VPN",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "xToRayHD VPN connection"
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("xToRayHD")
                .setContentText("VPN connection active")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("xToRayHD")
                .setContentText("VPN connection active")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build()
        }
    }
}
