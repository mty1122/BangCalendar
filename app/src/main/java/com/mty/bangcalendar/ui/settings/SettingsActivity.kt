package com.mty.bangcalendar.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.mty.bangcalendar.BangCalendarApplication.Companion.isNavBarImmersive
import com.mty.bangcalendar.BangCalendarApplication.Companion.systemDate
import com.mty.bangcalendar.R
import com.mty.bangcalendar.logic.DatabaseUpdater
import com.mty.bangcalendar.ui.BaseActivity
import com.mty.bangcalendar.ui.settings.view.FcmView
import com.mty.bangcalendar.ui.settings.view.LoginView
import com.mty.bangcalendar.ui.settings.view.UpdateAppView
import com.mty.bangcalendar.util.AnimUtil
import com.mty.bangcalendar.util.GenericUtil
import com.mty.bangcalendar.util.ThemeUtil
import com.mty.bangcalendar.util.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {

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
        navBarImmersion(findViewById(R.id.settingsActivity))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    @AndroidEntryPoint
    class SettingsFragment : PreferenceFragmentCompat() {

        private val viewModel:SettingsViewModel by viewModels()

        @Inject lateinit var loginView: LoginView
        @Inject lateinit var updateAppView: UpdateAppView
        @Inject lateinit var fcmView: FcmView

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            findPreference<Preference>("update_database")?.let { preference->
                lifecycleScope.launch {
                    viewModel.settingsUiState
                        .map { it.lastRefreshDate }
                        .distinctUntilChanged()
                        .filterNotNull()
                        .collect {
                            if (it.value != 0) preference.summary = "最后更新：${it.value}"
                        }
                }
                preference.setOnPreferenceClickListener {
                    preference.isSelectable = false
                    lifecycleScope.launch {
                        val originalSummary = preference.summary
                        viewModel.refreshDataBase().collect { updateState->
                            when (updateState) {
                                DatabaseUpdater.DatabaseUpdateState.PREPARE ->
                                    preference.summary = "正在下载更新"
                                DatabaseUpdater.DatabaseUpdateState.SUCCESS_EVENT ->
                                    preference.summary = "活动数据更新成功"
                                DatabaseUpdater.DatabaseUpdateState.SUCCESS_CHARACTER -> {
                                    preference.summary = "最后更新：${systemDate.toDate().value}"
                                    toast("更新成功")
                                    preference.isSelectable = true
                                }
                                DatabaseUpdater.DatabaseUpdateState.ERROR -> {
                                    preference.summary = originalSummary
                                    toast("更新失败，请检查网络")
                                    preference.isSelectable = true
                                }
                            }
                        }
                    }
                    return@setOnPreferenceClickListener true
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
                    val uiState = viewModel.settingsUiState.value
                    updateAppView.handleClickEvent(uiState.hasNewVersion, uiState.newVersionName)
                    return@setOnPreferenceClickListener true
                }
            }

            findPreference<Preference>("notification")?.let {
                it.setOnPreferenceChangeListener { preference, newValue ->
                    fcmView.handlePreferenceChangeEvent(
                        newValue as Boolean, preference as SwitchPreference
                    )
                    return@setOnPreferenceChangeListener false
                }
            }

            findPreference<Preference>("anim")?.let {
                it.setOnPreferenceChangeListener { _, newValue ->
                    AnimUtil.setAnimPreference(newValue as Boolean)
                    return@setOnPreferenceChangeListener true
                }
            }

            findPreference<Preference>("nvbar")?.let {
                it.setOnPreferenceChangeListener { preference, newValue ->
                    AlertDialog.Builder(requireActivity())
                        .setTitle("小白条沉浸")
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("是否${if (newValue as Boolean) "启用" else "关闭"}小白条沉浸")
                        .setNegativeButton("取消") { _, _ ->
                        }
                        .setPositiveButton("确认") { _, _ ->
                            isNavBarImmersive = newValue
                            (preference as SwitchPreference).isChecked = newValue
                            requireActivity().recreate()
                        }
                        .create().show()
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

            findPreference<Preference>("sign_in")?.let { preference ->
                preference.setOnPreferenceClickListener {
                    if (preference.summary == getString(R.string.sign_in_summary))
                        loginView.loginDialog(
                            sendSms = viewModel::sendSms,
                            requestLogin = viewModel::login,
                            setPhoneNumber = viewModel::setPhoneNumber
                        ).show()
                    else
                        loginView.logoutDialog(viewModel::setPhoneNumber).show()
                    return@setOnPreferenceClickListener true
                }
                //刷新登录状态
                lifecycleScope.launch {
                    viewModel.settingsUiState
                        .map { it.phoneNumber }
                        .distinctUntilChanged()
                        .collect { phoneNumber->
                            if (phoneNumber != "")
                                preference.summary = "已登录：$phoneNumber"
                            else
                                preference.summary = getString(R.string.sign_in_summary)
                        }
                }
            }
        }

    }

}