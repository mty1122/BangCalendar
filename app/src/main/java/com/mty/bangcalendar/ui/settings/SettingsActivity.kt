package com.mty.bangcalendar.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.R
import com.mty.bangcalendar.util.LogUtil
import java.util.regex.Pattern

class SettingsActivity : AppCompatActivity() {

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
                    REFRESH_CHARACTER_FAILURE -> {
                        Toast.makeText(activity, "角色数据更新失败，请检查网络",
                            Toast.LENGTH_SHORT).show()
                    }
                    REFRESH_CHARACTER_SUCCESS -> {
                        Toast.makeText(activity, "角色数据更新成功",
                            Toast.LENGTH_SHORT).show()
                    }
                    REFRESH_EVENT_FAILURE -> {
                        Toast.makeText(activity, "活动数据更新失败，请检查网络",
                            Toast.LENGTH_SHORT).show()
                    }
                    REFRESH_EVENT_SUCCESS -> {
                        Toast.makeText(activity, "活动数据更新成功",
                            Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(activity, "系统错误，请联系作者",
                            Toast.LENGTH_SHORT).show()
                    }
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

            findPreference<Preference>("program")?.let {
                it.setOnPreferenceClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://github.com/mty1122/BangCalendar")
                    startActivity(intent)
                    return@setOnPreferenceClickListener true
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
                .create()

            view.findViewById<Button>(R.id.send_sms_button).setOnClickListener {
                if (checkPhoneNum(phoneText.text.toString())) {
                    viewModel.login(phoneText.text.toString())
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
                        && loginResponse.smsCode == codeText.text.toString()) {
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