package com.shong.klog.floating

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

internal class NotificationMaker constructor(private val context: Context) {
    companion object {
        const val SERVICE_NOTI_ID = 6231
        const val CHANNEL_ID = "SimpleActLogging"
        const val CHANNEL_NAME = "FloatingLogging"
        const val SERVICE_TITLE = "title"
        const val SERVICE_CONTENT = "content"
        const val SERVICE_SUB = "sub"
    }

    private var notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    internal fun builder(): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(SERVICE_TITLE)
            .setContentText(SERVICE_CONTENT)
            .setSubText(SERVICE_SUB)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.description = "This is MiniCalculator Notification"
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


}