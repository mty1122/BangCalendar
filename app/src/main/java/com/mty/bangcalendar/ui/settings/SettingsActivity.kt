package com.mty.bangcalendar.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.R

class SettingsActivity : AppCompatActivity() {

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
                    val intent = Intent("com.mty.bangcalendar.USERNAME_CHANGE")
                    intent.setPackage(BangCalendarApplication.context.packageName)
                    BangCalendarApplication.context.sendBroadcast(intent)
                    return@setOnPreferenceChangeListener true
                }
            }
        }

    }

}