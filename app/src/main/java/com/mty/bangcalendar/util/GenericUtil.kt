package com.mty.bangcalendar.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.mty.bangcalendar.BangCalendarApplication.Companion.context

/**
 * 简化Activity的启动，示范：startActivity<ArticleActivity>("page" to 20)
 * @param pairs 等价于 intent.putExtra("page", 20)
 * 仅支持Int,Boolean,String,Long,Double,Float，其余会被转成String
 */
inline fun <reified T> Context.startActivity(vararg pairs: Pair<String, *>) {
    val intent = Intent(this, T::class.java)
    for (pair in pairs) {
        intent.putExtra(pair)
    }
    startActivity(intent)
}

/**
 * @param pair 仅支持Int,Boolean,String,Long,Double,Float，其余会被转成String
 */
fun Intent.putExtra(pair: Pair<String, *>) {
    when (pair.second) {
        is Int -> putExtra(pair.first, pair.second as Int)
        is Boolean -> putExtra(pair.first, pair.second as Boolean)
        is String -> putExtra(pair.first, pair.second as String)
        is Long -> putExtra(pair.first, pair.second as Long)
        is Double -> putExtra(pair.first, pair.second as Double)
        is Float -> putExtra(pair.first, pair.second as Float)
        else -> putExtra(pair.first, pair.second.toString())
    }
}

fun toast(text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

object GenericUtil {

    private val clipboardManager: ClipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    fun copyToClipboard(content: String, makeToast: Boolean = true) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, content))
        if (makeToast)
            toast("已复制到剪贴板")
    }

}