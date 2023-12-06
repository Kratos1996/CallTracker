package com.ishant.calltracker.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.CellLocation
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.telephony.gsm.GsmCellLocation
import android.text.TextUtils.isEmpty
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.ishant.calltracker.utils.Constant.DEFAULT_TELEPHONY_MANAGER_INT_VALUE
import com.ishant.calltracker.utils.Constant.DEFAULT_TELEPHONY_MANAGER_STRING_VALUE
import com.ishant.calltracker.utils.Constant.TELEPHONY_MANAGER_CELL_LOCATION
import com.ishant.calltracker.utils.Constant.TELEPHONY_MANAGER_CELL_LOCATION_BY_SUB_ID
import com.ishant.calltracker.utils.Constant.TELEPHONY_MANAGER_IMEI
import com.ishant.calltracker.utils.Constant.TELEPHONY_MANAGER_SIM_OPERATOR
import com.ishant.calltracker.utils.Constant.TELEPHONY_MANAGER_SIM_OPERATOR_NAME
import com.ishant.calltracker.utils.Constant.TELEPHONY_MANAGER_SIM_SERIAL_NUMBER
import com.ishant.calltracker.utils.Constant.TELEPHONY_MANAGER_SUBSCRIBERID
import com.ishant.calltracker.utils.Utils.getCellLocationValue
import com.ishant.calltracker.utils.Utils.getTelephonyManagerValues
import com.ishant.calltracker.utils.Utils.isLowerThanAndroidQ

data class TelePhoneNumberData(val phoneNumber:String,val carrierName:String,val cardId:String,val simSlotIndex :Int)
class TelephonyManagerPlus  constructor(private val mContext: Context) {
    private val mTelephonyManager: TelephonyManager?
    private var simSlot2 = 1
    var mSubscriptionInfoList: List<SubscriptionInfo>?
    init {
        mTelephonyManager = mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        mSubscriptionInfoList = subscriptionManager
        findSimSlot2()
        //printSubscriptionInfo();
    }

    fun getSimCardPhoneNumbers(context: Context): List<TelePhoneNumberData> {

        val phoneNumbers = mutableListOf<String?>()
        val phoneNumbersDetails = mutableListOf<TelePhoneNumberData>()

        val subscriptionManager = SubscriptionManager.from(mContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val activeSubscriptions = if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return arrayListOf()
            } else {
                subscriptionManager.activeSubscriptionInfoList
            }

            Log.e("CallTracker : " , "subscriptionList : ${Gson().toJson(activeSubscriptions)}")
            activeSubscriptions?.forEach { subscriptionInfo: SubscriptionInfo ->
                val phoneNumber = subscriptionInfo.number
                phoneNumbers.add(phoneNumber)
                phoneNumbersDetails.add(TelePhoneNumberData(phoneNumber,subscriptionInfo.carrierName.toString(),subscriptionInfo.cardId.toString(),subscriptionInfo.simSlotIndex))
            }
        }
       // Log.e("CallTracker : phoneNumbers" , "phoneNumbers ${Gson().toJson(phoneNumbers)}")
        Log.e("CallTracker : phoneNumbers" , "phoneNumbers ${Gson().toJson(phoneNumbersDetails)}")
        return phoneNumbersDetails
    }

    private fun findSimSlot2() {
        if (isPhoneStatePermissionGranted) {
            val simSerialNumber1 = simSerialNumber1
            getTelephonyInfo(simSerialNumber1, TELEPHONY_MANAGER_SIM_SERIAL_NUMBER, true)
        } else {
            val simOperator1 = simOperatorCode1
            getTelephonyInfo(simOperator1, TELEPHONY_MANAGER_SIM_OPERATOR, true)
        }
    }

    private val isPhoneStatePermissionGranted: Boolean
        private get() {
            if (ActivityCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return false
            }
            return true
        }
    private val isLocationPermissionGranted: Boolean
        private get() {
            if (ActivityCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return false
            }
            return true
        }

    private fun getTelephonyInfo(sim1Value: String, methodName: String, indexSet: Boolean): String {
        try {
            for (i in 0..6) {
                val simValue2: String = getTelephonyManagerValues(
                    mContext, mTelephonyManager, methodName, i
                )?:""
                if (!isEmpty(simValue2)
                    && simValue2 != "0"
                    && simValue2 != "-1"
                ) {
                    if (methodName == TELEPHONY_MANAGER_SIM_OPERATOR && simValue2.length > 6) {
                        continue
                    }
                    if (simValue2.length > 0 &&
                        simValue2 != sim1Value
                    ) {
                        if (indexSet) {
                            simSlot2 = i
                        }
                        return simValue2
                    }
                }
            }
        } catch (e: Exception) {

        }
        return DEFAULT_TELEPHONY_MANAGER_STRING_VALUE
    }

    private fun getCorrectSim2TelephonyInfo(sim1Value: String, methodName: String): String {
        val telephonyManagerValue: String = getTelephonyManagerValues(
            mContext, mTelephonyManager, methodName, simSlot2
        )?:""
        return if ((!isEmpty(telephonyManagerValue)
                    && (telephonyManagerValue == sim1Value))
        ) getTelephonyInfo(sim1Value, TELEPHONY_MANAGER_IMEI, false) else telephonyManagerValue
    }

    @get:RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    val isDualSim: Boolean
        get() = !isEmpty(imei2)

    @get:RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    val simSerialNumber1: String
        get() {
            if (isLowerThanAndroidQ(mContext)) {
                if (isPhoneStatePermissionGranted && mTelephonyManager != null) {
                    return mTelephonyManager.simSerialNumber
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (mSubscriptionInfoList != null
                        && mSubscriptionInfoList!!.size > 0
                    ) {
                        return mSubscriptionInfoList!![0].iccId
                    }
                }
            }
            return DEFAULT_TELEPHONY_MANAGER_STRING_VALUE
        }

    @get:RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    val simSerialNumber2: String
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (mSubscriptionInfoList != null
                    && mSubscriptionInfoList!!.size > 1
                ) {
                    return mSubscriptionInfoList!![1].iccId
                }
            }
            return getCorrectSim2TelephonyInfo(
                simSerialNumber1,
                TELEPHONY_MANAGER_SIM_SERIAL_NUMBER
            )
        }
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getPhoneNumber(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android Oreo (API 26) and above
            mTelephonyManager?.line1Number
        } else {
            // For versions below Oreo
            @Suppress("DEPRECATION")
            mTelephonyManager?.line1Number
        }
    }
    val simOperatorCode1: String
        get() = mTelephonyManager!!.simOperator
    val simOperatorCode2: String?
        get() {
            if (isLowerThanAndroidQ(mContext)) {
                return getCorrectSim2TelephonyInfo(simOperatorCode1, TELEPHONY_MANAGER_SIM_OPERATOR)
            }
            val mnc = getMncFromSubscriptionList(1)
            val mcc = getMccFromSubscriptionList(1)
            return if ((mnc != DEFAULT_TELEPHONY_MANAGER_INT_VALUE
                        && mcc != DEFAULT_TELEPHONY_MANAGER_INT_VALUE)
            ) {
                "" + mcc + mnc
            } else null
        }
    val simOperatorName1: String
        get() = mTelephonyManager!!.simOperatorName
    val simOperatorName2: String
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (mSubscriptionInfoList != null
                    && mSubscriptionInfoList!!.size > 1
                ) {
                    return mSubscriptionInfoList!![1].carrierName.toString()
                }
            }
            return getCorrectSim2TelephonyInfo(
                simOperatorName1,
                TELEPHONY_MANAGER_SIM_OPERATOR_NAME
            )
        }

    @get:RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    val imei1: String
        get() {
            if (isLowerThanAndroidQ(mContext)) {
                if (isPhoneStatePermissionGranted && mTelephonyManager != null) {
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mTelephonyManager.imei
                    } else mTelephonyManager.deviceId
                }
            }
            return DEFAULT_TELEPHONY_MANAGER_STRING_VALUE
        }

    @get:RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    val imei2: String
        get() {
            if (isPhoneStatePermissionGranted) {
                if (isLowerThanAndroidQ(mContext)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        return mTelephonyManager!!.getImei(1)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        return mTelephonyManager!!.getDeviceId(1)
                    }
                }
                return getCorrectSim2TelephonyInfo(imei1, TELEPHONY_MANAGER_IMEI)
            }
            return DEFAULT_TELEPHONY_MANAGER_STRING_VALUE
        }

    @get:RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    private val cellLocation1: CellLocation?
        private get() {
            if (isLowerThanAndroidQ(mContext)) {
                if (isLocationPermissionGranted
                    && mTelephonyManager != null
                ) return mTelephonyManager.cellLocation
            }
            return null
        }

    @get:RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    private val cellLocation1ToString: String
        private get() {
            return if ((isLocationPermissionGranted
                        && mTelephonyManager != null)
            ) "" + cellLocation1 else DEFAULT_TELEPHONY_MANAGER_STRING_VALUE
        }

    @get:RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    private val cellLocation2: String
        private get() {
            if (isLocationPermissionGranted) {
                val cellLocation1: String = getTelephonyManagerValues(
                    mContext, mTelephonyManager, TELEPHONY_MANAGER_CELL_LOCATION, 0
                )?:""
                return if (isEmpty(cellLocation1)) getCorrectSim2TelephonyInfo(
                    cellLocation1ToString, TELEPHONY_MANAGER_CELL_LOCATION_BY_SUB_ID
                ) else getCorrectSim2TelephonyInfo(
                    cellLocation1ToString, TELEPHONY_MANAGER_CELL_LOCATION
                )
            }
            return DEFAULT_TELEPHONY_MANAGER_STRING_VALUE
        }

    @get:RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    val cid1: Int
        get() {
            val cellLocation1 = cellLocation1
            return if (cellLocation1 != null) {
                //if (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM)
                (cellLocation1 as GsmCellLocation).cid
            } else DEFAULT_TELEPHONY_MANAGER_INT_VALUE
        }

    @get:RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    val cid2: Int
        get() = getCellLocationValue(mContext, cellLocation2, Utils.CellLocationType.CID)

    @get:RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    val lac1: Int
        get() {
            val cellLocation1 = cellLocation1
            return if (cellLocation1 != null) {
                (cellLocation1 as GsmCellLocation).lac
            } else DEFAULT_TELEPHONY_MANAGER_INT_VALUE
        }

    @get:RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    val lac2: Int
        get() = getCellLocationValue(mContext, cellLocation2, Utils.CellLocationType.LAC)
    val mnc1: Int
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (mSubscriptionInfoList != null
                    && mSubscriptionInfoList!!.size > 0
                ) {
                    return getMncFromSubscriptionList(0)
                }
            }
            return Utils.getMncFromNetworkOperator(simOperatorCode1)
        }
    val mnc2: Int?
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (mSubscriptionInfoList != null
                    && mSubscriptionInfoList!!.size > 1
                ) {
                    return getMncFromSubscriptionList(1)
                }
            }
            return simOperatorCode2?.let { Utils.getMncFromNetworkOperator(it) }
        }

    private fun getMncFromSubscriptionList(slot: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (mSubscriptionInfoList != null
                && mSubscriptionInfoList!!.size > slot
            ) {
                return mSubscriptionInfoList!![slot].mnc
            }
        }
        return DEFAULT_TELEPHONY_MANAGER_INT_VALUE
    }

    private fun getMccFromSubscriptionList(slot: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (mSubscriptionInfoList != null
                && mSubscriptionInfoList!!.size > slot
            ) {
                return mSubscriptionInfoList!![slot].mcc
            }
        }
        return DEFAULT_TELEPHONY_MANAGER_INT_VALUE
    }

    val mcc1: Int
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (mSubscriptionInfoList != null
                    && mSubscriptionInfoList!!.size > 0
                ) {
                    return getMccFromSubscriptionList(0)
                }
            }
            return Utils.getMccFromNetworkOperator(simOperatorCode1)
        }
    val mcc2: Int?
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (mSubscriptionInfoList != null
                    && mSubscriptionInfoList!!.size > 1
                ) {
                    return getMccFromSubscriptionList(1)
                }
            }
            return simOperatorCode2?.let { Utils.getMccFromNetworkOperator(it) }
        }

    @get:RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    val subscriberId1: String
        get() {
            if (isLowerThanAndroidQ(mContext)) {
                if (isPhoneStatePermissionGranted
                    && mTelephonyManager != null
                ) {
                    return mTelephonyManager.subscriberId
                }
            }
            return DEFAULT_TELEPHONY_MANAGER_STRING_VALUE
        }

    @get:RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    val subscriberId2: String
        get() = getCorrectSim2TelephonyInfo(subscriberId1, TELEPHONY_MANAGER_SUBSCRIBERID)
    private val subscriptionManager: List<SubscriptionInfo>?
        /////////////////////////////////////////////////////
        private get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                //SubscriptionManager subscriptionManager = SubscriptionManager.from(mContext);
                val subscriptionManager: SubscriptionManager = mContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                try {
                    if (!isPhoneStatePermissionGranted) {
                        return null
                    }
                    return subscriptionManager?.activeSubscriptionInfoList
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return null
        }

    companion object {
        fun getInstance(context: Context): TelephonyManagerPlus {
            return TelephonyManagerPlus(context)
        }
    }
}