package com.mty.bangcalendar.ui.guide

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.lifecycle.lifecycleScope
import com.mty.bangcalendar.BangCalendarApplication.Companion.isNavBarImmersive
import com.mty.bangcalendar.R
import com.mty.bangcalendar.ui.BaseActivity
import com.mty.bangcalendar.ui.main.MainActivity
import com.mty.bangcalendar.ui.theme.BangCalendarTheme
import com.mty.bangcalendar.util.AnimUtil
import com.mty.bangcalendar.util.EventUtil
import com.mty.bangcalendar.util.ThemeUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GuideActivity : BaseActivity() {

    private val viewModel: GuideViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.start)
        window.navigationBarColor = getColor(R.color.start)
        //初始化App
        lifecycleScope.launch {
            //等待初始化数据加载完成
            val initData = viewModel.initData.await()
            //设置主题
            ThemeUtil.setCurrentTheme(initData.theme)
            //设置动画偏好
            AnimUtil.setAnimPreference(initData.animPreference)
            //设置导航栏偏好（是否启动小白条沉浸）
            isNavBarImmersive = initData.nvbarPreference
            //设置今日活动
            EventUtil.todayEvent = initData.todayEvent
            //非首次启动不设置动画
            val anim = ActivityOptionsCompat
                .makeCustomAnimation(this@GuideActivity,0, 0).toBundle()
            //检查启动类型
            if (initData.isFirstStart) {
                /* 首次启动 */
                setContent { ShowContent() }
            } else {
                /* 常规启动 */
                startMainActivity(anim)
            }
        }
    }

    @Composable
    private fun ShowContent() {
        val uiState by viewModel.appInitUiState.collectAsState()

        BangCalendarTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                GuideView(
                    title = stringResource(id = R.string.welcome),
                    progress = uiState.initProgress.toFloat() / 100,
                    progressDetails = uiState.initDetails,
                    buttonText = stringResource(id = R.string.welcome_button),
                    onClickListener = {
                        val anim = ActivityOptionsCompat.makeCustomAnimation(
                            this,0, android.R.anim.fade_out).toBundle()
                        startMainActivity(anim)
                    },
                    buttonEnabled = uiState.initProgress == 100
                )
            }
        }
    }

    private fun startMainActivity(anim: Bundle?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent, anim)
        finish()
    }

    //引导界面不执行沉浸
    override fun navBarImmersion(rootView: View) {}

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