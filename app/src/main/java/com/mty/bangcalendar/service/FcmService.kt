package com.mty.bangcalendar.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.mty.bangcalendar.logic.Repository

class FcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Repository.setFcmToken(token)
    }

}