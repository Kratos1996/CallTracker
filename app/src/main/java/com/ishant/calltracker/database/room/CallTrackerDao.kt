package com.ishant.calltracker.database.room

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CallTrackerDao {

    @Insert(onConflict =   OnConflictStrategy.IGNORE)
    suspend fun insert(contact: ContactList)

    @Insert(onConflict =   OnConflictStrategy.IGNORE)
    suspend fun insertUploadContact(uploadContact: UploadContact)

    @Transaction
    suspend fun insert(dataList: List<ContactList>) {
        for (data in dataList) {
            insert(data)
        }
    }

    @Query("Select * From ContactList where name Like '%' || :data || '%' ")
    suspend fun getContactList(data:String):List<ContactList>

    @Query("Select * From ContactList")
    suspend fun getContactList():List<ContactList>


    @Query("Select * From ContactList where name Like '%' || :data || '%' and isFav=:isFav ")
    suspend fun getAllRestrictedContacts(data:String,isFav:Boolean ):List<ContactList>

    @Query("Delete From ContactList")
    suspend fun deleteAllContacts()

    @Query("Select * From ContactList where phoneNumber=:phone")
    suspend fun getSingleContact(phone:String): ContactList

    @Query("Select * From ContactList where phoneNumber=:phone and isFav=:isFav")
    suspend fun getSingleRestrictedContact(phone:String,isFav: Boolean): ContactList


    @Query("Update ContactList set isFav=:isFav Where phoneNumber=:phone")
    suspend fun setContactRestricted(phone:String,isFav:Boolean):Int

    @Query("Select * From ContactList Where isFav=:isFav")
     suspend fun getAllRestrictedContacts(isFav:Boolean):List<ContactList>

    @Query("Select * From UploadContact")
    suspend fun getUploadContactList():List<UploadContact>

    @Query("Select * From UploadContact where type Like '%' || :type")
    suspend fun getUploadContactList(type :String):List<UploadContact>
}