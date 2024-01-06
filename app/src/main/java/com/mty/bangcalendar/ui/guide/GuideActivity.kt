package com.mty.bangcalendar.ui.guide

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mty.bangcalendar.BangCalendarApplication
import com.mty.bangcalendar.R
import com.mty.bangcalendar.service.CharacterRefreshService
import com.mty.bangcalendar.service.EventRefreshService
import com.mty.bangcalendar.ui.main.MainActivity
import com.mty.bangcalendar.ui.theme.BangCalendarTheme
import com.mty.bangcalendar.util.ThemeUtil
import com.mty.bangcalendar.util.startActivity
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

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.start)
        //初始化App
        viewModel.getInitData { initData ->
            ThemeUtil.setCurrentTheme(initData.theme)
            if (initData.isFirstStart) {
                /* 首次启动 */
                setContent { ShowContent() }
                firstStartInit()
            } else if (BangCalendarApplication.systemDate.getDayOfWeak() == 2
                && initData.lastRefreshDay != BangCalendarApplication.systemDate.day) {
                /* 每周一自动更新数据库 */
                val intent = Intent(this, EventRefreshService::class.java)
                startService(intent)
                startMainActivity()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
                else
                    overridePendingTransition(0, 0)
            } else {
                /* 常规启动 */
                startMainActivity()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
                else
                    overridePendingTransition(0, 0)
            }
        }
    }

    @Suppress("DEPRECATION")
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
                        startMainActivity()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                                0, android.R.anim.fade_out)
                        else
                            overridePendingTransition(0, android.R.anim.fade_out)
                    },
                    buttonEnabled = buttonEnabled
                )
            }
        }
    }

    private fun startMainActivity() {
        startActivity<MainActivity>()
        finish()
    }

    private fun firstStartInit() {
        lifecycleScope.launch {
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