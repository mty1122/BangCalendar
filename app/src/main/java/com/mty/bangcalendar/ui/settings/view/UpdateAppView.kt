package com.mty.bangcalendar.ui.settings.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.mty.bangcalendar.R
import com.mty.bangcalendar.logic.network.ServiceCreator
import com.mty.bangcalendar.util.toast
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class UpdateAppView @Inject constructor(@ActivityContext private val context: Context) {

    fun handleClickEvent(hasNewVersion: Boolean, versionName: String) {
        if (hasNewVersion) {
            updateJumpDialog(versionName)
        } else {
            toast("当前版本已是最新版本")
        }
    }

    private fun updateJumpDialog(versionName: String) =
        AlertDialog.Builder(context)
        .setTitle("发现新版本")
        .setIcon(R.mipmap.ic_launcher)
        .setMessage("检测到新版本，版本号$versionName，是否立即更新？")
        .setNegativeButton("取消") { _, _ ->
        }
        .setPositiveButton("确认") { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(
                ServiceCreator.BASE_URL +
                        "app_service/BangCalendar_$versionName.apk")
            context.startActivity(intent)
        }
        .create()

}