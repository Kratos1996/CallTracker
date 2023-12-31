package com.ishant.calltracker.database.room

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DatabaseRepository @Inject constructor(val db: AppDB, val context: Context) {

    suspend fun insertContact(contact: ContactList) {
        if(contact.phoneNumber.isNotEmpty())
             db.getDao().insert(contact)
    }
    suspend fun insertContact(contacts: List<ContactList>) {
        if(contacts.isNotEmpty())
            db.getDao().insert(contacts)
    }

    suspend fun insertUpload(uploadContact: UploadContact) {
            db.getDao().insertUploadContact(uploadContact)
    }

    suspend fun getSingleContact(phone:String): ContactList {
        return db.getDao().getSingleContact(phone)
    }

    suspend fun getRestrictedContact(phone:String,isFav: Boolean): ContactList {
        return db.getDao().getSingleRestrictedContact(phone,isFav)
    }

    suspend fun setRestrictedContact(phone:String,isFav:Boolean){
        val int = db.getDao().setContactRestricted(phone, isFav)
    }

    suspend fun deleteAll() {
        db.getDao().deleteAllContacts()
        db.getDao().deleteAllUploadedContacts()
    }

     fun getContactList(data:String):LiveData<List<ContactList>>{
        return if(data.isNotEmpty()){
            db.getDao().getContactList(data)
        }else{
            db.getDao().getContactList()
        }
    }
     fun getRestrictedDataList(data:String): LiveData<List<ContactList>> {
        return if(data.isNotEmpty()){
            db.getDao().getAllRestrictedContacts(data,isFav = true)
        }else{
            db.getDao().getAllRestrictedContacts(isFav = true)
        }
    }

     fun getUploadContactList(type:String):Flow<List<UploadContact>>{
        return when(type) {
            UploadContactType.ALL -> db.getDao().getUploadContactList()
            else ->  db.getDao().getUploadContactList(type = type)
        }
    }
    suspend fun updateUploadContact(serialNo:Long,type: String){
        db.getDao().updateUploadContact(serialNo,type)
    }
}