package com.mty.bangcalendar.ui.list

import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityEventListBinding
import com.mty.bangcalendar.ui.BaseActivity

class EventListActivity : BaseActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(EventListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityEventListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.eventListToolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //小白条沉浸
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
            val adapter = EventListAdapter(it, this)
            binding.eventList.adapter = adapter
            val position = intent.getIntExtra("current_id", 1)
            (binding.eventList.layoutManager as LinearLayoutManager)
                .scrollToPosition(position - 1)
        }
        viewModel.getEventList()

    }

}