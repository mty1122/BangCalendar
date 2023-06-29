package com.mty.bangcalendar.logic.model

import com.google.gson.annotations.SerializedName

data class SmsRequest(val phone: String, @SerializedName("requestCode") val smsRequestCode: String,
                      val tag: String, val iv: String)
