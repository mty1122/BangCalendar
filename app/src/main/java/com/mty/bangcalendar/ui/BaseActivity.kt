package com.mty.bangcalendar.ui

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.mty.bangcalendar.BangCalendarApplication.Companion.isNavBarImmersive
import com.mty.bangcalendar.R
import com.mty.bangcalendar.util.ThemeUtil


open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        //锁定屏幕方向（纵向）
        @SuppressLint("SourceLockedOrientationActivity")
        if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        //非沉浸模式，且非深色模式下使用浅色背景导航栏
        val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        val isNightModeDisabled = uiModeManager.nightMode != UiModeManager.MODE_NIGHT_YES
        if (!isNavBarImmersive && isNightModeDisabled) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                window.decorView.windowInsetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        //沉浸模式恢复深色背景导航栏（透明）
        } else {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                window.decorView.windowInsetsController?.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = 0
            }
        }
        //设置主题
        ThemeUtil.currentTheme?.let {
            setTheme(it)
        }
        //非沉浸模式下设置导航栏颜色
        if (!isNavBarImmersive) {
            window.navigationBarColor = ThemeUtil.getBackgroundColor(this)
        }
        //设置状态栏颜色
        window.statusBarColor = ThemeUtil.getToolBarColor(this)

        super.onCreate(savedInstanceState)

    }

    /**
     * 本函数用来实现导航栏（小白条）沉浸，如需自定义沉浸逻辑，请重写此函数。本函数仅在ViewBinding模式下自动执行，
     * 其余模式请手动在setContentView后调用以实现沉浸
     * @param rootView 当前Activity的根视图
     */
    protected open fun navBarImmersion(rootView: View) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        //关闭装饰窗口自适应
        window.setDecorFitsSystemWindows(false)
        rootView.setOnApplyWindowInsetsListener { view, insets ->
            val top = WindowInsetsCompat.toWindowInsetsCompat(insets, view)
                .getInsets(WindowInsetsCompat.Type.statusBars()).top
            //手动设置根视图到顶部的距离（状态栏高度）
            view.updatePadding(top = top)
            insets
        }
        //设置导航栏透明
        window.navigationBarColor = getColor(R.color.transparent)
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        //实现小白条沉浸
        if (isNavBarImmersive)
            navBarImmersion(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        Glide.get(this).clearMemory()
    }

}