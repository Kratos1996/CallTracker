package com.ishant.callsoftware.database.room.messageLogTables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_packages")
data class AppPackage(@field:ColumnInfo(name = "package_name") var packageName: String) {
    @PrimaryKey(autoGenerate = true)
    var index = 0
}