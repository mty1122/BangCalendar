package com.mty.bangcalendar.ui.list

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mty.bangcalendar.databinding.ActivityEventListBinding
import com.mty.bangcalendar.ui.BaseActivity
import com.mty.bangcalendar.ui.main.state.EventCardUiState
import com.mty.bangcalendar.util.EventUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EventListActivity : BaseActivity() {

    private val viewModel: EventListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityEventListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.eventListToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //配置adapter
        val layoutManager = LinearLayoutManager(this)
        binding.eventList.layoutManager = layoutManager
        lifecycleScope.launch {
            var eventList = viewModel.getEventList()
            val bandId = intent.getIntExtra("band_id", -1)
            val startEventId = intent.getIntExtra("current_id", 1)
            var startPositionIndex = startEventId - 1
            //对乐队进行过滤
            if (bandId != -1) {
                eventList = eventList.filter { event ->
                    EventUtil.getBand(event).id == bandId
                }.also { newList ->
                    //计算起始活动在新列表里面的索引
                    newList.indexOfFirst { newEvent ->
                        newEvent.id.toInt() == startEventId
                    }.also { index ->
                        //找到索引后赋值给起始位置变量
                        startPositionIndex = if (index != -1) index else 0
                    }
                }
            }
            //配置活动图片和进度
            val eventCardUiStateList = eventList.map {
                val eventId = EventUtil.eventIdFormat(it.id.toInt())
                EventCardUiState(
                    event = it,
                    eventPicture = viewModel.getEventPic(eventId),
                    progress = EventUtil.getEventProgress(it)
                )
            }
            val adapter = EventListAdapter(this@EventListActivity,
                this@EventListActivity, eventCardUiStateList)
            binding.eventList.adapter = adapter
            //设置起始位置
            (binding.eventList.layoutManager as LinearLayoutManager)
                .scrollToPosition(startPositionIndex)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

}