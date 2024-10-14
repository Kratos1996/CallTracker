package com.ishant.boomblaster.database.room

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContactList(
    @PrimaryKey(autoGenerate = false)
    @NonNull var contactId: String,
    @NonNull var phoneNumber: String,
    @ColumnInfo var name: String? = null,
    @ColumnInfo var isFav: Boolean? = false
)


/*
@Entity
data class UploadContact(@PrimaryKey(autoGenerate = false )
                         @NonNull val serialNo :Long ,
                           @ColumnInfo var  sourceMobileNo:String,
                           @ColumnInfo var  mobile:String,
                           @ColumnInfo var name:String ,
                           @ColumnInfo var type:String,
                           @ColumnInfo var duration:String,
)
*/

@Entity("UploadContact")
data class UploadContact(@PrimaryKey(autoGenerate = false )
                         @NonNull val serialNo :Long,
                         @ColumnInfo var  listOfCalls:String,
                         @ColumnInfo var  date:Long,
                         @ColumnInfo var type:String,
    )


object UploadContactType {
    const val ALL = "all"
    const val PENDING = "pending"
    const val COMPLETE = "complete"
}


