package com.mty.bangcalendar.ui.settings.view

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.preference.SwitchPreference
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.R
import com.mty.bangcalendar.util.GenericUtil
import com.mty.bangcalendar.util.LogUtil
import com.mty.bangcalendar.util.toast
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class FcmView @Inject constructor(@ActivityContext private val context: Context) {

    fun handlePreferenceChangeEvent(newValue: Boolean, preference: SwitchPreference) {
        if (newValue)
            startPush(preference)
        else
            getDeviceCode()
    }

    private fun startPush(preference: SwitchPreference) {
        val dialog = AlertDialog.Builder(context)
            .setTitle("启用FCM推送服务")
            .setIcon(R.mipmap.ic_launcher)
            .setMessage(context.getString(R.string.notification_text))
            .setNegativeButton("取消") { _, _ ->
            }
            .setPositiveButton("启用") { _, _ ->
                val googleApiAvailability = GoogleApiAvailability.getInstance()
                val result = googleApiAvailability
                    .isGooglePlayServicesAvailable(BangCalendarApplication.context)
                if (result == ConnectionResult.SUCCESS) {
                    fcmInit()
                    toast("开启成功")
                    preference.isChecked = true
                } else {
                    toast("开启失败，GMS服务不可用")
                }
            }
            .create()
        dialog.show()
    }

    private fun fcmInit() {
        Firebase.analytics.setAnalyticsCollectionEnabled(true)
        Firebase.messaging.isAutoInitEnabled = true
    }

    private fun getDeviceCode() {
        Firebase.messaging.token.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result
                GenericUtil.copyToClipboard(token, "已复制推送token到剪贴板")
            } else {
                LogUtil.w("FCM Warning", it.exception.toString())
            }
        }
    }

}