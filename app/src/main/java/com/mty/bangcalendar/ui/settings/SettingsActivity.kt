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
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
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
import com.mty.bangcalendar.logic.model.LoginRequest
import com.mty.bangcalendar.ui.ActivityCollector
import com.mty.bangcalendar.ui.BaseActivity
import com.mty.bangcalendar.ui.guide.GuideActivity
import com.mty.bangcalendar.util.*
import java.util.regex.Pattern

class SettingsActivity : BaseActivity() {

    companion object {
        const val REFRESH_CHARACTER_FAILURE = 10
        const val REFRESH_CHARACTER_SUCCESS = 11
        const val REFRESH_EVENT_FAILURE = 20
        const val REFRESH_EVENT_SUCCESS = 21
        const val REFRESH_USERNAME = 1
        const val REFRESH_BAND = 2
        const val REFRESH_CHARACTER = 3
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
            viewModel.phoneNum.observe(this) {
                findPreference<Preference>("sign_in")?.let { preference ->
                    if (it != "")
                        preference.summary = "已登录：$it"
                    else
                        preference.summary = getString(R.string.sign_in_summary)
                }
            }
            viewModel.getPhoneNum()

            viewModel.loginResponse.observe(this) {
                val loginResponse = it.getOrNull()
                loginResponse?.let {
                    LogUtil.d("login", "send sms success")
                }
            }

            viewModel.userPreference.observe(this) {
                viewModel.uploadUserPreference(it)
            }
            viewModel.uploadResponse.observe(this) {
                val response = it.getOrNull()
                response?.let { responseBody ->
                    if (responseBody.string() == "0")
                        Toast.makeText(activity, "备份成功", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(activity, "备份失败，可能是服务器故障"
                            , Toast.LENGTH_SHORT).show()
                }
                if (response == null)
                    Toast.makeText(activity, "备份失败，请检查网络", Toast.LENGTH_SHORT).show()
            }

            viewModel.downloadPreference.observe(this) { result ->
                val userPreference = result.getOrNull()
                userPreference?.let {
                    viewModel.setUserPreference(it)
                }
                if (userPreference == null)
                    Toast.makeText(activity, "恢复失败，原因可能是还未进行过备份或网络连接失败",
                        Toast.LENGTH_LONG).show()
            }
            viewModel.setResponse.observe(this) {
                if (it == 0)
                    showPreferenceRecoverCompleteDialog()
                else
                    Toast.makeText(activity, "恢复失败", Toast.LENGTH_SHORT).show()
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
                    viewModel.getUserPreference(viewModel.phoneNum.value!!)
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
                    viewModel.downloadUserPreference(LoginRequest(viewModel.phoneNum.value!!,
                        SecurityUtil.getRequestCode()))
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

            findPreference<Preference>("signature")?.let {
                it.setOnPreferenceChangeListener { _, _ ->
                    val intent = Intent("com.mty.bangcalendar.SETTINGS_CHANGE")
                    intent.setPackage(BangCalendarApplication.context.packageName)
                    intent.putExtra("settingsCategory", REFRESH_USERNAME)
                    BangCalendarApplication.context.sendBroadcast(intent)
                    return@setOnPreferenceChangeListener true
                }
            }

            findPreference<Preference>("band")?.let {
                it.setOnPreferenceChangeListener { _, _ ->
                    val intent = Intent("com.mty.bangcalendar.SETTINGS_CHANGE")
                    intent.setPackage(BangCalendarApplication.context.packageName)
                    intent.putExtra("settingsCategory", REFRESH_BAND)
                    BangCalendarApplication.context.sendBroadcast(intent)
                    return@setOnPreferenceChangeListener true
                }
            }

            findPreference<Preference>("character")?.let {
                it.setOnPreferenceChangeListener { _, _ ->
                    val intent = Intent("com.mty.bangcalendar.SETTINGS_CHANGE")
                    intent.setPackage(BangCalendarApplication.context.packageName)
                    intent.putExtra("settingsCategory", REFRESH_CHARACTER)
                    BangCalendarApplication.context.sendBroadcast(intent)
                    return@setOnPreferenceChangeListener true
                }
            }

            findPreference<Preference>("theme")?.let {
                it.setOnPreferenceChangeListener { _, _ ->
                    restartActivity()
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
                if (checkPhoneNum(phoneText.text.toString())) {
                    viewModel.login(LoginRequest(phoneText.text.toString(),
                        SecurityUtil.getRequestCode()))
                    val button = it as Button
                    button.isEnabled = false
                    button.text = getString(R.string.sms_send_success)
                } else
                    Toast.makeText(activity, "请输入正确的手机号", Toast.LENGTH_SHORT).show()
            }
            view.findViewById<Button>(R.id.login_cancel_button).setOnClickListener {
                dialog.hide()
            }
            view.findViewById<Button>(R.id.login_button).setOnClickListener {
                viewModel.loginResponse.value?.let {
                    val loginResponse = it.getOrNull()
                    if (loginResponse != null && loginResponse.phone == phoneText.text.toString()
                        && SecurityUtil.decrypt(loginResponse.smsCode) == codeText.text.toString())
                    {
                        viewModel.setPhoneNum(loginResponse.phone)
                        dialog.hide()
                        viewModel.loginFinished()
                        Toast.makeText(activity, "登录成功", Toast.LENGTH_SHORT).show()
                    } else {
                        it.exceptionOrNull()?.printStackTrace()
                        Toast.makeText(activity, "手机号或验证码错误", Toast.LENGTH_SHORT).show()
                    }
                }
                if (viewModel.loginResponse.value == null)
                    Toast.makeText(activity, "手机号或验证码错误", Toast.LENGTH_SHORT).show()
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

        private fun showPreferenceRecoverCompleteDialog() {
            val dialog = AlertDialog.Builder(requireActivity())
                .setTitle("恢复完成")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(getString(R.string.recover_complete))
                .setNegativeButton("稍后重启") { _, _ ->
                }
                .setPositiveButton("现在重启") { _, _ ->
                    restartActivity()
                    Toast.makeText(activity, "恢复成功", Toast.LENGTH_SHORT).show()
                }
                .create()
            dialog.show()
        }

        private fun restartActivity() {
            ActivityCollector.finishAll()
            val intent = Intent(this@SettingsFragment.context, GuideActivity::class.java)
            intent.putExtra("settings_change", true)
            startActivity(intent)
        }

    }

}