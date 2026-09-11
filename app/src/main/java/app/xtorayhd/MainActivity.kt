package app.xtorayhd

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.xtorayhd.core.ConfigBuilder
import app.xtorayhd.core.ProfileStore
import app.xtorayhd.core.ProxyProfile
import app.xtorayhd.core.SubscriptionParser
import app.xtorayhd.vpn.XrayVpnService
import java.util.UUID

private val Bg = Color(0xFF070A10)
private val Surface = Color(0xFF10151E)
private val Surface2 = Color(0xFF171D28)
private val Cyan = Color(0xFF69E7FF)
private val Emerald = Color(0xFF45E0A4)
private val Text = Color(0xFFF4F7FA)
private val Muted = Color(0xFF8994A5)

class MainActivity : ComponentActivity() {
    private lateinit var store: ProfileStore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = ProfileStore(this)
        setContent { XToRayApp() }
    }

    @Composable
    private fun XToRayApp() {
        var profiles by remember { mutableStateOf(store.load()) }
        var active by remember { mutableStateOf(profiles.firstOrNull()) }
        var connected by remember { mutableStateOf(false) }
        var tab by remember { mutableIntStateOf(0) }
        var importOpen by remember { mutableStateOf(false) }
        var input by remember { mutableStateOf("") }
        val vpnLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && active != null) {
                startVpn(active!!)
                connected = true
            }
        }

        MaterialTheme(colorScheme = darkColorScheme(background=Bg,surface=Surface,primary=Cyan,secondary=Emerald,onBackground=Text,onSurface=Text)) {
            Surface(Modifier.fillMaxSize(), color=Bg) {
                Column(Modifier.fillMaxSize().statusBarsPadding()) {
                    TopBar()
                    AnimatedContent(tab, transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) }, label="tab") {
                        when (it) {
                            0 -> Home(profiles, active, connected, onConnect = {
                                if (active == null) { Toast.makeText(this,"Add a profile first",Toast.LENGTH_SHORT).show(); return@Home }
                                if (connected) {
                                    stopVpn(); connected=false
                                } else {
                                    val p = VpnService.prepare(this)
                                    if (p == null) { startVpn(active!!); connected=true } else vpnLauncher.launch(p)
                                }
                            }, onSelect={active=it}, onImport={importOpen=true})
                            1 -> Profiles(profiles, active, onSelect={active=it}, onDelete={
                                profiles=profiles.filterNot { x->x.id==it.id }; if(active?.id==it.id) active=profiles.firstOrNull(); store.save(profiles)
                            }, onImport={importOpen=true})
                            else -> SettingsPage()
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    NavigationBar(containerColor=Surface) {
                        NavItem(Icons.Rounded.Dashboard,"Home",tab==0){tab=0}
                        NavItem(Icons.Rounded.Wifi,"Profiles",tab==1){tab=1}
                        NavItem(Icons.Rounded.Settings,"Settings",tab==2){tab=2}
                    }
                }
                if (importOpen) {
                    ImportSheet(input,onInput={input=it},onDismiss={importOpen=false},onImport={
                        val parsed=SubscriptionParser.parse(input)
                        if(parsed.isNotEmpty()){
                            profiles=(profiles+parsed).distinctBy{it.rawUri}
                            active=active ?: parsed.first()
                            store.save(profiles); input=""; importOpen=false
                        } else Toast.makeText(this,"No supported VLESS / VMess / Trojan / Shadowsocks profiles found",Toast.LENGTH_LONG).show()
                    })
                }
            }
        }
    }

    private fun startVpn(p: ProxyProfile) {
        val i=Intent(this,XrayVpnService::class.java).setAction(XrayVpnService.ACTION_START).putExtra(XrayVpnService.EXTRA_CONFIG,ConfigBuilder.build(p))
        ContextCompat.startForegroundService(this,i)
    }
    private fun stopVpn() {
        startService(Intent(this,XrayVpnService::class.java).setAction(XrayVpnService.ACTION_STOP))
    }
}

@Composable private fun TopBar() {
    Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=14.dp),verticalAlignment=Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Cyan.copy(.12f)),contentAlignment=Alignment.Center) {
            Text("x",color=Cyan,fontSize=23.sp,fontWeight=FontWeight.Black)
        }
        Spacer(Modifier.width(12.dp))
        Column { Text("xToRayHD",fontSize=21.sp,fontWeight=FontWeight.Bold); Text("Premium tunnel",color=Muted,fontSize=12.sp) }
    }
}

@Composable private fun Home(profiles:List<ProxyProfile>,active:ProxyProfile?,connected:Boolean,onConnect:()->Unit,onSelect:(ProxyProfile)->Unit,onImport:()->Unit) {
    LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item {
            GlassCard {
                Text(if(connected)"Connected" else "Disconnected",color=if(connected)Emerald else Muted,fontSize=13.sp,fontWeight=FontWeight.SemiBold)
                Text(active?.name ?: "No active profile",fontSize=24.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=6.dp))
                Text(active?.let{"${it.protocol}  •  ${it.server}:${it.port}"} ?: "Import a subscription or profile",color=Muted,fontSize=13.sp,modifier=Modifier.padding(top=4.dp))
                Spacer(Modifier.height(20.dp))
                Button(onClick=onConnect,modifier=Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=if(connected) Surface2 else Cyan,contentColor=if(connected) Text else Bg)) {
                    Icon(Icons.Rounded.Wifi,null); Spacer(Modifier.width(9.dp)); Text(if(connected)"Disconnect" else "Connect",fontWeight=FontWeight.Bold)
                }
            }
        }
        item { SectionTitle("Quick profiles") }
        items(profiles.take(5),key={it.id}) { p ->
            ProfileRow(p,p.id==active?.id,onSelect={onSelect(p)})
        }
        item {
            OutlinedButton(onClick=onImport,modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(17.dp)) {
                Icon(Icons.Rounded.Add,null); Spacer(Modifier.width(8.dp)); Text("Add subscription / config")
            }
        }
    }
}

@Composable private fun Profiles(profiles:List<ProxyProfile>,active:ProxyProfile?,onSelect:(ProxyProfile)->Unit,onDelete:(ProxyProfile)->Unit,onImport:()->Unit) {
    LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("Your profiles") }
        items(profiles,key={it.id}) { p ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(if(p.id==active?.id)Cyan.copy(.10f) else Surface).clickable{onSelect(p)}.padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(p.name,fontWeight=FontWeight.SemiBold); Text("${p.protocol} • ${p.server}:${p.port}",color=Muted,fontSize=12.sp) }
                IconButton(onClick={onDelete(p)}) { Icon(Icons.Rounded.Delete,null,tint=Muted) }
            }
        }
        item { OutlinedButton(onClick=onImport,modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(17.dp)){Icon(Icons.Rounded.Add,null);Spacer(Modifier.width(8.dp));Text("Import")}}
    }
}

@Composable private fun SettingsPage() {
    Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        SectionTitle("Settings")
        GlassCard {
            SettingRow("Routing","Full device VPN")
            SettingRow("DNS","1.1.1.1 • 8.8.8.8")
            SettingRow("Core","Xray")
            SettingRow("Motion","0.5× premium motion")
        }
    }
}

@Composable private fun ImportSheet(value:String,onInput:(String)->Unit,onDismiss:()->Unit,onImport:()->Unit) {
    AlertDialog(onDismissRequest=onDismiss,title={Text("Add configuration")},text={
        Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text("Paste a subscription URL, base64 subscription, or supported share links.",color=Muted,fontSize=12.sp)
            OutlinedTextField(value,onInput,modifier=Modifier.fillMaxWidth().height(150.dp),placeholder={Text("vless://… or subscription URL")})
            val clip=LocalClipboardManager.current
            TextButton(onClick={clip.getText()?.let{onInput(it.text)}}){Icon(Icons.Rounded.ContentPaste,null);Spacer(Modifier.width(5.dp));Text("Paste clipboard")}
        }
    },confirmButton={Button(onClick=onImport){Text("Import")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancel")}})
}

@Composable private fun ProfileRow(p:ProxyProfile,selected:Boolean,onSelect:()->Unit) {
    val c by animateColorAsState(if(selected)Cyan.copy(.13f) else Surface,animationSpec=tween(250,easing=FastOutSlowInEasing),label="row")
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(19.dp)).background(c).clickable{onSelect()}.padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(if(selected)Cyan.copy(.18f) else Surface2),contentAlignment=Alignment.Center){Icon(Icons.Rounded.Speed,null,tint=if(selected)Cyan else Muted)}
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)){Text(p.name,fontWeight=FontWeight.SemiBold);Text("${p.protocol} • ${p.server}:${p.port}",color=Muted,fontSize=12.sp)}
        if(selected) Text("ACTIVE",color=Cyan,fontSize=10.sp,fontWeight=FontWeight.Bold)
    }
}

@Composable private fun GlassCard(content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Surface).padding(20.dp),content=content)
}
@Composable private fun SectionTitle(t:String){Text(t,fontSize=14.sp,color=Muted,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(vertical=2.dp))}
@Composable private fun SettingRow(a:String,b:String){Row(Modifier.fillMaxWidth().padding(vertical=8.dp)){Text(a,Modifier.weight(1f));Text(b,color=Muted,fontSize=13.sp)}}
@Composable private fun RowScope.NavItem(icon:androidx.compose.ui.graphics.vector.ImageVector,label:String,selected:Boolean,onClick:()->Unit){NavigationBarItem(selected,onClick,icon={Icon(icon,null)},label={Text(label)})}
