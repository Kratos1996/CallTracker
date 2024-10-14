package com.ishant.LKing.database.room.messageLogDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ishant.LKing.database.room.messageLogTables.ReplyLogs

@Dao
interface ReplyLogsDao {
    @Query(
        "SELECT reply_logs.notif_reply_date FROM REPLY_LOGS " +
                "INNER JOIN app_packages ON app_packages.`index` = reply_logs.`index` " +
                "WHERE app_packages.package_name=:packageName AND reply_logs.notif_title=:title AND reply_logs.notif_reply_date =:currentDate " +
                "ORDER BY notif_reply_time"
    )
    suspend fun getLastReplyTimeStamp(title: String?, packageName: String?,currentDate: String?): List<String>

    @Insert
    suspend fun logReply(log: ReplyLogs)

    @get:Query("SELECT COUNT(id) FROM MESSAGE_LOGS")
    val numReplies: Long

    //https://stackoverflow.com/questions/11771580/deleting-android-sqlite-rows-older-than-x-days
    @Query("DELETE FROM reply_logs WHERE notif_reply_time <= strftime('%s', datetime('now', '-30 days'));")
    suspend fun purgeMessageLogs()

    @get:Query("SELECT notif_reply_time FROM REPLY_LOGS ORDER BY notif_reply_time DESC LIMIT 1")
    val firstRepliedTime: Long
}