package com.mty.bangcalendar.ui.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mty.bangcalendar.R
import com.mty.bangcalendar.ui.main.state.CalendarItemUiState
import com.mty.bangcalendar.util.CalendarUtil
import com.mty.bangcalendar.util.EventUtil
import com.mty.bangcalendar.util.GenericUtil
import com.mty.bangcalendar.util.LogUtil
import com.mty.bangcalendar.util.ThemeUtil
import de.hdodenhof.circleimageview.CircleImageView

class CalendarViewAdapter(
    private val context: Context,
    var uiState: CalendarItemUiState,
    val calendarUtil: CalendarUtil
) : RecyclerView.Adapter<CalendarViewAdapter.ViewHolder>() {

    private var selectedItemPosition = 0

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.dateItem)
        val selectBg: CircleImageView = view.findViewById(R.id.select_bg)
        val birthday: ImageView = view.findViewById(R.id.birthday)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.date_item, parent, false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            val selectedItem = holder.date.text.toString()
            if (selectedItem != "") {
                val selectedDay = selectedItem.toInt()
                val currentDay = uiState.getCurrentDate().getDay()
                //如果不是重复点击，则更新当前日期
                if (selectedDay != currentDay) {
                    uiState.onDateChange(
                        CalendarUtil.getDate(calendarUtil.year, calendarUtil.month, selectedDay)
                    )
                    //相同页面点击操作，先去除旧的选中，再更新新的选中
                    hideSelectedItem()
                    showSelectedItem()
                }
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int){
        //获取生日角色
        val characterId = uiState.birthdayMap[uiState.dateList[position]]
        //五行显示不下动态调整六行
        if (calendarUtil.rows == CalendarUtil.SIX_ROWS) {
            holder.itemView.layoutParams.let {
                it.height = GenericUtil.dpToPx(58)
                holder.itemView.layoutParams = it
            }
        }
        //添加日期
        holder.date.text = uiState.dateList[position]
        //设置选中项背景
        if (uiState.isVisible &&
            uiState.dateList[position] == uiState.getCurrentDate().getDay().toString()) {
            holder.birthday.visibility = View.GONE
            holder.selectBg.visibility = View.VISIBLE
            holder.date.setTextColor(context.getColor(R.color.white))
            LogUtil.d("Calendar", "$position 被选中")
            selectedItemPosition = position //记录最后一个被选中的item，用于更新
        //如果生日角色存在则设置角色为背景
        } else if (uiState.dateList[position] != "" && characterId != null) {
            holder.selectBg.visibility = View.GONE
            holder.date.setTextColor(context.getColor(R.color.transparent))
            Glide.with(context).load(EventUtil.matchCharacter(characterId)).into(holder.birthday)
            holder.birthday.visibility = View.VISIBLE
        } else {
            holder.selectBg.visibility = View.GONE
            holder.birthday.visibility = View.GONE
            holder.date.setTextColor(context.getColor(ThemeUtil.getDateTextColor(context)))
        }
    }

    override fun getItemCount() = uiState.dateList.size

    fun showSelectedItem() {
        uiState.dateList.forEachIndexed { index, day ->
            if (day == uiState.getCurrentDate().getDay().toString())
                notifyItemChanged(index)
        }
    }
    fun hideSelectedItem() {
        notifyItemChanged(selectedItemPosition)
    }

}