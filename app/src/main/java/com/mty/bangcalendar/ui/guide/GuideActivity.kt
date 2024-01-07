package com.mty.bangcalendar.ui.guide

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.R
import com.mty.bangcalendar.service.CharacterRefreshService
import com.mty.bangcalendar.service.EventRefreshService
import com.mty.bangcalendar.ui.main.MainActivity
import com.mty.bangcalendar.ui.theme.BangCalendarTheme
import com.mty.bangcalendar.util.ThemeUtil
import kotlinx.coroutines.launch

class GuideActivity : ComponentActivity() {

    private val viewModel by lazy { ViewModelProvider(this)[GuideViewModel::class.java] }

    private val eventConnection: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                val refreshBinder = p1 as EventRefreshService.RefreshBinder
                refreshBinder.refresh { progress, details ->
                    viewModel.refreshDetails(details)
                    viewModel.refreshDataProgress(progress)
                }
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
            }
        }
    }

    private val characterConnection: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                val refreshBinder = p1 as CharacterRefreshService.RefreshBinder
                refreshBinder.refresh { progress, details ->
                    viewModel.refreshDetails(details)
                    viewModel.refreshDataProgress(progress)
                }
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.start)
        //初始化App
        viewModel.getInitData { initData ->
            //设置主题
            ThemeUtil.setCurrentTheme(initData.theme)
            //非首次启动不设置动画
            val anim = ActivityOptionsCompat
                .makeCustomAnimation(this,0, 0).toBundle()
            //检查启动类型
            if (initData.isFirstStart) {
                /* 首次启动 */
                setContent { ShowContent() }
                firstStartInit()
            } else if (BangCalendarApplication.systemDate.getDayOfWeak() == 2
                && initData.lastRefreshDay != BangCalendarApplication.systemDate.day) {
                /* 每周一自动更新数据库 */
                val intent = Intent(this, EventRefreshService::class.java)
                startService(intent)
                startMainActivity(anim)
            } else {
                /* 常规启动 */
                startMainActivity(anim)
            }
        }
    }

    @Composable
    private fun ShowContent() {
        val progress by viewModel.refreshDataProgress.collectAsState()
        val progressDetails by viewModel.refreshDetails.collectAsState()
        val buttonEnabled by viewModel.launchButtonEnabled.collectAsState()
        BangCalendarTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                GuideView(
                    title = stringResource(id = R.string.welcome),
                    progress = progress.toFloat() / 100,
                    progressDetails = progressDetails,
                    buttonText = stringResource(id = R.string.welcome_button),
                    onClickListener = {
                        val anim = ActivityOptionsCompat.makeCustomAnimation(
                            this,0, android.R.anim.fade_out).toBundle()
                        startMainActivity(anim)
                    },
                    buttonEnabled = buttonEnabled
                )
            }
        }
    }

    private fun startMainActivity(anim: Bundle?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent, anim)
        finish()
    }

    private fun firstStartInit() {
        lifecycleScope.launch {
            viewModel.setDefaultPreference()
            var isCharacterRefreshServiceNotStart = true
            viewModel.refreshDataProgress.collect { progress ->
                when (progress) {
                    50 -> {
                        if (isCharacterRefreshServiceNotStart) {
                            val intent = Intent(this@GuideActivity,
                                CharacterRefreshService::class.java)
                            bindService(intent, characterConnection,
                                Context.BIND_AUTO_CREATE)
                            isCharacterRefreshServiceNotStart = false
                        }
                    }
                    100 -> {
                        viewModel.isNotFirstStart()
                        unbindService(eventConnection)
                        unbindService(characterConnection)
                        viewModel.setLaunchButtonEnabled(true)
                        viewModel.refreshDetails(getString(R.string.init_complete))
                    }
                }
            }
        }
        val intent = Intent(this, EventRefreshService::class.java)
        bindService(intent, eventConnection, Context.BIND_AUTO_CREATE)
    }

}

@Composable
fun GuideView(title: String, progress: Float, progressDetails: String, buttonText: String,
              buttonEnabled: Boolean, onClickListener: () -> Unit) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(3f))
        Text(
            text = title,
            color = MaterialTheme.colors.primaryVariant,
            fontSize = 32.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(3.8f))
        Text(
            text = progressDetails,
            color = MaterialTheme.colors.primaryVariant,
        )
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(top = 5.dp, bottom = 10.dp)
        )
        Button(
            onClick = onClickListener,
            modifier = Modifier.fillMaxWidth(0.95f),
            enabled = buttonEnabled
        ) {
            Text(text = buttonText)
        }
        Spacer(Modifier.weight(2f))
    }
}

@Preview(showBackground = true)
@Composable
fun GuideViewPreview() {
    var progress by remember { mutableStateOf(0f) }
    var progressDetails by remember { mutableStateOf("") }
    BangCalendarTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            GuideView(
                title = stringResource(id = R.string.welcome),
                progress = progress,
                progressDetails = progressDetails,
                buttonText = stringResource(id = R.string.welcome_button),
                buttonEnabled = true,
                onClickListener = {
                    progress += 0.1f
                    progressDetails = progress.toString()
                }
            )
        }
    }

}