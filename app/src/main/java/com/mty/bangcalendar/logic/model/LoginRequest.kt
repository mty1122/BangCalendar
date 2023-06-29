package com.mty.bangcalendar.logic.model

data class LoginRequest(val phone: String, val smsCode: String, val key: String)
