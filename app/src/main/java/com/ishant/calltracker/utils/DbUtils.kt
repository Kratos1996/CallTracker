package com.ishant.calltracker.utils

import android.content.Context
import android.service.notification.StatusBarNotification
import com.ishant.calltracker.database.room.MessageLogDB
import com.ishant.calltracker.database.room.messageLogTables.AppPackage
import com.ishant.calltracker.database.room.messageLogTables.MessageLogs
import com.ishant.calltracker.database.room.messageLogTables.ReplyLogs
import com.ishant.calltracker.utils.Utils.getCurrentDate


class DbUtils (val context: Context,val messageLogsDB: MessageLogDB){


    val nunReplies: Long
        get() {

            return messageLogsDB!!.replyLogsDao()!!.numReplies
        }

    suspend fun purgeMessageLogs() {

        messageLogsDB!!.replyLogsDao()!!.purgeMessageLogs()
    }

   suspend fun logReply(sbn: StatusBarNotification, title: String?) {


        var packageIndex = messageLogsDB!!.appPackageDao()!!.getPackageIndex(sbn.packageName)
        if (packageIndex <= 0) {
            val appPackage = AppPackage(sbn.packageName)
            messageLogsDB.appPackageDao()!!.insertAppPackage(appPackage)
            packageIndex = messageLogsDB.appPackageDao()!!.getPackageIndex(sbn.packageName)
        }

        val logs = ReplyLogs(
            packageIndex,
            title!!,
            sbn.notification.`when`,
            AppPreference.replyMsg,
            System.currentTimeMillis(),
            getCurrentDate()
        )
        messageLogsDB.replyLogsDao()!!.logReply(logs)
    }

    fun saveLogs(sbn: StatusBarNotification, title: String?, message: String?) {

        var packageIndex = messageLogsDB!!.appPackageDao()!!.getPackageIndex(sbn.packageName)
        if (packageIndex <= 0) {
            val appPackage = AppPackage(sbn.packageName)
            messageLogsDB.appPackageDao()!!.insertAppPackage(appPackage)
            packageIndex = messageLogsDB.appPackageDao()!!.getPackageIndex(sbn.packageName)
        }
        val logs = MessageLogs(
            packageIndex,
            title!!,
            message,
            sbn.notification.`when`,
            sbn.id
        )
        messageLogsDB.messageLogsDao()!!.logMessage(logs)
    }

    suspend fun getLastRepliedTime(packageName: String?, title: String?): List<String> {

        return messageLogsDB!!.replyLogsDao()!!.getLastReplyTimeStamp(title, packageName,
            getCurrentDate()
        )
    }

    val firstRepliedTime: Long
        get() {

            return messageLogsDB!!.replyLogsDao()!!.firstRepliedTime
        }
}