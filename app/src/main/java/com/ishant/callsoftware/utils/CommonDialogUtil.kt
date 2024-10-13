package com.ishant.callsoftware.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import com.ishant.callsoftware.app.showAsBottomSheet
import com.ishant.callsoftware.databinding.DialogProgressBinding
import com.ishant.corelibcompose.toolkit.ui.commondialog.CommonAlertBottomSheet
import com.ishant.corelibcompose.toolkit.ui.snackbar.SnackBarSlideEffect
import com.ishant.corelibcompose.toolkit.ui.snackbar.custom.SnackBarType
import com.ishant.corelibcompose.toolkit.ui.snackbar.showSnackBar

fun showCommonDialog(title:String,message:String,context: Context,positiveClick:()->Unit){

    context.getActivityContext().showAsBottomSheet{dismiss ->
        CommonAlertBottomSheet(msg = message,
            onPositiveClick = {positiveClick.invoke() },
            onNegativeClick = {
                dismiss.invoke()
            })

    }
}
fun showLoadingDialog(context: Context,progressDialog: Dialog): Dialog {
    if (progressDialog.window != null) {
        val window = progressDialog.window
        window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.CENTER)
        progressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
    val binding: DialogProgressBinding = DialogProgressBinding.inflate(LayoutInflater.from(context))
    progressDialog.setContentView(binding.root)
    progressDialog.setCancelable(false)
    progressDialog.setCanceledOnTouchOutside(false)
    return progressDialog
}
fun Context.toast(message: String?,slideEffect: SnackBarSlideEffect = SnackBarSlideEffect.FROM_TOP) {
    if(!message.isNullOrEmpty()){
        this.getActivityContext().showSnackBar(
            message = message.trim(),
            isBrushEnable = false,
            snackBarType = SnackBarType.ERROR_SNACK_BAR,
            slideEffect = slideEffect
        )
    }
}