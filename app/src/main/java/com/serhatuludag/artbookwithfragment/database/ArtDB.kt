package com.serhatuludag.artbookwithfragment.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.serhatuludag.artbookwithfragment.model.Art

@Database(entities = [Art::class], version = 1)
abstract class ArtDatabase : RoomDatabase() {
    abstract fun artDao(): ArtDao
}