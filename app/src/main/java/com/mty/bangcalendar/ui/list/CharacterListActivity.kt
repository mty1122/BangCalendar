package com.mty.bangcalendar.ui.list

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.mty.bangcalendar.BangCalendarApplication.Companion.isNavigationBarImmersionEnabled
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivityCharacterListBinding
import com.mty.bangcalendar.ui.BaseActivity

class CharacterListActivity : BaseActivity() {

    private val viewModel: CharacterListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityCharacterListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.characterListToolBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //小白条沉浸
        if (isNavigationBarImmersionEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            binding.root.setOnApplyWindowInsetsListener { view, insets ->
                val top = WindowInsetsCompat.toWindowInsetsCompat(insets, view)
                    .getInsets(WindowInsetsCompat.Type.statusBars()).top
                view.updatePadding(top = top)
                insets
            }
            window.navigationBarColor = getColor(R.color.transparent)
        }

        //配置adapter
        val layoutManager = LinearLayoutManager(this)
        binding.characterList.layoutManager = layoutManager
        viewModel.characterList.observe(this) {
            val adapter = CharacterListAdapter(it, this)
            binding.characterList.adapter = adapter
            val id = intent.getIntExtra("current_id", 1)
            (binding.characterList.layoutManager as LinearLayoutManager)
                .scrollToPosition(id - 1)
        }
        viewModel.getCharacterList()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

}