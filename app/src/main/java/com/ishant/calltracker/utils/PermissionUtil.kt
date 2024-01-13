import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.ishant.calltracker.R
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
                    showCommonDialog(title = getString(R.string.required_permission),message = title ,context){
                        navToSetting(context as AppCompatActivity)
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissionRequest: PermissionRequest,
                permissionToken: PermissionToken
            ) {
                permissionToken.continuePermissionRequest()
                rejected?.let { it() }
                showCommonDialog(title = getString(R.string.required_permission),message = title,context){
                    navToSetting(context as AppCompatActivity)
                }
            }
        })
        .withErrorListener {
            rejected?.let { it() }
            showCommonDialog(title = getString(R.string.required_permission),message = title,context){
                navToSetting(context as AppCompatActivity)
            }
        }
        .check()
}