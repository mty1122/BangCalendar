package com.mty.bangcalendar.logic.dao

import androidx.room.*
import com.mty.bangcalendar.logic.model.Character

@Dao
interface CharacterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: Character)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characterList: List<Character>)

    @Update
    fun updateCharacter(character: Character)

    @Query("select * from Character where birthday like :month || '%'")
    fun getCharacterByMonth(month: String): List<Character>

    @Query("select * from Character where birthday = :birthday")
    fun getCharacterByBirthday(birthday: String): List<Character>

    @Query("select * from Character where id = :id")
    fun getCharacterById(id: Int): Character

    @Query("select * from Character where name like '%' || :name")
    fun getCharacterByName(name: String): Character?

    @Query("select * from Character")
    fun getCharacterList(): List<Character>

}