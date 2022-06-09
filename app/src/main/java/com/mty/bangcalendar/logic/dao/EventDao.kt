package com.mty.bangcalendar.logic.dao

import androidx.room.*
import com.mty.bangcalendar.logic.model.Event

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(event: Event): Long

    @Update
    fun updateEvent(event: Event)

    @Query("select * from Event where startDate = " +
            "(select max(startDate) from Event where startDate <= :date)")
    fun getNearlyEventByDate(date: Int): Event?

    @Query("select * from Event where startDate >= :date and character1 = :character1Id " +
            "and character5 = :character1Id + 4")
    fun getNearlyBandEventByDate(date: Int, character1Id: Int): Event?

}