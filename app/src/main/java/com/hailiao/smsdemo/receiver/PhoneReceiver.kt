package com.hailiao.smsdemo.receiver

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.hailiao.smsdemo.activity.getSmsManager

/**
 * 通话状态监听器
 *
 * @Author: D10NG
 * @Time: 2021/3/11 9:37 上午
 */
class PhoneReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?: return
        intent?: return
        if (intent.action == Intent.ACTION_NEW_OUTGOING_CALL) {
            Log.e("通话状态", "往外拨打电话中")
        }
        val manager = context.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
        when(manager.callState) {
            TelephonyManager.CALL_STATE_IDLE -> {
                Log.e("通话状态", "空闲中")
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                Log.e("通话状态", "通话中")
                // 发送短信
                val sms = context.getSmsManager()
                sms.sendTextMessage("13106673302", null, "测试${System.currentTimeMillis()}", null, null)
            }
            TelephonyManager.CALL_STATE_RINGING -> {
                Log.e("通话状态", "响铃中")
            }
        }
    }
}