package com.mty.bangcalendar.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Event(@PrimaryKey(autoGenerate = true) var id: Long, var name: String, var startDate: Int,
                 var attrs: Int, var type: Int, var character1: Int, var character2: Int,
                 var character3: Int, var character4: Int, var character5: Int,
                 var character6: Int, var character7: Int)
