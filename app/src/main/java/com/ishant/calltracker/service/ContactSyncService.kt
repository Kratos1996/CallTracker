package com.ishant.calltracker.service

import android.app.IntentService
import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import com.google.gson.Gson
import com.ishant.calltracker.app.CallTrackerApplication.Companion.contactLoading
import com.ishant.calltracker.database.room.ContactList
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ContactSyncService : Service() {

    @Inject
    lateinit var databaseRepository: DatabaseRepository

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()

    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e("DEBUG", "CallTracker :  ContactService onStartCommand")

        fetchContacts()
        return START_STICKY
    }

    private fun fetchContacts() {
        Log.e(ServiceRestarterService.TAG, "CallTracker : Service > ContactService >loading contact")
        scope.launch {
            contactLoading.value = true
            val contacts = mutableListOf<ContactList>()
            val contentResolver: ContentResolver = applicationContext.contentResolver
            val cursor: Cursor? = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val hasPhoneNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)).toInt()
                    val contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                    if (hasPhoneNumber > 0) {
                        val phoneNumbers = getPhoneNumbersForContact(contactId)
                        if (phoneNumbers.isNotEmpty()) {
                            val dataContact = ContactList(
                                contactId = contactId,
                                name = name,
                                phoneNumber = phoneNumbers[0],
                                isFav = false
                            )
                            contacts.add(dataContact)
                            saveContactsToDatabase(dataContact)
                        }
                    }
                }

            }
            closedContactService()
            cursor?.close()
        }
    }

    private fun closedContactService() {
        scope.launch {
            delay(2000)
            contactLoading.value = false
        }

       // this@ContactSyncService.stopServiceContact()
    }

    private fun getPhoneNumbersForContact(contactId: String): List<String> {
        val phoneNumbers = mutableListOf<String>()
        val contentResolver: ContentResolver = applicationContext.contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val phoneNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                phoneNumbers.add(phoneNumber)
            }
        }

        return phoneNumbers
    }

    private  fun saveContactsToDatabase(contacts: ContactList) {
        scope.launch {
            databaseRepository.insertContact(contacts)
        }
        Log.e(
            ServiceRestarterService.TAG,
            "CallTracker : Service > ContactService >Contact Saved ${Gson().toJson(contacts)}"
        )
    }


    @Deprecated("Deprecated in Java")
    override fun onDestroy() {
        super.onDestroy()
        scope.coroutineContext.cancelChildren()
    }

    override fun onBind(p0: Intent?): IBinder? {
       return null
    }
}