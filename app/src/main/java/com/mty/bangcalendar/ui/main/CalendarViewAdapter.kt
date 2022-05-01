package com.mty.bangcalendar.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mty.bangcalendar.R
import com.mty.bangcalendar.logic.util.CalendarUtil
import com.mty.bangcalendar.logic.util.LogUtil

class CalendarViewAdapter(private val context: Context, var dateList: List<String>,
    val calendarUtil: CalendarUtil, private val viewModel: MainViewModel)
    : RecyclerView.Adapter<CalendarViewAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.dateItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.date_item, parent, false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            val day = holder.date.text.toString()
            if (day != "") {
                //如果不为空，则选中目标日期
                viewModel.run {
                    currentDate.value?.day = Integer.parseInt(day)
                    setSelectedItem(Integer.parseInt(day)) //选中目标
                    refreshCurrentDate() //刷新日期
                }
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dayOfWeek = calendarUtil.getDayOfWeak()
        val maxDay = calendarUtil.getMaximumDaysInMonth()
        //五行显示不下动态调整六行
        if (dayOfWeek + maxDay > 36) {
            val scale = context.resources.displayMetrics.density
            val dpValue = 58
            val pxValue = (dpValue * scale + 0.5f).toInt()
            holder.itemView.layoutParams.let {
                it.height = pxValue
                holder.itemView.layoutParams = it
            }
        }
        //添加日期
        holder.date.text = dateList[position]
        //设置背景
        if (dateList[position] != "" && Integer.parseInt(dateList[position])
            == viewModel.selectedItem.value) {
            holder.date.setBackgroundColor(context.getColor(R.color.ppp_bar))
            LogUtil.d("Calendar", "$position 被选中")
        }
    }

    override fun getItemCount() = dateList.size

}