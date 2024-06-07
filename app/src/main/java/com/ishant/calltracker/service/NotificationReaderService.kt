package com.ishant.calltracker.service


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.ishant.calltracker.R
import com.ishant.calltracker.api.request.UploadContactRequest
import com.ishant.calltracker.app.CallTrackerApplication
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.database.room.UploadContact
import com.ishant.calltracker.database.room.UploadContactType
import com.ishant.calltracker.di.BaseUrlInterceptor
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.network.Resource
import com.ishant.calltracker.ui.dashboard.DashboardActivity
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.convertDate
import com.ishant.calltracker.utils.getPhoneNumber
import com.ishant.calltracker.utils.getPhoneNumberByName
import com.ishant.calltracker.utils.helper.App
import com.ishant.calltracker.utils.helper.Constants
import com.ishant.calltracker.utils.helper.Constants.SUPPORTED_APPS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
@AndroidEntryPoint
class NotificationReaderService : NotificationListenerService() {

    private val TAG = "NotificationReaderService"
    @Inject
    lateinit var baseUrlInterceptor: BaseUrlInterceptor
    @Inject
    lateinit var contactUseCase: ContactUseCase
    @Inject
    lateinit var databaseRepository: DatabaseRepository

    private  val LAST_API_CALL_TIMESTAMP = "last_api_call_timestamp"
    private  val RATE_LIMIT_INTERVAL_MS = 1000L // 1 second
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

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Check if the notification is from WhatsApp Business
        Log.d("IshantTest", " Notification "+ Gson().toJson(sbn))
        for( data in Constants.SUPPORTED_APPS){
            if(data.packageName == sbn.packageName){
                val callFromApplication =  data.name
                val contactName = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
                val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT)
                if(text?.contains("Incoming video call") == true){
                    Log.d(TAG, "Notification received: $contactName - $text and ${sbn.packageName} ")
                    sendDataofWhatsapp(contactName, data, text)
                }else if(text?.contains( "Incoming voice call") == true){
                    Log.d(TAG, "Notification received: $contactName - $text")
                    sendDataofWhatsapp(contactName, data, text)
                }
                else if(text?.contains("Ongoing voice call") == true ){
                    Log.d(TAG, "Notification received: $contactName - $text")
                    sendDataofWhatsapp(contactName, data, text)
                }
                else{
                    Log.d(TAG, "Notification received: $contactName - $text")
                }
                break
            }
        }

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        // Create an intent for the notification
        val intent = Intent(this, DashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

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

