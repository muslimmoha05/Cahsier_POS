package com.lovelycafe.casheirpos.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Create the notification channel for notifications
        createNotificationChannel()
    }

    // Method to create the notification channel
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "order_notifications"
            val channelName = "Order Notifications"
            val channelDescription = "Notifications for new orders"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
