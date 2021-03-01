package com.hailiao.smsdemo.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.eazypermissions.common.model.PermissionResult
import com.eazypermissions.coroutinespermission.PermissionManager
import com.hailiao.smsdemo.R
import com.hailiao.smsdemo.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // 发送
        binding.btnSend.setOnClickListener {
            val phone = binding.edtPhone.text?.toString()
            if (phone.isNullOrEmpty()) return@setOnClickListener
            val content = binding.edtContent.text?.toString()
            if (content.isNullOrEmpty()) return@setOnClickListener
            val sms = getSmsManager()
            sms.sendTextMessage(phone, null, content, null, null)
        }

        GlobalScope.launch {
            checkPermissionWith( Manifest.permission.SEND_SMS )
            checkPermissionWith( Manifest.permission.READ_PHONE_STATE )
        }
    }
}

@SuppressLint("MissingPermission")
fun Context.getSmsManager(): SmsManager {
    // 手机卡管理器
    val subscriptionManager = getSystemService(AppCompatActivity.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    // 拿到手机里面的手机卡列表
    val list = subscriptionManager.activeSubscriptionInfoList
    Log.e("测试", "手机卡列表=$list")
    // [{id=13, iccId=898659171[****] simSlotIndex=1 displayName=电信卫星 carrierName=电信卫星 nameSource=0 iconTint=-16746133 dataRoaming=0 iconBitmap=android.graphics.Bitmap@a2d6fc2 mcc 0 mnc 0 isEmbedded false accessRules null}]
    // 通过反射修改当前短信管理器使用的卡ID
    val sms = SmsManager.getDefault()
    if (list.size > 0) {
        try {
            val clz = SmsManager::class.java
            val field = clz.getDeclaredField("mSubId")
            field.isAccessible = true
            val subscriptionInfoClz = SubscriptionInfo::class.java
            val cardIdField = subscriptionInfoClz.getDeclaredField("mId")
            cardIdField.isAccessible = true
            field.set(sms, cardIdField.get(list[0]))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return sms
}

/**
 * 检查权限
 * @receiver BaseActivity
 * @return Boolean
 */
suspend fun AppCompatActivity.checkPermissionWith(permission: String): Boolean {
    return suspendCoroutine { cont ->
        GlobalScope.launch {
            // 请求定位权限
            val permissionResult = PermissionManager.requestPermissions(
                this@checkPermissionWith,
                1,
                permission,
            )
            if (permissionResult is PermissionResult.PermissionGranted) {
                // 请求成功
                cont.resume(true)
            } else {
                cont.resume(false)
            }
        }
    }
}