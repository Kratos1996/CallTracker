package com.ishant.boomblaster.utils

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds

object ContactSaver {
    private val operations = ArrayList<ContentProviderOperation>()
    fun saveContact(context: Context,name: String, phoneNumber: String) {
        operations.clear()
        val rawContactId = createRawContact(operations)
        addNameToContact(operations, rawContactId, name)
        addPhoneNumberToContact(operations, rawContactId, phoneNumber)
        applyBatch(context,operations)
    }


    private fun createRawContact(operations: ArrayList<ContentProviderOperation>): Long {
        val rawContactInsertIndex = operations.size
        operations.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )
        return rawContactInsertIndex.toLong()
    }

    private fun addNameToContact(operations: ArrayList<ContentProviderOperation>, rawContactId: Long, name: String) {
        operations.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactId.toInt())
                .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build()
        )
    }

    private fun addPhoneNumberToContact(operations: ArrayList<ContentProviderOperation>, rawContactId: Long, phoneNumber: String) {
        operations.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactId.toInt())
                .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.TYPE_MOBILE)
                .build()
        )
    }

    private fun applyBatch(context: Context,operations: ArrayList<ContentProviderOperation>) {
        try {
            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the exception
        }
    }
}

