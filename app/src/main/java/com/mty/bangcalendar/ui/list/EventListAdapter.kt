package com.mty.bangcalendar.ui.list

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.card.MaterialCardView
import com.mty.bangcalendar.R
import com.mty.bangcalendar.logic.model.Event
import com.mty.bangcalendar.ui.main.MainActivity
import com.mty.bangcalendar.util.EventUtil
import com.mty.bangcalendar.util.ThemeUtil
import com.tomergoldst.progress_circle.ProgressCircle

class EventListAdapter(private val eventList: List<Event> , private val context: Context) :
    RecyclerView.Adapter<EventListAdapter.ViewHolder>(){

    private val glideOptions = RequestOptions()
        .skipMemoryCache(false)
        .diskCacheStrategy(DiskCacheStrategy.ALL)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val eventCard: MaterialCardView = view.findViewById(R.id.listEventCard)
        val eventType: TextView = eventCard.findViewById(R.id.eventType)
        val eventBackground: TextView = eventCard.findViewById(R.id.eventBackground)
        val eventAttrs: ImageView = eventCard.findViewById(R.id.eventAttrs)
        val eventButton: Button = eventCard.findViewById(R.id.eventButton)
        val char1: ImageView = eventCard.findViewById(R.id.char1)
        val char2: ImageView = eventCard.findViewById(R.id.char2)
        val char3: ImageView = eventCard.findViewById(R.id.char3)
        val char4: ImageView = eventCard.findViewById(R.id.char4)
        val char5: ImageView = eventCard.findViewById(R.id.char5)
        val eventBand: ImageView = eventCard.findViewById(R.id.eventBand)
        val eventProgress: ProgressCircle = eventCard.findViewById(R.id.eventProgress)
        val eventProgressName: TextView = eventCard.findViewById(R.id.eventProgressName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.event_list_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            startMainActivity(viewHolder)
        }
        viewHolder.eventButton.setOnClickListener {
            startMainActivity(viewHolder)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = eventList[position]
        holder.run {
            eventProgress.progress = 66
            eventProgress.progressColor =
                context.getColor(ThemeUtil.getThemeColor(context))
            eventProgress.textColor =
                context.getColor(ThemeUtil.getThemeColor(context))
            eventProgressName.text = "浏览模式"
            //刷新活动类型
            eventType.text = StringBuilder().run {
                append("活动")
                append(event.id)
                append(" ")
                append(EventUtil.matchType(event.type))
                toString()
            }
            //刷新活动角色
            Glide.with(context).load(EventUtil.matchCharacter(event.character1)).into(char1)
            Glide.with(context).load(EventUtil.matchCharacter(event.character2)).into(char2)
            Glide.with(context).load(EventUtil.matchCharacter(event.character3)).into(char3)
            Glide.with(context).load(EventUtil.matchCharacter(event.character4)).into(char4)
            Glide.with(context).load(EventUtil.matchCharacter(event.character5)).into(char5)
            //刷新活动属性
            Glide.with(context).load(EventUtil.matchAttrs(event.attrs)).into(eventAttrs)
            //刷新乐队图片
            Glide.with(context).load(EventUtil.getBandPic(event)).into(eventBand)
            //刷新活动图片
            val eventId = EventUtil.eventIdFormat(event.id.toInt())
            val uri = Uri.parse("https://www.mxmnb.cn/bangcalendar/" +
                    "event/banner_memorial_event$eventId.png")
            Glide.with(context).load(uri).apply(glideOptions)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable
                                                 , transition: Transition<in Drawable>?) {
                        eventBackground.background = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        //
                    }
                })
        }
    }

    override fun getItemCount() = eventList.size

    private fun startMainActivity(viewHolder: ViewHolder) {
        val intent = Intent("com.mty.bangcalendar.JUMP_DATE")
        intent.setPackage(context.packageName)
        val position = viewHolder.adapterPosition
        intent.putExtra("current_start_date", eventList[position].startDate)
        context.sendBroadcast(intent)

        val activityIntent = Intent(context, MainActivity::class.java)
        context.startActivity(activityIntent)
    }

}