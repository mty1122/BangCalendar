package com.mty.bangcalendar.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mty.bangcalendar.BangCalendarApplication.Companion.isNavigationBarImmersionEnabled
import com.mty.bangcalendar.util.ThemeUtil

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        //非沉浸模式下使用浅色背景导航栏
        if (!isNavigationBarImmersionEnabled) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                window.decorView.windowInsetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                @RequiresApi(Build.VERSION_CODES.O)
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }
        //设置主题
        ThemeUtil.currentTheme?.let {
            setTheme(it)
        }
        //非沉浸模式下设置状态栏和导航栏颜色
        if (!isNavigationBarImmersionEnabled) {
            window.statusBarColor = ThemeUtil.getToolBarColor(this)
            window.navigationBarColor = ThemeUtil.getBackgroundColor(this)
        }

        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        Glide.get(this).clearMemory()
    }

}