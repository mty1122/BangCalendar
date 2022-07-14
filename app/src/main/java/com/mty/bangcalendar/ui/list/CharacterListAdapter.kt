package com.mty.bangcalendar.ui.list

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mty.bangcalendar.R
import com.mty.bangcalendar.logic.model.Character
import com.mty.bangcalendar.ui.main.MainActivity
import com.mty.bangcalendar.util.CalendarUtil
import com.mty.bangcalendar.util.CharacterUtil
import com.mty.bangcalendar.util.EventUtil

class CharacterListAdapter(private val characterList: List<Character>, private val context: Context)
    : RecyclerView.Adapter<CharacterListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val charName: TextView = view.findViewById(R.id.char_name)
        val charBir: TextView = view.findViewById(R.id.char_bir)
        val charColor: TextView = view.findViewById(R.id.char_color)
        val charBand: TextView = view.findViewById(R.id.char_band)
        val charPic: ImageView = view.findViewById(R.id.char_image)
        val charButton: Button = view.findViewById(R.id.characterButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.character_card, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.charButton.setOnClickListener {
            startMainActivity(viewHolder)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val character = characterList[position]
        holder.run {
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
            Glide.with(context).load(EventUtil.matchCharacter(character.id.toInt())).into(charPic)
        }
    }

    override fun getItemCount() = characterList.size

    private fun startMainActivity(viewHolder: ViewHolder) {
        val position = viewHolder.adapterPosition
        val jumpDate =
            CharacterUtil.getNextBirthdayDate(characterList[position].birthday, CalendarUtil())[0]

        val intent = Intent("com.mty.bangcalendar.JUMP_DATE")
        intent.setPackage(context.packageName)
        intent.putExtra("current_start_date", jumpDate)
        context.sendBroadcast(intent)

        val activityIntent = Intent(context, MainActivity::class.java)
        context.startActivity(activityIntent)
    }

}