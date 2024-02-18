package com.mty.bangcalendar.ui.list

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mty.bangcalendar.databinding.ActivityCharacterListBinding
import com.mty.bangcalendar.ui.BaseActivity

class CharacterListActivity : BaseActivity() {

    private val viewModel: CharacterListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityCharacterListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.characterListToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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