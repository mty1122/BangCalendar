package com.mty.bangcalendar.logic.model

import com.google.gson.annotations.SerializedName

data class UserPreference(val phone: Int, val name: String, val theme: String, val band: String,
                          @SerializedName("char_pref") val character: String)