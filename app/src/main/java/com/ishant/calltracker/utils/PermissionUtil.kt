import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.ishant.calltracker.R
import com.ishant.calltracker.utils.getActivityContext
import com.ishant.calltracker.utils.navToSetting
import com.ishant.calltracker.utils.showCommonDialog

fun Context.readPhoneStatePermission(granted:()->Unit, rejected:(() -> Unit)? = null){
    takePermissions(
        permissions = Manifest.permission.READ_PHONE_STATE,
        title = getString(R.string.phone_state_permission),
        granted = granted,
        rejected =rejected
    )
}

fun Context.takeForegroundService(granted:()->Unit, rejected:(() -> Unit)? = null){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        takePermissions(
            permissions = Manifest.permission.FOREGROUND_SERVICE,
            title = getString(R.string.phone_foreground_permission),
            granted = granted,
            rejected = rejected
        )
    }else {
        granted()
    }
}

fun Context.takeForegroundCallService(granted:()->Unit, rejected:(() -> Unit)? = null){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        takePermissions(
            permissions = Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL,
            title = getString(R.string.phone_foreground_permission),
            granted = granted,
            rejected = rejected
        )
    }else{
        granted()
    }
}fun Context.takeForegroundContactService(granted:()->Unit, rejected:(() -> Unit)? = null){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        takePermissions(
            permissions = Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC,
            title = getString(R.string.contact_permission),
            granted = granted,
            rejected = rejected
        )
    }else{
        granted()
    }
}

fun Context.readPhoneLogPermission(granted:()->Unit, rejected:(() -> Unit)? = null){
    takePermissions(
        permissions = Manifest.permission.READ_CALL_LOG,
        title = getString(R.string.read_call_log_permission),
        granted = granted,
        rejected =rejected
    )
}

fun Context.readPhoneNumberPermission(granted:()->Unit, rejected:(() -> Unit)? = null ){
    takePermissions(
        permissions = Manifest.permission.READ_PHONE_NUMBERS,
        title = getString(R.string.phone_number_permission),
        granted = granted,
        rejected =rejected
    )
}

fun Context.readPhoneStorage(granted:()->Unit, rejected:(() -> Unit)? = null ){
    takePermissions(
        permissions = Manifest.permission.READ_EXTERNAL_STORAGE,
        title = getString(R.string.read_device_permission),
        granted = granted,
        rejected =rejected
    )
}

fun Context.writePhoneStorage(granted:()->Unit, rejected:(() -> Unit)? = null ){
    takePermissions(
        permissions = Manifest.permission.WRITE_EXTERNAL_STORAGE,
        title = getString(R.string.write_device_permission),
        granted = granted,
        rejected =rejected
    )
}

fun Context.recordAudioCallPermission(granted:()->Unit, rejected:(() -> Unit)? = null ){
    takePermissions(
        permissions = Manifest.permission.RECORD_AUDIO,
        title = getString(R.string.read_call_record_permission),
        granted = granted,
        rejected =rejected
    )
}

fun isNotificationPermissionGranted(context: Context): Boolean {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationManager.areNotificationsEnabled()
    } else {
        // For versions before Oreo, there's no specific notification permission
        true
    }
}
fun requestNotificationPermission(context: Context) {
    val intent = Intent().apply {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 -> {
                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                putExtra("app_package", context.packageName)
                putExtra("app_uid", context.applicationInfo.uid)
            }
            else -> {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                addCategory(Intent.CATEGORY_DEFAULT)
                data = Uri.parse("package:" + context.packageName)
            }
        }
    }
    context.startActivity(intent)
}



fun Context.readPhoneContactPermission(granted:()->Unit, rejected:(() -> Unit)? = null ){
    takePermissions(
        permissions = Manifest.permission.READ_CONTACTS,
        title = getString(R.string.read_contact_permission),
        granted = granted,
        rejected =rejected
    )
}

fun Context.writePhoneContactPermission(granted:()->Unit, rejected:(() -> Unit)? = null ){
    takePermissions(
        permissions = Manifest.permission.WRITE_CONTACTS,
        title = getString(R.string.read_write_contact_permission),
        granted = granted,
        rejected =rejected
    )
}
private fun Context.takePermissions(permissions :String, title:String, granted:()->Unit, rejected:(() -> Unit)? = null){
    val context = this
    Dexter.withContext(this)
        .withPermission(permissions)
        .withListener(object : PermissionListener {
            override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                granted()
            }
            override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
                rejected?.let { it() }
                if(permissionDeniedResponse.isPermanentlyDenied){
                   /* showCommonDialog(title = getString(R.string.required_permission),message = title ,context){
                        navToSetting(context.getActivityContext())
                    }*/
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissionRequest: PermissionRequest,
                permissionToken: PermissionToken
            ) {
                permissionToken.continuePermissionRequest()
                rejected?.let { it() }
                /*showCommonDialog(title = getString(R.string.required_permission),message = title,context){
                    navToSetting(context.getActivityContext())
                }*/
            }
        })
        .withErrorListener {
            rejected?.let { it() }
           /* showCommonDialog(title = getString(R.string.required_permission),message = title,context){
                navToSetting(context.getActivityContext())
            }*/
        }
        .check()
}