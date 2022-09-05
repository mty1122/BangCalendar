package com.mty.bangcalendar.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Event(@PrimaryKey(autoGenerate = true) var id: Long, var name: String? = null,
                 var startDate: Int, var endDate: Int? = null, var attrs: Int, var type: Int,
                 var character1: Int, var character2: Int, var character3: Int,
                 var character4: Int? = null, var character5: Int? = null,
                 var character6: Int? = null, var character7: Int? = null)
