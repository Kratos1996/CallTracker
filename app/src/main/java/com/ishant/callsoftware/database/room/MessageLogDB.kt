package com.ishant.callsoftware.database.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ishant.callsoftware.database.room.messageLogDao.*
import com.ishant.callsoftware.database.room.messageLogTables.*
import com.ishant.callsoftware.database.room.messageLogTables.ReplyLogs


@Database(entities = [MessageLogs::class, ReplyLogs::class, AppPackage::class], version = 7)
abstract class MessageLogDB : RoomDatabase() {

    abstract fun messageLogsDao(): MessageLogsDao?
    abstract fun replyLogsDao(): ReplyLogsDao?
    abstract fun appPackageDao(): AppPackageDao?

}