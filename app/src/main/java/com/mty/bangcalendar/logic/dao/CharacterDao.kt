package com.mty.bangcalendar.logic.dao

import androidx.room.*
import com.mty.bangcalendar.logic.model.Character

@Dao
interface CharacterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCharacter(character: Character): Long

    @Update
    fun updateCharacter(character: Character)

    @Query("select * from Character where birthday like :birthday || '%'")
    fun getCharacterByBirthday(birthday: String): List<Character>

}