package com.mty.bangcalendar.ui.guide

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.mty.bangcalendar.R
import com.mty.bangcalendar.util.LogUtil
import com.mty.bangcalendar.ui.main.MainActivity

class GuideActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(GuideViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        supportActionBar?.hide()
        firstStartInit()
    }

    private fun firstStartInit() {
        viewModel.isFirstStart.observe(this) { isFirstStart ->
            if (isFirstStart) {
                LogUtil.d("AppInit", "App is first start")
                viewModel.refreshDataProgress.observe(this) { progress ->
                    if (progress == 100) {
                        startMainActivity()
                    }
                }
                //初始化数据库
                viewModel.initDataBase(this)
            } else {
                LogUtil.d("AppInit", "App is not first start")
                startMainActivity()
            }
        }
        viewModel.isFirstStart()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}