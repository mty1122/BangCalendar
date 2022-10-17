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

/**
 * 采用类名作为tag发出debug log
 * @param obj 一般情况下传入this即可，若this为匿名类，则采用LogUtil作为Tag
 */
fun <T> log(obj: T, msg: String) {
    LogUtil.d(obj, msg)
}

object GenericUtil {

    private val clipboardManager: ClipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    /**
     * 复制文本到剪贴板
     * @param content 需要复制到剪贴板的文本内容
     * @param toast 弹出toast提示复制成功，本参数的默认值为“已复制到剪贴板”，如不想弹出toast，请传入null
     */
    fun copyToClipboard(content: String, toast: String? = "已复制到剪贴板") {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, content))
        toast?.let {
            toast(it)
        }
    }

}