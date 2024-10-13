package com.ishant.jagtap.database.room.messageLogTables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reply_logs",
    foreignKeys = [ForeignKey(
        entity = AppPackage::class,
        parentColumns = arrayOf("index"),
        childColumns = arrayOf("index"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = arrayOf("index"))]
)
data class ReplyLogs(
    var index: Int,
    @field:ColumnInfo(name = "notif_title") var notifTitle: String,
    @field:ColumnInfo(name = "notif_arrived_time") var notifArrivedTime: Long,
    @field:ColumnInfo(name = "notif_replied_msg") var notifRepliedMsg: String,
    @field:ColumnInfo(name = "notif_reply_time") var notifReplyTime: Long,
    @field:ColumnInfo(name = "notif_reply_date") var notifReplyDate: String
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "notif_id")
    var notifId: String? = null

    @ColumnInfo(name = "notif_is_replied")
    var isNotifIsReplied = true

}