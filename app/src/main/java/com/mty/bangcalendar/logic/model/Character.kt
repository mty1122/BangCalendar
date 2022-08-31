package com.mty.bangcalendar.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Character(@PrimaryKey(autoGenerate = true) var id: Long, var name: String,
                     var birthday: String, var color: String, var band: String)
