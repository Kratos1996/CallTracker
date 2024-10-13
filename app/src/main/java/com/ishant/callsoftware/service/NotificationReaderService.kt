package com.ishant.callsoftware.service


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.SpannableString
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.google.gson.Gson
import com.ishant.callsoftware.R
import com.ishant.callsoftware.api.notification.NotificationWear
import com.ishant.callsoftware.api.request.UploadContactRequest
import com.ishant.callsoftware.app.CallTrackerApplication
import com.ishant.callsoftware.database.room.DatabaseRepository
import com.ishant.callsoftware.database.room.MessageLogDB
import com.ishant.callsoftware.database.room.UploadContact
import com.ishant.callsoftware.database.room.UploadContactType
import com.ishant.callsoftware.di.BaseUrlInterceptor
import com.ishant.callsoftware.domain.ContactUseCase
import com.ishant.callsoftware.network.Resource
import com.ishant.callsoftware.ui.dashboard.DashboardActivity

import com.ishant.callsoftware.utils.AppPreference
import com.ishant.callsoftware.utils.DbUtils
import com.ishant.callsoftware.utils.convertDate
import com.ishant.callsoftware.utils.getPhoneNumber
import com.ishant.callsoftware.utils.getPhoneNumberByName
import com.ishant.callsoftware.utils.helper.App
import com.ishant.callsoftware.utils.helper.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
@AndroidEntryPoint
class NotificationReaderService : NotificationListenerService() {

    private val TAG = "NotificationReaderService"
    @Inject
    lateinit var baseUrlInterceptor: BaseUrlInterceptor
    private var dbUtils: DbUtils? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject
    lateinit var contactUseCase: ContactUseCase

    @Inject
    lateinit var messageLogDB: MessageLogDB

    @Inject
    lateinit var databaseRepository: DatabaseRepository
    private val MAX_OLD_NOTIFICATION_CAN_BE_REPLIED_TIME_MS = 2 * 60 * 1000
    private val LAST_API_CALL_TIMESTAMP = "last_api_call_timestamp"
    private val RATE_LIMIT_INTERVAL_MS = 1000L // 1 second

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        // Create a notification channel for the service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "notification_reader_channel",
                "Notification Reader",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Start the foreground service
        val notification = createNotification()
        startForeground(1, notification)

    }

    private fun isGroupMessage(sbn: StatusBarNotification): Boolean {
        val rawTitle = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
        val rawText = SpannableString.valueOf("" + sbn.notification.extras["android.text"])
        val isPossiblyAnImageGrpMsg = (rawTitle != null && ": ".contains(rawTitle)
                && rawText != null && rawText.toString().startsWith("\uD83D\uDCF7"))
        return if (!sbn.notification.extras.getBoolean("android.isGroupConversation")) {
            !isPossiblyAnImageGrpMsg
        } else {
            false
        }
    }

    private suspend fun canSendReplyNow(sbn: StatusBarNotification): Boolean {


        // Time between consecutive replies is 10 secs
        val DELAY_BETWEEN_REPLY_IN_MILLISEC = 3 * 1000
        val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
        Log.d(TAG, "canSendReplyNow: " + title)
        Log.d(TAG, "canSendReplyNow: " + sbn.notification.extras.getString("android.text"))
        val selfDisplayName = sbn.notification.extras.getString("android.selfDisplayName")

        if (title != null && selfDisplayName != null && title.equals(
                selfDisplayName,
                ignoreCase = true
            )
        ) {
            return false
        }

        if (dbUtils == null) {
            dbUtils = DbUtils(applicationContext, messageLogDB)
        }

        return dbUtils!!.getLastRepliedTime(
            sbn.packageName,
            title
        ).isEmpty()

    }

    private suspend fun canReply(sbn: StatusBarNotification): Boolean {
        return isNewNotification(sbn) &&
                isGroupMessage(sbn) && AppPreference.isUserLoggedIn && canSendReplyNow(sbn)
    }

    fun isNewNotification(sbn: StatusBarNotification): Boolean {

        return sbn.notification.`when` == 0L ||
                System.currentTimeMillis() - sbn.notification.`when` < MAX_OLD_NOTIFICATION_CAN_BE_REPLIED_TIME_MS
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Check if the notification is from WhatsApp Business
        scope.launch {
            Log.d("IshantTest", " Notification " + Gson().toJson(sbn))
            for (data in Constants.SUPPORTED_APPS) {
                if (data.packageName == sbn.packageName) {
                    val callFromApplication = data.name
                    val contactName = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
                    val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT)

//                    if (canReply(sbn)) {
//                        sendReply(sbn)
//                        saveLogs(sbn)
//                    }
//                    Log.d(TAG, "phoneNumberFromContact: "+contactName)
//                    if (text?.contains("Incoming video call") == true) {
//                        if (contactName != null) {
//                            // First, attempt to get the phone number from the contact name
//                            val phoneNumberFromContact = getPhoneNumberFromContactName(applicationContext, contactName)
//
//                            Log.d(TAG, "phoneNumberFromContact: "+phoneNumberFromContact)
//                            if (phoneNumberFromContact != null) {
//                                // Contact is saved in the phone; phone number found
//                                sendDataofWhatsapp(contactName, data, text)
//                                Log.d("NotificationListener", "Stored Contact: $contactName, Phone Number: $phoneNumberFromContact")
//                            } else {
//                                // Contact is not saved in the phone; try to extract phone number from the notification text
//                                val phoneNumberFromNotification = extractPhoneNumber(text)
//
//                                if (phoneNumberFromNotification != null) {
//                                    sendDataofWhatsapp(contactName, data, text)
//                                   // Log.d("NotificationListener", "Unstored Contact: $title, Phone Number: $phoneNumberFromNotification")
//                                } else {
//                                   // Log.d("NotificationListener", "Phone number not found for $title in notification text")
//                                }
//                            }
//
//                    }} else if (text?.contains("Incoming voice call") == true) {
//                        if (contactName != null) {
//                            // First, attempt to get the phone number from the contact name
//                            val phoneNumberFromContact = getPhoneNumberFromContactName(applicationContext, contactName)
//                            Log.d(TAG, "phoneNumberFromContact: "+phoneNumberFromContact)
//                            if (phoneNumberFromContact != null) {
//                                // Contact is saved in the phone; phone number found
//                                sendDataofWhatsapp(contactName, data, text)
//                                Log.d("NotificationListener", "Stored Contact: $contactName, Phone Number: $phoneNumberFromContact")
//                            } else {
//                                // Contact is not saved in the phone; try to extract phone number from the notification text
//                                val phoneNumberFromNotification = extractPhoneNumber(text)
//
//                                if (phoneNumberFromNotification != null) {
//                                    sendDataofWhatsapp(contactName, data, text)
//                                    // Log.d("NotificationListener", "Unstored Contact: $title, Phone Number: $phoneNumberFromNotification")
//                                } else {
//                                    // Log.d("NotificationListener", "Phone number not found for $title in notification text")
//                                }
//                            }
//
//                        }
//                    } else if (text?.contains("Ongoing voice call") == true) {
//                        Log.d(TAG, "Notification received: $contactName - $text")
//                        sendDataofWhatsapp(contactName, data, text)
//                    } else {
//                        Log.d(TAG, "Notification received: $contactName - $text")
//                    }
                    break
                }
            }
        }

    }


    private fun extractPhoneNumber(text: String?): String? {
        // Regular expression to match phone numbers in the notification text
        val phonePattern = "\\+?[0-9. ()-]{7,}".toRegex()
        return text?.let { phonePattern.find(it)?.value }?.replace(" ","")
    }
    fun getPhoneNumberFromContactName(context: Context, contactName: String): String? {
        val contentResolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

        // Query the contact by the name (display name)
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(contactName)

        val cursor = contentResolver.query(
            uri,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                // Get the phone number from the cursor
                val phoneNumberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                return it.getString(phoneNumberIndex)
            }
        }
        return null
    }
    private fun sendDataofWhatsapp(
        contactName: String?,
        data: App,
        text: String?
    ) {
        val dataReq = UploadContactRequest()
        dataReq.countryCode = AppPreference.loginUser.countryCode
        dataReq.data.add(
            UploadContactRequest.UploadContactData(
                sourceMobileNo = getPhoneNumber(),
                mobile = getPhoneNumberByName(applicationContext, contactName ?: "") ?: contactName
                ?: "",
                name = contactName ?: "",
                type = data.name + " " + text,
                dateTime = convertDate(System.currentTimeMillis()),
                duration = "30"
            )
        )
        saveContact(dataReq)
    }

    private fun saveLogs(sbn: StatusBarNotification) {
        if (dbUtils == null) {
            dbUtils = DbUtils(applicationContext, messageLogDB)
        }
        dbUtils!!.saveLogs(
            sbn,
            sbn.notification.extras.getString(Notification.EXTRA_TITLE),
            sbn.notification.extras.getString(Notification.EXTRA_TEXT)
        )
    }

    private suspend fun sendReply(sbn: StatusBarNotification) {
        val (_, pendingIntent, remoteInputs1) = extractWearNotification(sbn)
        if (remoteInputs1.isEmpty()) {
            return
        }


        val remoteInputs = arrayOfNulls<RemoteInput>(remoteInputs1.size)
        val localIntent = Intent()
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val localBundle = Bundle()


        for ((i, remoteIn) in remoteInputs1.withIndex()) {
            remoteInputs[i] = remoteIn

            try {


                localBundle.putCharSequence(
                    remoteInputs[i]!!.resultKey, AppPreference.replyMsg

                )

            } catch (e: Exception) {
                e.printStackTrace()

            }
        }


        RemoteInput.addResultsToIntent(remoteInputs, localIntent, localBundle)
        try {
            if (pendingIntent != null) {
                if (dbUtils == null) {
                    dbUtils = DbUtils(applicationContext, messageLogDB)
                }
                dbUtils!!.logReply(sbn, sbn.notification.extras.getString(Notification.EXTRA_TITLE))
                pendingIntent.send(this, 0, localIntent)

                cancelNotification(sbn.key)
//                if (canPurgeMessages()) {
                dbUtils!!.purgeMessageLogs()
                AppPreference.lastPurgedTime = System.currentTimeMillis()
//                }
            }
        } catch (e: PendingIntent.CanceledException) {
            Log.e(TAG, "replyToLastNotification error: " + e.localizedMessage)
        }
    }

    private fun canPurgeMessages(): Boolean {
        val daysBeforePurgeInMS = 30 * 24 * 60 * 60 * 1000L
        return System.currentTimeMillis() -
                (AppPreference.lastPurgedTime) > daysBeforePurgeInMS
    }

    fun extractWearNotification(statusBarNotification: StatusBarNotification): NotificationWear {
        val wearableExtender =
            NotificationCompat.WearableExtender(statusBarNotification.notification)
        val actions = wearableExtender.actions
        val remoteInputs: MutableList<RemoteInput> = ArrayList(actions.size)
        var pendingIntent: PendingIntent? = null

        for (act in actions) {
            if (act != null && act.remoteInputs != null) {
                for (x in act.remoteInputs!!.indices) {
                    val remoteInput = act.remoteInputs!![x]
                    remoteInputs.add(remoteInput)
                    pendingIntent = act.actionIntent
                }
            }
        }
        return NotificationWear(
            statusBarNotification.packageName,
            pendingIntent,
            remoteInputs,
            statusBarNotification.notification.extras,
            statusBarNotification.tag,
            UUID.randomUUID().toString()
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        // Create an intent for the notification
        val intent = Intent(this, DashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val builder = Notification.Builder(this, "notification_reader_channel")
            .setContentTitle("Notification Reader Service")
            .setContentText("Listening for WhatsApp Business notifications")
            .setSmallIcon(R.drawable.notification_ico)
            .setContentIntent(pendingIntent)

        return builder.build()
    }

    private fun saveContact(uploadContact: UploadContactRequest) {
        if (uploadContact.data.isNotEmpty()) {
            val currentTime = System.currentTimeMillis()
            val lastApiCallTimestamp = AppPreference.lastWahtsappApicalled

            if (currentTime - lastApiCallTimestamp < RATE_LIMIT_INTERVAL_MS) {
                Log.e("CallTracker", "Rate limit exceeded. Try again later.")
                return
            }
            AppPreference.lastWahtsappApicalled = currentTime

            baseUrlInterceptor.setBaseUrl(AppPreference.baseUrl)
            contactUseCase.uploadContact(uploadContact).onEach { result ->
                when (result) {
                    is Resource.Error -> {
                        Log.e("CallTracker", "CallTracker: Contact Not Saved")
                        val data = UploadContact(
                            serialNo = System.currentTimeMillis(),
                            listOfCalls = Gson().toJson(uploadContact),
                            type = UploadContactType.PENDING,
                            date = System.currentTimeMillis()
                        )
                        databaseRepository.insertUpload(data)
                        delay(1000)
                        CallTrackerApplication.isRefreshUi.value = true
                    }

                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        delay(1000)
                        CallTrackerApplication.isRefreshUi.value = true
                    }
                }
            }.launchIn(CoroutineScope(Dispatchers.IO))
        }
    }
}

