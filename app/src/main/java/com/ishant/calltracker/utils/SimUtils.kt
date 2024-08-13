package com.ishant.calltracker.utils

import android.content.Context
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
class SimUtils(private val context: Context) {

    // Get the active SIM cards
    fun getActiveSims(): List<SubscriptionInfo> {
        val subscriptionManager = SubscriptionManager.from(context)
        return subscriptionManager.activeSubscriptionInfoList ?: emptyList()
    }

    // Determine the currently selected SIM
    fun getSelectedSim(): SubscriptionInfo? {
        val subscriptionManager = SubscriptionManager.from(context)
        val activeSims = getActiveSims()

        // You can implement your own logic to determine the selected SIM
        // For example, you might store the selected SIM in SharedPreferences
        // and retrieve it here.

        // For this example, we'll just return the first active SIM.
        return activeSims.firstOrNull()
    }

    // Set the selected SIM (for demonstration purposes)
    fun setSelectedSim(subscriptionId: Int) {
        val subscriptionManager = SubscriptionManager.from(context)
        // You can implement your own logic to store the selected SIM
        // For example, you might use SharedPreferences to store the selected SIM.
    }
}