package com.ishant.boomblaster.database.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ishant.boomblaster.database.room.messageLogDao.*
import com.ishant.boomblaster.database.room.messageLogTables.*
import com.ishant.boomblaster.database.room.messageLogTables.ReplyLogs


@Database(entities = [MessageLogs::class, ReplyLogs::class, AppPackage::class], version = 7)
abstract class MessageLogDB : RoomDatabase() {

    abstract fun messageLogsDao(): MessageLogsDao?
    abstract fun replyLogsDao(): ReplyLogsDao?
    abstract fun appPackageDao(): AppPackageDao?

}