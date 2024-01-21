package com.mty.bangcalendar.ui.list

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.mty.bangcalendar.BangCalendarApplication.Companion.isNavigationBarImmersionEnabled
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityEventListBinding
import com.mty.bangcalendar.ui.BaseActivity
import com.mty.bangcalendar.util.EventUtil

class EventListActivity : BaseActivity() {

    private val viewModel: EventListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityEventListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.eventListToolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //小白条沉浸
        if (isNavigationBarImmersionEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            findViewById<LinearLayout>(R.id.eventListActivity)
                .setOnApplyWindowInsetsListener { view, insets ->
                    val top = WindowInsetsCompat.toWindowInsetsCompat(insets, view)
                        .getInsets(WindowInsetsCompat.Type.statusBars()).top
                    view.updatePadding(top = top)
                    insets
                }
        }

        //配置adapter
        val layoutManager = LinearLayoutManager(this)
        binding.eventList.layoutManager = layoutManager
        viewModel.eventList.observe(this) {
            val bandId = intent.getIntExtra("band_id", -1)
            val startEventId = intent.getIntExtra("current_id", 1)
            var startPositionIndex = startEventId - 1
            //对乐队进行过滤
            val eventList = if (bandId != -1) {
                it.filter { event->
                    EventUtil.getBand(event).id == bandId
                }.also { newList->
                    //计算起始活动在新列表里面的索引
                    newList.indexOfFirst { newEvent->
                        newEvent.id.toInt() == startEventId
                    }.also { index->
                        //找到索引后赋值给起始位置变量
                        startPositionIndex = if (index != -1) index else 0
                    }
                }
            } else {
                it
            }
            val adapter = EventListAdapter(eventList, this) { eventId, onPictureReady->
                viewModel.getEventPic(eventId, onPictureReady)
            }
            binding.eventList.adapter = adapter
            //设置起始位置
            (binding.eventList.layoutManager as LinearLayoutManager)
                .scrollToPosition(startPositionIndex)
        }
        viewModel.getEventList()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

}