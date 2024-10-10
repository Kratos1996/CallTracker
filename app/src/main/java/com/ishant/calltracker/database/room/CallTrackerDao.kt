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
    fun getContactList(data:String):Flow<List<ContactList>>

    @Query("Select * From ContactList")
    fun getContactList():Flow<List<ContactList>>


    @Query("Select * From ContactList where name Like '%' || :data || '%' and isFav=:isFav  ")
    fun getAllRestrictedContacts(data:String,isFav:Boolean ):Flow<List<ContactList>>

    @Query("Delete From ContactList")
    suspend fun deleteAllContacts()

    @Query("Select * From ContactList where phoneNumber=:phone")
    suspend fun getSingleContact(phone:String): ContactList

    @Query("Select * From ContactList where phoneNumber=:phone and isFav=:isFav")
    suspend fun getSingleRestrictedContact(phone:String,isFav: Boolean): ContactList
    @Query("Select * From ContactList where phoneNumber=:phone ")
    suspend fun getSingleRestrictedContact(phone:String,): ContactList


    @Query("Update ContactList set isFav=:isFav Where phoneNumber=:phone")
    suspend fun setContactRestricted(phone:String,isFav:Boolean):Int

    @Query("Select * From ContactList Where isFav=:isFav")
    fun getAllRestrictedContacts(isFav:Boolean):Flow<List<ContactList>>

    @Query("Select * From UploadContact  ORDER BY date DESC ")
    fun getUploadContactList():Flow<List<UploadContact>>

    @Query("Select * From UploadContact where type Like '%' || :type ORDER BY date DESC ")
    fun getUploadContactList(type :String):Flow<List<UploadContact>>

    @Query("Update UploadContact set type=:type Where serialNo=:serialNo ")
    suspend fun updateUploadContact(serialNo:Long,type :String):Int

    @Query("Delete From UploadContact")
    suspend fun deleteAllUploadedContacts()

    @Query("Delete From UploadContact where serialNo =:serialNo")
    suspend fun deleteUploadCallData(serialNo: Long)


}