package com.ishant.calltracker.database.room

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContactList(
    @PrimaryKey(autoGenerate = false)
    @NonNull var phoneNumber: String,
    @ColumnInfo var name: String? = null,
    @ColumnInfo var isFav: Boolean? = false
)


@Entity
data class UploadContact(@PrimaryKey(autoGenerate = false )
                         @NonNull val serialNo :Long ,
                           @ColumnInfo var  sourceMobileNo:String,
                           @ColumnInfo var  mobile:String,
                           @ColumnInfo var name:String ,
                           @ColumnInfo var type:String,
                           @ColumnInfo var apiPushed:Boolean
)


object UploadContactType {
    const val ALL = "all"
    const val PENDING = "pending"
    const val COMPLETE = "complete"
}


