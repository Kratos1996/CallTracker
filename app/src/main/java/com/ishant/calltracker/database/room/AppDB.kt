package com.ishant.calltracker.database.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ContactList::class,UploadContact::class], version = 2, exportSchema = false)
abstract class AppDB : RoomDatabase() {
    abstract fun getDao(): CallTrackerDao
}