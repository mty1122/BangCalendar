package com.mty.bangcalendar.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mty.bangcalendar.logic.model.Character

@Dao
interface CharacterDao {

    @Insert
    fun insertCharacter(character: Character): Long

    @Update
    fun updateCharacter(character: Character)

    @Query("select * from Character where birthday = :birthday")
    fun getCharacterByBirthday(birthday: String): List<Character>

}