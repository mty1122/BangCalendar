package com.mty.bangcalendar.logic.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.Event

@Database(entities = [Character::class, Event::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun characterDao(): CharacterDao

    abstract fun eventDao(): EventDao

    companion object {

        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(BangCalendarApplication.context,
                AppDatabase::class.java, "bang_calendar")
                .build().apply {
                    instance = this
                }
        }

    }

}