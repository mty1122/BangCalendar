package com.mty.bangcalendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mty.bangcalendar.logic.util.LogUtil
import com.mty.bangcalendar.ui.main.MainViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.isFirstStart()
        viewModel.isFirstStart.observe(this) {
            if (it) {
                LogUtil.d("AppInit", "App is first start")
                viewModel.addCharacter(this, true)
                viewModel.addEvent(this, true)
            } else {
                LogUtil.d("AppInit", "App is not first start")
            }
        }

    }

}