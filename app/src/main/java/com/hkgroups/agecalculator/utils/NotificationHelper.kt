package com.hkgroups.agecalculator.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.hkgroups.agecalculator.R

const val NOTIFICATION_CHANNEL_ID = "horoscope_channel"
const val NOTIFICATION_ID = 1

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Daily Horoscope",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Get your daily horoscope reading."
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showHoroscopeNotification(signName: String, horoscope: String) {
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // You'd replace this with a proper icon
            .setContentTitle("Your Daily Horoscope for $signName")
            .setContentText(horoscope)
            .setStyle(NotificationCompat.BigTextStyle().bigText(horoscope))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}