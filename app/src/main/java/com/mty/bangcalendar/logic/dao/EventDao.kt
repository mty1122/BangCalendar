package com.mty.bangcalendar.logic.dao

import androidx.room.*
import com.mty.bangcalendar.logic.model.Event

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(eventList: List<Event>)

    @Update
    fun updateEvent(event: Event)

    @Query("select * from Event where startDate = " +
            "(select max(startDate) from Event where startDate <= :date)")
    fun getNearlyEventByDate(date: Int): Event?

    @Query("select * from Event where startDate >= :date and character1 = :character1Id " +
            "and character5 = :character1Id + 4")
    fun getNearlyBandEventByDate(date: Int, character1Id: Int): Event?

    @Query("select * from Event where startDate < :date and character1 = :character1Id " +
            "and character5 = :character1Id + 4 order by id desc limit 1")
    fun getLastNearlyBandEventByDate(date: Int, character1Id: Int): Event?

    @Query("select * from Event where id = :id")
    fun getEventById(id: Int): Event?

    @Query("select * from Event")
    fun getEventList(): List<Event>

}