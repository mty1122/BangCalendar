package com.mty.bangcalendar.logic.model

data class LoginRequest(val phone: String, val requestCode: String, val tag: String, val iv: String)
