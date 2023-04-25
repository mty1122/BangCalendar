package com.mty.bangcalendar.util

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import com.mty.bangcalendar.BangCalendarApplication.Companion.context
import java.security.MessageDigest

object SecurityUtil {

    init {
        System.loadLibrary("bangcalendar")
    }

    external fun getRequestCode(): String

    external fun decrypt(text: String): String

    @SuppressLint("PackageManagerGetSignatures")
    @Suppress("DEPRECATION")
    fun getSignature(): ByteArray {
        var signatures: Array<Signature>? = null
        try {
            signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = context.packageManager.getPackageInfo(context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
                if (signingInfo.hasMultipleSigners())
                    signingInfo.apkContentsSigners //多个签名者
                else
                    signingInfo.signingCertificateHistory //单个签名者
            } else {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName,
                    PackageManager.GET_SIGNATURES)
                packageInfo.signatures //低于安卓P兼容
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val signature = signatures?.get(0)?.toByteArray()
        signature?.let {
            val messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.update(it)
            return messageDigest.digest()
        }
        return ByteArray(0)
    }

}