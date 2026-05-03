package com.hkgroups.agecalculator.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.hkgroups.agecalculator.MainActivity
import com.hkgroups.agecalculator.R

const val NOTIFICATION_CHANNEL_ID = "horoscope_channel"
const val NOTIFICATION_ID = 1
const val COSMIC_EVENTS_CHANNEL_ID = "cosmic_events_channel"
const val COSMIC_EVENT_NOTIFICATION_ID = 2
const val MOOD_REMINDER_CHANNEL_ID = "mood_reminder_channel"
const val MOOD_REMINDER_NOTIFICATION_ID = 3

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

    fun createCosmicEventsChannel() {
        val channel = NotificationChannel(
            COSMIC_EVENTS_CHANNEL_ID,
            "Cosmic Events",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for eclipses, retrogrades, equinoxes, and solstices."
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showHoroscopeNotification(signName: String, horoscope: String) {
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_horoscope)
            .setContentTitle("Your Daily Horoscope for $signName")
            .setContentText(horoscope)
            .setStyle(NotificationCompat.BigTextStyle().bigText(horoscope))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun createMoodReminderChannel() {
        val channel = NotificationChannel(
            MOOD_REMINDER_CHANNEL_ID,
            "Evening mood log",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Gentle 8pm reminder to log how you felt today."
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showMoodReminderNotification(title: String, body: String) {
        val builder = NotificationCompat.Builder(context, MOOD_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_horoscope)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())

        notificationManager.notify(MOOD_REMINDER_NOTIFICATION_ID, builder.build())
    }

    fun showCosmicEventNotification(title: String, body: String) {
        val builder = NotificationCompat.Builder(context, COSMIC_EVENTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_horoscope)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())

        notificationManager.notify(COSMIC_EVENT_NOTIFICATION_ID, builder.build())
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}