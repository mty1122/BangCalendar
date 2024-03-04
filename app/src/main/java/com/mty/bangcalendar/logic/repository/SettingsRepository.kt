package com.mty.bangcalendar.logic.repository

import androidx.core.content.pm.PackageInfoCompat
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.logic.dao.PreferenceDao
import com.mty.bangcalendar.logic.model.GetPreferenceRequest
import com.mty.bangcalendar.logic.model.IntDate
import com.mty.bangcalendar.logic.model.LoginRequest
import com.mty.bangcalendar.logic.model.SmsRequest
import com.mty.bangcalendar.logic.network.BangCalendarNetwork
import com.mty.bangcalendar.util.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsRepository @Inject constructor() {

    suspend fun getLastRefreshDate() = withContext(Dispatchers.IO) {
        IntDate(PreferenceDao.getLastRefreshDate())
    }

    suspend fun setPhoneNumber(phone: String) = withContext(Dispatchers.IO) {
        PreferenceDao.setPhoneNum(phone)
    }

    suspend fun getPhoneNumber() = withContext(Dispatchers.IO) {
        PreferenceDao.getPhoneNum()
    }

    suspend fun getAppUpdateInfo() = withContext(Dispatchers.IO) {
        try {
            val updateInfo = BangCalendarNetwork.getAppUpdateInfo()
            Result.success(updateInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getVersionCode(): Long {
        val packageManager = BangCalendarApplication.context.packageManager
        val packageInfo = packageManager
            .getPackageInfo(BangCalendarApplication.context.packageName, 0)
        return PackageInfoCompat.getLongVersionCode(packageInfo)
    }

    suspend fun sendSms(phoneNumber: String) = withContext(Dispatchers.IO) {
        try {
            val requestCode = SecurityUtil.getSmsRequestCode()
            val request = SmsRequest(phoneNumber, requestCode[0], requestCode[1], requestCode[2])
            val result = BangCalendarNetwork.sendSms(request)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(phoneNumber: String, smsCode: String) = withContext(Dispatchers.IO) {
        try {
            SecurityUtil.aesKey = SecurityUtil.getRandomKey()
            PreferenceDao.aesKey = SecurityUtil.aesKey
            val request = LoginRequest(
                phoneNumber,
                SecurityUtil.encrypt(SecurityUtil.aesKey, smsCode),
                SecurityUtil.getEncryptedKey(SecurityUtil.aesKey)
            )
            val result = BangCalendarNetwork.login(request)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun backupPreference() = withContext(Dispatchers.IO) {
        val preference = PreferenceDao.getUserPreference()
        preference.encrypt() //加密后上传
        val response = try {
            BangCalendarNetwork.setUserPreference(preference)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
        return@withContext response.string() == "OK"
    }

    suspend fun recoveryPreference() = withContext(Dispatchers.IO) {
        val preference = try {
            val phoneNumber = PreferenceDao.getPhoneNum()
            val request = GetPreferenceRequest(
                phoneNumber, SecurityUtil.encrypt(SecurityUtil.aesKey, phoneNumber)
            )
            BangCalendarNetwork.getUserPreference(request)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
        preference.decrypt() //解密后入库
        PreferenceDao.setUserPreference(preference)
        return@withContext preference
    }

}