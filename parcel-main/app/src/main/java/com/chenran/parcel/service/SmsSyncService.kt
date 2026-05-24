package com.chenran.parcel.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.chenran.parcel.util.addLog

class SmsSyncService : Service() {
    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= 26) {
            val mgr = getSystemService(NotificationManager::class.java)
            val id = "parcel_sync"
            if (mgr?.getNotificationChannel(id) == null) {
                val ch = NotificationChannel(id, "数据更新", NotificationManager.IMPORTANCE_MIN)
                ch.setShowBadge(false)
                ch.enableLights(false)
                ch.enableVibration(false)
                mgr?.createNotificationChannel(ch)
            }
            val notif = NotificationCompat.Builder(this, id)
                .setSmallIcon(com.chenran.parcel.R.drawable.ic_notification)
                .setContentTitle("数据更新")
                .setContentText("正在处理短信")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()
            startForeground(1002, notif)
        }
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            try {
                Thread.sleep(1500L)
                val i = Intent("com.chenran.parcel.CUSTOM_SMS_ADDED").setPackage(packageName)
                sendBroadcast(i)
                try {
                    val mgr = android.appwidget.AppWidgetManager.getInstance(this)
                    com.chenran.parcel.widget.ParcelWidget.updateAppWidget(this, mgr, null, null)
                    com.chenran.parcel.widget.ParcelWidgetLarge.updateAppWidget(this, mgr, null, null)
                    com.chenran.parcel.widget.ParcelWidgetXL.updateAppWidget(this, mgr, null, null)
                    com.chenran.parcel.widget.ParcelWidgetMiui.updateAppWidget(this, mgr, null, null)
                    com.chenran.parcel.widget.ParcelWidgetLargeMiui.updateAppWidget(this, mgr, null, null)
                } catch (_: Exception) {}
                addLog(this, "短信前台服务刷新完成并广播更新")
            } catch (e: Exception) { addLog(this, "短信服务出错: ${e.message}") }
            try { stopForeground(true) } catch (_: Exception) {}
            stopSelf()
        }.start()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
