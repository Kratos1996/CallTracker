package com.ishant.LKing.database.room.messageLogDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ishant.LKing.database.room.messageLogTables.AppPackage


@Dao
interface AppPackageDao {
    
    @Query("SELECT [index] FROM app_packages WHERE package_name=:packageName")
    fun getPackageIndex(packageName: String?): Int

    @Insert
    fun insertAppPackage(appPackage: AppPackage)
}