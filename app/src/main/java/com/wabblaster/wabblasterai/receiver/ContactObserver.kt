package com.wabblaster.wabblasterai.receiver

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.provider.ContactsContract
import com.wabblaster.wabblasterai.service.ContactSyncService
import readPhoneContactPermission


class ContactObserver(val context: Context, handler: Handler) : ContentObserver(handler) {

    private val contentResolver = context.contentResolver

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        context.readPhoneContactPermission(
            granted = {
                context.startService(Intent(context, ContactSyncService::class.java))
            })
    }

    fun registerObserver() {
        contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,
            this
        )
    }

    fun unregisterObserver() {
        contentResolver.unregisterContentObserver(this)
    }
}