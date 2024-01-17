package com.mty.bangcalendar.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.collection.ArrayMap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mty.bangcalendar.R
import com.mty.bangcalendar.ui.main.MainViewModel
import com.mty.bangcalendar.util.CalendarUtil
import com.mty.bangcalendar.util.EventUtil
import com.mty.bangcalendar.util.LogUtil
import com.mty.bangcalendar.util.ThemeUtil
import de.hdodenhof.circleimageview.CircleImageView

class CalendarViewAdapter(private val context: Context, var dateList: List<String>,
    val calendarUtil: CalendarUtil, private val viewModel: MainViewModel
)
    : RecyclerView.Adapter<CalendarViewAdapter.ViewHolder>() {

    val birthdayMap = ArrayMap<String, Int>()

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
            val day = holder.date.text.toString()
            if (day != "") {
                //如果不为空，则选中目标日期
                val intDay = Integer.parseInt(day)
                viewModel.run {
                    //避免重复点击
                    if (selectedItem.value != intDay) {
                        currentDate.value?.day = intDay
                        setSelectedItem(intDay) //选中目标
                        refreshCurrentDate() //刷新日期
                        //刷新生日卡片
                        val characterId = birthdayMap[day]
                        if (characterId != null) {
                            LogUtil.d("Character", "有角色过生日")
                            viewModel.refreshBirthdayCard(characterId)
                        }else {
                            viewModel.refreshBirthdayCard(0)
                        }
                    }
                }
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //获取生日角色
        val characterId = birthdayMap[dateList[position]]
        //五行显示不下动态调整六行
        if (calendarUtil.rows == CalendarUtil.SIX_ROWS) {
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
        //设置选中项背景
        if (dateList[position] != "" && Integer.parseInt(dateList[position])
            == viewModel.selectedItem.value) {
            holder.birthday.visibility = View.GONE
            holder.selectBg.visibility = View.VISIBLE
            holder.date.setTextColor(context.getColor(R.color.white))
            LogUtil.d("Calendar", "$position 被选中")
        } else if (dateList[position] != "" && characterId != null) { //如果生日角色存在则设置角色为背景
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

    override fun getItemCount() = dateList.size

}