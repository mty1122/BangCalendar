package com.mty.bangcalendar.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.mty.bangcalendar.logic.repository.ObjectRepository

class FcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        try {
            super.onNewToken(token)
            ObjectRepository.setFcmToken(token)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}