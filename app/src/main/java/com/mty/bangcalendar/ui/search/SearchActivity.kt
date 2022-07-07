package com.mty.bangcalendar.ui.search

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.mty.bangcalendar.R
import com.mty.bangcalendar.databinding.ActivitySearchBinding
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.ui.BaseActivity
import com.mty.bangcalendar.util.EventUtil
import com.mty.bangcalendar.util.LogUtil
import com.mty.bangcalendar.util.ThemeUtil
import java.util.regex.Pattern

class SearchActivity : BaseActivity() {

    companion object {
        const val SEARCH_CHARACTER = 0
        const val SEARCH_EVENT = 1
    }

    private val viewModel by lazy {
        ViewModelProvider(this).get(SearchViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val searchBinding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(searchBinding.root)

        val toolbar: Toolbar = findViewById(R.id.searchToolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //小白条沉浸
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            findViewById<LinearLayout>(R.id.searchActivity)
                .setOnApplyWindowInsetsListener { view, insets ->
                    val top = WindowInsetsCompat.toWindowInsetsCompat(insets, view)
                        .getInsets(WindowInsetsCompat.Type.statusBars()).top
                    view.updatePadding(top = top)
                    insets
                }
        }

        //活动&角色卡片初始化
        searchBinding.searchEventCard.run {
            eventCardItem.visibility = View.GONE
            eventProgress.progress = 88
            eventProgress.progressColor =
                getColor(ThemeUtil.getThemeColor(this@SearchActivity))
            eventProgress.textColor =
                getColor(ThemeUtil.getThemeColor(this@SearchActivity))
            eventProgressName.text = "搜索模式"
        }
        searchBinding.searchCharacterCard.characterCardItem.visibility = View.GONE

        viewModel.eventPicture.observe(this) {
            val responseBody = it.getOrNull()
            if (responseBody != null) {
                try {
                    val byte = responseBody.bytes()
                    val bitmap = BitmapFactory.decodeByteArray(byte, 0, byte.size)
                    searchBinding.searchEventCard.eventBackground.background =
                        BitmapDrawable(this.resources, bitmap)
                } catch (e: Exception) {
                    LogUtil.i("Internet", "获取不到返回Body，可能是屏幕发生旋转")
                }
            } else {
                it.exceptionOrNull()?.printStackTrace()
            }
        }

        viewModel.event.observe(this) { event ->
            if (event != null &&
                getSearchType(searchBinding.searchContent.text.toString()) == SEARCH_EVENT) {
                LogUtil.d("Search", "活动id为${event.id}")
                refreshEventComponent(event, searchBinding)
            } else {
                searchBinding.searchEventCard.eventCardItem.visibility = View.GONE
                Toast.makeText(this, "请输入正确的活动编号", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.character.observe(this) { character ->
            if (character != null &&
                getSearchType(searchBinding.searchContent.text.toString()) == SEARCH_CHARACTER) {
                //解决Activity意外重启重复刷新的问题
                refreshCharacterComponent(character, searchBinding)
            } else {
                searchBinding.searchCharacterCard.characterCardItem.visibility = View.GONE
                Toast.makeText(this, "请输入正确角色名称", Toast.LENGTH_SHORT).show()
            }
        }

        searchBinding.searchButton.setOnClickListener {
            searchBinding.searchContent.text.toString().run {
                when (getSearchType(this)) {
                    SEARCH_CHARACTER -> {
                        searchBinding.searchEventCard.eventCardItem.visibility = View.GONE
                        viewModel.getCharacterByName(this)
                    }
                    SEARCH_EVENT -> {
                        LogUtil.d("Search", "活动id为${Integer.parseInt(this)}")
                        searchBinding.searchCharacterCard.characterCardItem.visibility = View.GONE
                        viewModel.getEventById(Integer.parseInt(this))
                    }
                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    private fun getSearchType(content: String): Int {
        val regex = "\\b\\d+\\b"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(content)
        if (matcher.find()) {
            return SEARCH_EVENT
        }
        return SEARCH_CHARACTER
    }

    private fun refreshCharacterComponent(character: Character, binding: ActivitySearchBinding) {
        binding.searchCharacterCard.run {
            charName.text = StringBuilder().run {
                append("姓名：")
                append(character.name)
                toString()
            }
            charBir.text = StringBuilder().run {
                append("生日：")
                append(character.birthday)
                toString()
            }
            charColor.text = StringBuilder().run {
                append("颜色：")
                append(character.color)
                toString()
            }
            charColor.setTextColor(Color.parseColor(character.color))
            charBand.text = StringBuilder().run {
                append("所属乐队：")
                append(character.band)
                toString()
            }
            Glide.with(this@SearchActivity)
                .load(EventUtil.matchCharacter(character.id.toInt())).into(charImage)
            characterCardItem.visibility = View.VISIBLE
        }
    }

    private fun refreshEventComponent(event: Event, binding: ActivitySearchBinding) {
        //刷新活动类型
        binding.searchEventCard.eventType.text = StringBuilder().run {
            append("活动")
            append(event.id)
            append(" ")
            append(EventUtil.matchType(event.type))
            toString()
        }
        //刷新活动角色
        Glide.with(this).load(EventUtil.matchCharacter(event.character1))
            .into(binding.searchEventCard.char1)
        Glide.with(this).load(EventUtil.matchCharacter(event.character2))
            .into(binding.searchEventCard.char2)
        Glide.with(this).load(EventUtil.matchCharacter(event.character3))
            .into(binding.searchEventCard.char3)
        Glide.with(this).load(EventUtil.matchCharacter(event.character4))
            .into(binding.searchEventCard.char4)
        Glide.with(this).load(EventUtil.matchCharacter(event.character5))
            .into(binding.searchEventCard.char5)
        //刷新活动属性
        Glide.with(this).load(EventUtil.matchAttrs(event.attrs))
            .into(binding.searchEventCard.eventAttrs)
        //刷新乐队图片
        Glide.with(this).load(EventUtil.getBandPic(event))
            .into(binding.searchEventCard.eventBand)
        //刷新活动图片
        viewModel.getEventPicture(EventUtil.eventIdFormat(event.id.toInt()))
        binding.searchEventCard.eventCardItem.visibility = View.VISIBLE
    }

}