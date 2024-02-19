package com.techja.musicapp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

class MyReciver: BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SuspiciousIndentation")
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action // nhận action từ service gửi sang
        if (action != null) {
            val serviceIntent = Intent(context, ServiceMusic::class.java)//khởi tạo nơi nhận dữ liệu
            serviceIntent.action = action//gửi action mới nhận dc qua service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(serviceIntent)
            } else {
                context?.startService(serviceIntent)//chạy hàm startcomant với action mới gửi đi
            }
        }
    }
}