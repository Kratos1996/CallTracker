package com.wabblaster.wabblasterai.service

import android.app.IntentService
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.provider.ContactsContract
import com.wabblaster.wabblasterai.database.room.ContactList
import com.wabblaster.wabblasterai.database.room.DatabaseRepository
import com.wabblaster.wabblasterai.utils.Utils
import com.wabblaster.wabblasterai.utils.stopServiceContact
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ContactSyncService : IntentService("ContactSyncService") {

    @Inject
    lateinit var databaseRepository: DatabaseRepository

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        val contacts = fetchContacts()
        saveContactsToDatabase(contacts)
    }

    private fun fetchContacts(): List<ContactList> {
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
                    if (phoneNumbers.isNotEmpty())
                        contacts.add(ContactList(
                                contactId = contactId,
                                name = name,
                                phoneNumber = phoneNumbers[0],
                                isFav = false
                            )
                        )
                }
            }
        }
        cursor?.close()
        this@ContactSyncService.stopServiceContact()
        return contacts
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
                phoneNumbers.add(Utils.extractLast10Digits(phoneNumber))
            }
        }

        return phoneNumbers
    }

    private fun saveContactsToDatabase(contacts: List<ContactList>) {
        scope.launch {
           databaseRepository.insertContact(contacts)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onDestroy() {
        super.onDestroy()
        scope.coroutineContext.cancelChildren()
    }
}