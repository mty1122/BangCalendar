package com.mty.bangcalendar.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.R
import com.mty.bangcalendar.logic.Repository
import com.mty.bangcalendar.logic.model.LoginRequest
import com.mty.bangcalendar.logic.model.SmsRequest
import com.mty.bangcalendar.logic.network.ServiceCreator
import com.mty.bangcalendar.ui.BaseActivity
import com.mty.bangcalendar.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class SettingsActivity : BaseActivity() {

    companion object {
        const val REFRESH_CHARACTER_FAILURE = 10
        const val REFRESH_CHARACTER_SUCCESS = 11
        const val REFRESH_EVENT_FAILURE = 20
        const val REFRESH_EVENT_SUCCESS = 21
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }

        val toolbar: Toolbar = findViewById(R.id.settingsToolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //小白条沉浸
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            findViewById<LinearLayout>(R.id.settingsActivity)
                .setOnApplyWindowInsetsListener { view, insets ->
                val top = WindowInsetsCompat.toWindowInsetsCompat(insets, view)
                    .getInsets(WindowInsetsCompat.Type.statusBars()).top
                view.updatePadding(top = top)
                insets
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private val viewModel by lazy {
            ViewModelProvider(this).get(SettingsViewModel::class.java)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            //监听数据库刷新结果
            viewModel.refreshDataResult.observe(this) { result ->
                when (result) {
                    REFRESH_CHARACTER_FAILURE -> toast("角色数据更新失败，请检查网络")
                    REFRESH_CHARACTER_SUCCESS -> toast("角色数据更新成功")
                    REFRESH_EVENT_FAILURE -> toast("活动数据更新失败，请检查网络")
                    REFRESH_EVENT_SUCCESS -> toast("活动数据更新成功")
                    else -> toast("系统错误，请联系作者")
                }
            }

            //刷新数据库
            findPreference<Preference>("update_database")?.let {
                it.setOnPreferenceClickListener {
                    viewModel.refreshDataBase(requireContext())
                    return@setOnPreferenceClickListener true
                }
            }

            //登录
            requireActivity().lifecycleScope.launch {
                viewModel.phoneNum.collect {
                    findPreference<Preference>("sign_in")?.let { preference ->
                        if (it != "")
                            preference.summary = "已登录：$it"
                        else
                            preference.summary = getString(R.string.sign_in_summary)
                    }
                }
            }
            viewModel.getPhoneNum()

            viewModel.appUpdateInfo.observe(this) {
                if (it.isFailure) {
                    toast("检查更新失败，请检查网络连接")
                } else {
                    val currentVersionCode = getVersionCode()
                    val latestVersionCode = it.getOrNull()!!.versionCode
                    if (currentVersionCode < latestVersionCode) {
                        updateJump(it.getOrNull()!!.versionName)
                    } else {
                        toast("当前版本已是最新版本")
                    }
                }
            }

            //设置同步
            findPreference<Preference>("upload_preference")?.let {
                it.setOnPreferenceClickListener {
                    findPreference<Preference>("sign_in")?.let { preference ->
                        if (preference.summary == getString(R.string.sign_in_summary)) {
                            Toast.makeText(activity, "请先登录", Toast.LENGTH_SHORT).show()
                            return@setOnPreferenceClickListener true
                        }
                    }
                    viewModel.backupPreference()
                    return@setOnPreferenceClickListener true
                }
            }

            findPreference<Preference>("download_preference")?.let {
                it.setOnPreferenceClickListener {
                    findPreference<Preference>("sign_in")?.let { preference ->
                        if (preference.summary == getString(R.string.sign_in_summary)) {
                            Toast.makeText(activity, "请先登录", Toast.LENGTH_SHORT).show()
                            return@setOnPreferenceClickListener true
                        }
                    }
                    viewModel.recoveryPreference { userPreference ->
                        ThemeUtil.setCurrentTheme(userPreference.theme)
                        requireActivity().recreate()
                        toast("恢复成功")
                    }
                    return@setOnPreferenceClickListener true
                }
            }

            findPreference<Preference>("program")?.let {
                it.setOnPreferenceClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://github.com/mty1122/BangCalendar")
                    startActivity(intent)
                    return@setOnPreferenceClickListener true
                }
            }

            findPreference<Preference>("author")?.let {
                it.setOnPreferenceClickListener { preference ->
                    GenericUtil.copyToClipboard(preference.summary.toString())
                    return@setOnPreferenceClickListener true
                }
            }

            findPreference<Preference>("version")?.let {
                it.setOnPreferenceClickListener {
                    viewModel.getAppUpdateInfo()
                    return@setOnPreferenceClickListener true
                }
            }

            findPreference<Preference>("notification")?.let {
                it.setOnPreferenceChangeListener { preference, newValue ->
                    if (newValue as Boolean) {
                        startPush(preference as SwitchPreference)
                    } else {
                        getDeviceCode()
                    }
                    return@setOnPreferenceChangeListener false
                }
            }

            findPreference<Preference>("theme")?.let {
                it.setOnPreferenceChangeListener { _, newValue ->
                    ThemeUtil.setCurrentTheme(newValue as String)
                    requireActivity().recreate()
                    return@setOnPreferenceChangeListener true
                }
            }

            findPreference<Preference>("sign_in")?.let {
                it.setOnPreferenceClickListener { preference ->
                    if (preference.summary == getString(R.string.sign_in_summary))
                        login()
                    else
                        logout()
                    return@setOnPreferenceClickListener true
                }
            }
        }

        private fun login() {
            val view = LayoutInflater.from(activity)
                .inflate(R.layout.login, null, false)
            val phoneText: EditText = view.findViewById(R.id.sms_phone)
            val codeText: EditText = view.findViewById(R.id.sms_code)
            val dialog = AlertDialog.Builder(requireActivity())
                .setTitle("登录")
                .setIcon(R.mipmap.ic_launcher)
                .setView(view)
                .setCancelable(false)
                .create()

            view.findViewById<Button>(R.id.send_sms_button).setOnClickListener {
                val phoneNumber = phoneText.text.toString()
                if (checkPhoneNum(phoneNumber)) {
                    val button = it as Button
                    button.isEnabled = false
                    button.text = getString(R.string.sms_sending)
                    requireActivity().lifecycleScope.launch(Dispatchers.IO) {
                        val requestCode = SecurityUtil.getSmsRequestCode()
                        val result = viewModel.sendSms(
                            SmsRequest(phoneNumber, requestCode[0], requestCode[1], requestCode[2])
                        )
                        val response = result.getOrNull()
                        withContext(Dispatchers.Main) {
                            if (response != null && response.string() == "OK") {
                                button.text = getString(R.string.sms_send_success)
                                codeText.requestFocus()
                            } else {
                                toast("发送失败，请重试，或检查网络连接")
                                button.isEnabled = true
                                button.text = getString(R.string.sms_send)
                            }
                        }
                    }
                } else
                    toast("请输入正确的手机号")
            }
            view.findViewById<Button>(R.id.login_cancel_button).setOnClickListener {
                dialog.hide()
            }
            view.findViewById<Button>(R.id.login_button).setOnClickListener {
                //验证手机号和验证码合法性，登录中禁用按钮
                val phoneNumber = phoneText.text.toString()
                val smsCode = codeText.text.toString()
                if (checkPhoneNum(phoneNumber) && smsCode.length == 6) {
                    val button = it as Button
                    button.isEnabled = false
                    button.text = getString(R.string.logging)
                    requireActivity().lifecycleScope.launch(Dispatchers.IO) {
                        SecurityUtil.aesKey = SecurityUtil.getRandomKey()
                        Repository.setAesKey(SecurityUtil.aesKey)
                        val result = viewModel.login(
                            LoginRequest(phoneNumber,
                                         SecurityUtil.encrypt(SecurityUtil.aesKey, smsCode),
                                         SecurityUtil.getEncryptedKey(SecurityUtil.aesKey)
                            )
                        )
                        val response = result.getOrNull()
                        withContext(Dispatchers.Main) {
                            if (response != null && response.string() == "OK") {
                                viewModel.setPhoneNum(phoneNumber)
                                dialog.hide()
                                toast("登录成功")
                            } else {
                                toast("手机号或验证码错误")
                                button.isEnabled = true
                                button.text = getString(R.string.sign_in_title)
                            }
                        }
                    }
                }
            }

            dialog.show()
        }

        private fun logout() {
            val dialog = AlertDialog.Builder(requireActivity())
                .setTitle("退出登录")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(getString(R.string.logout))
                .setNegativeButton("取消") { _, _ ->
                }
                .setPositiveButton("确认") { _, _ ->
                    viewModel.setPhoneNum("")
                    Toast.makeText(activity, "退出登录成功", Toast.LENGTH_SHORT).show()
                }
                .create()
            dialog.show()
        }

        private fun getVersionCode(): Long {
            val packageManager = BangCalendarApplication.context.packageManager
            val packageInfo = packageManager
                .getPackageInfo(BangCalendarApplication.context.packageName, 0)
            return PackageInfoCompat.getLongVersionCode(packageInfo)
        }

        private fun updateJump(versionName: String) {
            val dialog = AlertDialog.Builder(requireActivity())
                .setTitle("发现新版本")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("检测到新版本，版本号$versionName，是否立即更新？")
                .setNegativeButton("取消") { _, _ ->
                }
                .setPositiveButton("确认") { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(ServiceCreator.BASE_URL +
                            "app_service/BangCalendar_$versionName.apk")
                    startActivity(intent)
                }
                .create()
            dialog.show()
        }

        private fun startPush(preference: SwitchPreference) {
            val dialog = AlertDialog.Builder(requireActivity())
                .setTitle("启用FCM推送服务")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(getString(R.string.notification_text))
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

        private fun checkPhoneNum(phone: String?): Boolean {
            phone?.let {
                val regex = "\\b1[3-9]\\d{9}\\b"
                val pattern = Pattern.compile(regex)
                val matcher = pattern.matcher(it)
                if (matcher.find())
                    return true
            }
            return false
        }

    }

}