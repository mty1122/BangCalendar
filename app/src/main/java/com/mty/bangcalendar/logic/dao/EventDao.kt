package com.mty.bangcalendar.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mty.bangcalendar.logic.model.Event

@Dao
interface EventDao {

    @Insert
    fun insertEvent(event: Event): Long

    @Update
    fun updateEvent(event: Event)

    @Query("select * from Event where startDate = (select max(startDate) from Event where startDate <= :date)")
    fun getNearlyEventByDate(date: Int): Event

}