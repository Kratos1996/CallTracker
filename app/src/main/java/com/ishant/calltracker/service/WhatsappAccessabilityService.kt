package com.ishant.calltracker.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.ishant.calltracker.utils.AppPreference


class WhatsappAccessibilityService : AccessibilityService() {


    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(
            "MyAccessibilityService",
            "Event Type: ${event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED}, Package: ${event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED}"
        )
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED
        ) {
            // Handle the event
        }
        if (AppPreference.isServiceEnabled) {

            if (rootInActiveWindow == null) {
             return
            }

            val rootInActiveWindow = AccessibilityNodeInfoCompat.wrap(
                rootInActiveWindow
            )
         //   performGlobalAction(GLOBAL_ACTION_BACK)
            // Whatsapp Message EditText id
            val messageNodeList =
                rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.whatsapp:id/entry")
            if (messageNodeList == null || messageNodeList.isEmpty()) {
               return
            }

            if(messageNodeList.isEmpty()){
                performGlobalAction(GLOBAL_ACTION_BACK)
            }


            // check if the whatsapp message EditText field is filled with text and ending with your suffix (explanation above)
            val messageField = messageNodeList[0]
            if (messageField.text == null || messageField.text.isEmpty()
            ) { // So your service doesn't process any message, but the ones ending your apps suffix
               return
            }

            // Whatsapp send button id
            val sendMessageNodeInfoList =
                rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send")
            if (sendMessageNodeInfoList == null || sendMessageNodeInfoList.isEmpty()) {
              return
            }

            val sendMessageButton = sendMessageNodeInfoList[0]
            if (!sendMessageButton.isVisibleToUser) {
              return
            }

            // Now fire a click on the send button
            sendMessageButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)

            // Now go back to your app by clicking on the Android back button twice:
            // First one to leave the conversation screen
            // Second one to leave whatsapp
            try {
                Thread.sleep(500) // hack for certain devices in which the immediate back click is too fast to handle
                performGlobalAction(GLOBAL_ACTION_BACK)
                Thread.sleep(500) // same hack as above
                performGlobalAction(GLOBAL_ACTION_BACK)
            } catch (ignored: InterruptedException) {
                AppPreference.isServiceEnabled = false
                AppPreference.isFromService = false
            }

            AppPreference.isServiceEnabled = false
            AppPreference.isFromService = false
        }
    }

    override fun onInterrupt() {
        AppPreference.isServiceEnabled = false
        AppPreference.isFromService = false
        stopSelf()
    }
}