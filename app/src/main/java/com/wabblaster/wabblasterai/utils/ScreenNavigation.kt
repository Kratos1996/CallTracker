package com.wabblaster.wabblasterai.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.wabblaster.wabblasterai.ui.callupdatecenter.CallUploadCenterActivity
import com.wabblaster.wabblasterai.service.CallService
import com.wabblaster.wabblasterai.service.ContactSyncService
import com.wabblaster.wabblasterai.service.ContactUpdateOnServer
import com.wabblaster.wabblasterai.service.ServiceRestarterService
import com.wabblaster.wabblasterai.ui.home.HomeActivity
import com.wabblaster.wabblasterai.ui.login.ui.login.LoginActivity
import com.wabblaster.wabblasterai.ui.restricted.AddNewContact
import com.wabblaster.wabblasterai.ui.restricted.ContactActivity
import com.wabblaster.wabblasterai.ui.restricted.RestrictedContactActivity

val settingApplicationCode = 1996
fun Context.navToHome(){
    val intent = Intent(this, HomeActivity::class.java)
    startActivity(intent)

}
fun Context.navToLogin(){
    val intent = Intent(this, LoginActivity::class.java)
    startActivity(intent)
}
fun Context.navToCallService(){
    val intent = Intent(this, CallService::class.java)
    startService(intent)
}
fun Context.navToRestrictContactActivity(){
    val intent = Intent(this, RestrictedContactActivity::class.java)
    startActivity(intent)
}
fun Context.navToContactActivity(){
    val intent = Intent(this, ContactActivity::class.java)
    startActivity(intent)
}
fun Context.navToUploadContactActivity(){
    val intent = Intent(this, CallUploadCenterActivity::class.java)
    startActivity(intent)
}
fun Context.navToSaveContactActivity(){
    val intent = Intent(this, AddNewContact::class.java)
    startActivity(intent)
}
fun Context.serviceContactUploadRestarter(){
    startService(Intent(this, ServiceRestarterService::class.java))
}
fun Context.serviceContact(){
    startService(Intent(this, ContactSyncService::class.java))
}
fun Context.stopServiceContactUpload(){
    stopService(Intent(this, ContactUpdateOnServer::class.java))
}
fun Context.stopServiceContact(){
    stopService(Intent(this, ContactSyncService::class.java))
}

fun Context.navToSetting(activity: AppCompatActivity){
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:" + packageName)
    )
    activity.startActivityForResult(intent,settingApplicationCode)
}

fun Context.addAutoStartup() {
    try {
        val intent = Intent()
        val manufacturer = Build.MANUFACTURER
        if ("xiaomi".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
        } else if ("oppo".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
        } else if ("vivo".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
        } else if ("Letv".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")
        } else if ("Honor".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
        }else if ("Huawei".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")
        }else if ("iqoo".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")
        }else if ("samsung".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
        }else if ("htc".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")
        }else if ("asus".equals(manufacturer, ignoreCase = true)) {
            intent.component = ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity")
        }
        val list: List<ResolveInfo> = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (list.size > 0 &&!(AppPreference.isRegister)) {
            AppPreference.isRegister = (true)
        }
    } catch (e: Exception) {
        AppPreference.isRegister = (false)
        Log.e("exc", e.toString())
    }
}
