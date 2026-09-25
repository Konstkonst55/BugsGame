package com.kxnst.bugsgame.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = true
)
abstract class BugsGameDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
