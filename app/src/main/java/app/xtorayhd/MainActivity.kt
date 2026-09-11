package app.xtorayhd

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.xtorayhd.vpn.XrayVpnService

private val Background = Color(0xFF070A10)
private val SurfaceColor = Color(0xFF10151E)
private val SurfaceColor2 = Color(0xFF171D28)
private val Cyan = Color(0xFF69E7FF)
private val Emerald = Color(0xFF45E0A4)
private val TextPrimary = Color(0xFFF4F7FA)
private val TextMuted = Color(0xFF8994A5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XToRayHDApp()
        }
    }
}

@androidx.compose.runtime.Composable
private fun XToRayHDApp() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var connected by remember { mutableStateOf(false) }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        androidx.compose.animation.fadeIn(tween(350)) togetherWith
                            androidx.compose.animation.fadeOut(tween(250))
                    },
                    label = "page"
                ) { page ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (page) {
                            0 -> HomeScreen(
                                connected = connected,
                                onConnect = {
                                    val intent = Intent(context, XrayVpnService::class.java)
                                    androidx.core.content.ContextCompat.startForegroundService(context, intent)
                                    connected = true
                                },
                                onDisconnect = {
                                    context.stopService(Intent(context, XrayVpnService::class.java))
                                    connected = false
                                }
                            )
                            1 -> ProfilesScreen()
                            else -> SettingsScreen()
                        }
                    }
                }

                BottomNavigation(
                    selectedTab = selectedTab,
                    onSelected = { selectedTab = it }
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun HomeScreen(
    connected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "xToRayHD",
                    color = TextPrimary,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Premium Xray Client",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(SurfaceColor2, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = if (connected) Emerald else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(42.dp))

        val statusColor by animateColorAsState(
            targetValue = if (connected) Emerald else TextMuted,
            animationSpec = tween(500),
            label = "statusColor"
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .background(SurfaceColor, RoundedCornerShape(110.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(94.dp)
                        .background(
                            statusColor.copy(alpha = 0.12f),
                            RoundedCornerShape(47.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (connected) Icons.Default.Wifi else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (connected) "Connected" else "Disconnected",
                    color = statusColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(34.dp))

        Text(
            text = if (connected) "xToRayHD VPN is active" else "Ready to connect",
            color = TextMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = if (connected) onDisconnect else onConnect,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (connected) SurfaceColor2 else Cyan,
                contentColor = if (connected) TextPrimary else Background
            )
        ) {
            Icon(
                imageVector = if (connected) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (connected) "Disconnect" else "Connect",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "No profile selected",
            color = TextMuted,
            fontSize = 13.sp
        )
    }
}

@androidx.compose.runtime.Composable
private fun ProfilesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Profiles",
            color = TextPrimary,
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Manage your Xray servers",
            color = TextMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceColor, RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Cyan.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Cyan
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Add Premium Server",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Import a VLESS, VMess, Trojan or Shadowsocks profile",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Settings",
            color = TextPrimary,
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "xToRayHD configuration",
            color = TextMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        SettingRow("Auto reconnect", "Reconnect when the network changes")
        SettingRow("VPN mode", "System VPN")
        SettingRow("DNS", "Automatic")
        SettingRow("Routing", "Global")
        SettingRow("About", "xToRayHD Premium")
    }
}

@androidx.compose.runtime.Composable
private fun SettingRow(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun BottomNavigation(
    selectedTab: Int,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceColor)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NavigationItem(
            selected = selectedTab == 0,
            icon = Icons.Default.Home,
            label = "Home",
            onClick = { onSelected(0) }
        )

        NavigationItem(
            selected = selectedTab == 1,
            icon = Icons.Default.Wifi,
            label = "Profiles",
            onClick = { onSelected(1) }
        )

        NavigationItem(
            selected = selectedTab == 2,
            icon = Icons.Default.Settings,
            label = "Settings",
            onClick = { onSelected(2) }
        )
    }
}

@androidx.compose.runtime.Composable
private fun NavigationItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(
        targetValue = if (selected) Cyan else TextMuted,
        animationSpec = tween(350),
        label = "navigationTint"
    )

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(23.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = tint,
            fontSize = 11.sp
        )
    }
}
