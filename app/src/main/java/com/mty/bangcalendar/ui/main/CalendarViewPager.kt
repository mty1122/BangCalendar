package com.mty.bangcalendar.ui.main

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.mty.bangcalendar.util.LogUtil

@Deprecated("过早设置监听器会导致空指针异常，因为RecyclerView还未初始化完成，读取不到子布局")
class CalendarViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    private var count = 0 //page总数
    private var currentPosition = 1 //当前位置

    private val onPageChangeListener: OnPageChangeListener = object : OnPageChangeListener{
        override fun onPageSelected(position: Int) {
            //实际上position是将要滚动的位置
            currentPosition = position
            if (position == 1) {
                val view = getChildAt(1) as RecyclerView
                val adapter = view.adapter as CalendarViewAdapter
                val calendarUtil = adapter.calendarUtil
                LogUtil.d("CalendarViewItem", "date：${calendarUtil.toDate()}")
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            when (state) {
                //滚动完成
                SCROLL_STATE_IDLE -> {
                    //如果当前位置为0，则设置item为倒数第二
                    if (currentPosition == 0) {
                        setCurrentItem(count - 2, false)
                    //如果当前位置为倒数第一，则设置item为1
                    } else if (currentPosition == count - 1) {
                        setCurrentItem(1, false)
                    }
                }
            }
        }

        override fun onPageScrolled(position: Int, positionOffset: Float,
            positionOffsetPixels: Int) {

        }
    }

    init {
        //监听page位置变化
        addOnPageChangeListener(onPageChangeListener)
    }

    override fun setAdapter(adapter: PagerAdapter?) {
        super.setAdapter(adapter)
        if (adapter?.count != null) {
            //设置item总数
            count = adapter.count
        }
        //设置初始item为1
        currentItem = 1
    }

}