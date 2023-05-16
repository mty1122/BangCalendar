package com.mty.bangcalendar.ui.main

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.mty.bangcalendar.logic.model.CalendarScrollView
import com.mty.bangcalendar.util.LogUtil

class CalendarViewPagerAdapter(val views: List<CalendarScrollView>) : PagerAdapter() {

    companion object {
        //定义滚动结果常量
        private const val RESULT_PLUS = 1
        private const val RESULT_MINUS = -1
        private const val RESULT_KEEP = 0
    }

    override fun getCount(): Int {
        //总共滚动的页数
        return 11
    }

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    //实例化item，ViewPager会预加载左右两侧的view
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        LogUtil.d("CalendarAdapter", "position = $position")
        //根据position动态调整滚动的view
        val index = position % views.size
        val view = views[index].view
        //获取view的上个位置
        val lastPosition = views[index].lastPosition
        //更新view在viewPager中的位置
        views[index].lastPosition = position
        LogUtil.d("CalendarAdapter", "lastPosition = $lastPosition")
        //若view已存在在viewGroup中则移除（×**）
        if (container.indexOfChild(view) != -1) {
            container.removeView(view)
        }
        //加入view（**√），始终保持只有三个view，并且三个view交替变化
        container.addView(view)
        //刷新view的内容
        refreshView(views[index], lastPosition, position)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        //已经移除view，故销毁item时无需操作
    }

    @SuppressLint("NotifyDataSetChanged")//由于每月的数据集都不同，因此只能使用notifyDataSetChange
    private fun refreshView(calendarView: CalendarScrollView, lastPosition: Int, position: Int) {
        val recyclerView = calendarView.view as RecyclerView
        val adapter = recyclerView.adapter as CalendarViewAdapter
        val calendarUtil = adapter.calendarUtil

        when (getScrollResult(lastPosition, position)) {
            RESULT_PLUS -> {
                repeat(3) {
                    calendarUtil.plusOneMonth()
                }
                adapter.dateList.run {
                    this as ArrayList
                    clear()
                    addAll(calendarUtil.getDateList())
                }
                adapter.notifyDataSetChanged()
            }
            RESULT_MINUS -> {
                repeat(3) {
                    calendarUtil.minusOneMonth()
                }
                adapter.dateList.run {
                    this as ArrayList
                    clear()
                    addAll(calendarUtil.getDateList())
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun getScrollResult(lastPosition: Int, position: Int): Int =
        //向右滚动到达临界点触发循环时，view[2]会从八号位跳到二号位
        if (position == 2 && lastPosition == 8) {
            RESULT_PLUS
            //向左滚动到达临界点触发循环时，view[2]会从二号位跳到八号位
        } else if (position == 8 && lastPosition == 2) {
            RESULT_MINUS
            //判断滚动方向。由于循环时，view[1]会从十号位跳到一号位，且view[1]已经完成自增，故不能再加
        } else if (position - lastPosition > 0 && position - lastPosition != 9) {
            RESULT_PLUS
        } else if (position - lastPosition < 0 && position - lastPosition != -9) {
            RESULT_MINUS
        } else {
            RESULT_KEEP
        }

}