package com.shong.klog.floating

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.shong.klog.Klog
import com.shong.klog.floating.NotificationMaker.Companion.SERVICE_NOTI_ID

internal class FloatingService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private var floatingLoggerMaker: FloatingLoggerMaker? = null
    override fun onCreate() {
        super.onCreate()
        floatingLoggerMaker = FloatingLoggerMaker(this.baseContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            startForeground(SERVICE_NOTI_ID, NotificationMaker(this.baseContext).builder())
        }

        val autoStop =
            intent?.getBooleanExtra("autoStop", Klog.AUTO_STOP_BASE) ?: Klog.AUTO_STOP_BASE
        val max = intent?.getIntExtra("max", Klog.MAX_BASE) ?: Klog.MAX_BASE
        floatingLoggerMaker?.makeWindow(autoStop, max)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        floatingLoggerMaker?.closeWindow()
        super.onDestroy()
    }

}