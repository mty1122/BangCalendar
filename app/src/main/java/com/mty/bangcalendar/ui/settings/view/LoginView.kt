package com.mty.bangcalendar.ui.settings.view

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.mty.bangcalendar.R
import com.mty.bangcalendar.util.toast
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.util.regex.Pattern
import javax.inject.Inject

class LoginView @Inject constructor(
    @ActivityContext private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {

    fun loginDialog(
        sendSms: suspend (phoneNumber: String) -> Result<ResponseBody>,
        login: suspend (phoneNumber: String, smsCode: String) -> Result<ResponseBody>,
        onSuccess: (phoneNumber: String) -> Unit
    ): AlertDialog {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.login, null, false)
        val phoneText: EditText = view.findViewById(R.id.sms_phone)
        val codeText: EditText = view.findViewById(R.id.sms_code)
        val dialog = AlertDialog.Builder(context)
            .setTitle("登录")
            .setIcon(R.mipmap.ic_launcher)
            .setView(view)
            .setCancelable(false)
            .create()
            .apply { setCanceledOnTouchOutside(false) }

        view.findViewById<Button>(R.id.send_sms_button).setOnClickListener {
            val phoneNumber = phoneText.text.toString()
            if (phoneNumber.isPhoneNum()) {
                val button = it as Button
                button.isEnabled = false
                button.text = context.getString(R.string.sms_sending)
                lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val result = sendSms(phoneNumber)
                    val response = result.getOrNull()
                    withContext(Dispatchers.Main) {
                        if (response != null && response.string() == "OK") {
                            button.text = context.getString(R.string.sms_send_success)
                            codeText.requestFocus()
                        } else {
                            toast("发送失败，请重试，或检查网络连接")
                            button.isEnabled = true
                            button.text = context.getString(R.string.sms_send)
                        }
                    }
                }
            } else
                toast("请输入正确的手机号")
        }
        view.findViewById<Button>(R.id.login_cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.login_button).setOnClickListener {
            //验证手机号和验证码合法性，登录中禁用按钮
            val phoneNumber = phoneText.text.toString()
            val smsCode = codeText.text.toString()
            if (phoneNumber.isPhoneNum() && smsCode.length == 6) {
                val button = it as Button
                button.isEnabled = false
                button.text = context.getString(R.string.logging)
                lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val result = login(phoneNumber, smsCode)
                    val response = result.getOrNull()
                    withContext(Dispatchers.Main) {
                        if (response != null && response.string() == "OK") {
                            onSuccess(phoneNumber)
                            dialog.dismiss()
                            toast("登录成功")
                        } else {
                            toast("手机号或验证码错误")
                            button.isEnabled = true
                            button.text = context.getString(R.string.sign_in_title)
                        }
                    }
                }
            }
        }
        return dialog
    }

    private fun String.isPhoneNum(): Boolean {
        val regex = "\\b1[3-9]\\d{9}\\b"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(this)
        return matcher.find()
    }

}