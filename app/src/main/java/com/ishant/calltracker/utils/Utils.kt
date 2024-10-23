package com.ishant.calltracker.utils

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import android.text.TextUtils
import com.ishant.calltracker.utils.constant.Constant.DEFAULT_GSM_CELL_ID_VALUE
import com.ishant.calltracker.utils.constant.Constant.DEFAULT_TELEPHONY_MANAGER_INT_VALUE
import com.ishant.calltracker.utils.constant.Constant.DEFAULT_TELEPHONY_MANAGER_STRING_VALUE
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


internal object Utils {
    fun getTelephonyManagerValues(
        context: Context?, telephony: TelephonyManager?, methodName: String?, simSlotId: Int
    ): String? {
        if (context == null) return DEFAULT_TELEPHONY_MANAGER_STRING_VALUE
        val telephonyClass: Class<*>
        var reflectionMethod: String? = null
        var output: String? = null
        if (telephony != null) {
            try {
                telephonyClass = Class.forName(telephony.javaClass.name)
                for (method in telephonyClass.methods) {
                    val name = method.name
                    if (name.equals(methodName, ignoreCase = true) /*name.contains(methodName)*/) {
                        val params = method.parameterTypes
                        if (params.size == 1 && params[0].name == "int") {
                            reflectionMethod = name
                        }
                    }
                }
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
        if (reflectionMethod != null) {
            try {
                output = getOutputByReflection(telephony, reflectionMethod, simSlotId, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return output
    }
    fun getCurrentDate(format: String="dd/MM/yyyy"): String {
        val formatter = SimpleDateFormat(format, Locale("en"))
        val date = Date()
        return formatter.format(date)
    }

    private fun getOutputByReflection(
        telephony: TelephonyManager?, predictedMethodName: String, slotID: Int, isPrivate: Boolean
    ): String {
        var result: String = DEFAULT_TELEPHONY_MANAGER_STRING_VALUE
        try {
            val telephonyClass = Class.forName(telephony!!.javaClass.name)
            val parameter = arrayOfNulls<Class<*>?>(1)
            parameter[0] = Int::class.javaPrimitiveType
            val getSimID: Method?
            getSimID = if (slotID != -1) {
                if (isPrivate) {
                    telephonyClass.getDeclaredMethod(predictedMethodName, *parameter)
                } else {
                    telephonyClass.getMethod(predictedMethodName, *parameter)
                }
            } else {
                if (isPrivate) {
                    telephonyClass.getDeclaredMethod(predictedMethodName)
                } else {
                    telephonyClass.getMethod(predictedMethodName)
                }
            }
            val ob_phone: Any?
            val obParameter = arrayOfNulls<Any>(1)
            obParameter[0] = slotID
            if (getSimID != null) {
                ob_phone = if (slotID != -1) {
                    getSimID.invoke(telephony, *obParameter)
                } else {
                    getSimID.invoke(telephony)
                }
                if (ob_phone != null) {
                    result = ob_phone.toString()
                }
            }
        } catch (e: Exception) {
            return DEFAULT_TELEPHONY_MANAGER_STRING_VALUE
        }
        return if (TextUtils.isEmpty(result)) {
            DEFAULT_TELEPHONY_MANAGER_STRING_VALUE
        } else result
    }

    fun getCellLocationValue(context: Context, cellLocation: String, cellLocationType: Int): Int {
        var cellLocation = cellLocation
        val splitedCellLocation: Array<String>
        if (isTelephonyManagerValueValid(cellLocation)) {
            try {
                cellLocation = cellLocation.replace("[\\[\\]]".toRegex(), "")
                splitedCellLocation =
                    cellLocation.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                if (splitedCellLocation.isNotEmpty()) {
                    val value = splitedCellLocation[cellLocationType]
                    if (isNumeric(value)) {
                        return value.toInt()
                    }
                }
            } catch (e: Exception) {
            }
        }
        return DEFAULT_GSM_CELL_ID_VALUE
    }

    fun isNumeric(stringNumber: String): Boolean {
        return !TextUtils.isEmpty(stringNumber) && stringNumber.matches("[-+]?\\d*\\.?\\d+".toRegex())
    }

    private fun isTelephonyManagerValueValid(value: String): Boolean {
        return !TextUtils.isEmpty(value)
    }

    fun getMncFromNetworkOperator(simOperatorCode: String): Int {
        if (isNumeric(simOperatorCode)) {
            if (simOperatorCode.length >= 5) {
                try {
                    return simOperatorCode.substring(3).toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return DEFAULT_TELEPHONY_MANAGER_INT_VALUE
    }

    fun getMccFromNetworkOperator(simOperatorCode: String): Int {
        if (isNumeric(simOperatorCode)) {
            if (simOperatorCode.length > 3) {
                try {
                    return simOperatorCode.substring(0, 3).toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return DEFAULT_TELEPHONY_MANAGER_INT_VALUE
    }

    fun getAppTargetSdkVersion(context: Context?): Int {
        var version = Build.VERSION_CODES.Q
        if (context != null) {
            val pm = context.packageManager
            try {
                if (pm != null) {
                    val applicationInfo = pm.getApplicationInfo(context.packageName, 0)
                    if (applicationInfo != null) {
                        version = applicationInfo.targetSdkVersion
                    }
                }
            } catch (e: Exception) {
            }
        }
        return version
    }

    fun isLowerThanAndroidQ(context: Context?): Boolean {
        return (getAppTargetSdkVersion(context) < Build.VERSION_CODES.Q
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
    }
    fun extractLast10Digits(phoneNumber: String): String {
        // Remove non-digit characters
        val digitsOnly = phoneNumber.replace(Regex("[^\\d]"), "")

        // Extract the last 10 digits
        return if (digitsOnly.length >= 10) {
            digitsOnly.substring(digitsOnly.length - 10)
        } else {
            // Handle the case when the string has less than 10 digits
            digitsOnly
        }
    }


    internal interface CellLocationType {
        companion object {
            const val LAC = 0
            const val CID = 1
            const val PSC = 2
        }
    }
}