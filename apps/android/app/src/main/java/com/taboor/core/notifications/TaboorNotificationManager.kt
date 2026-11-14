package com.taboor.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.taboor.R

object TaboorNotificationManager {
    private const val NOTIFICATION_ID = 7101
    private const val CHANNEL_PREFIX = "queue_alerts_"

    fun showTestNotification(context: Context, isArabic: Boolean, customersAhead: Int, selectedChime: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false

        val chime = chimeFor(selectedChime)
        val channelId = CHANNEL_PREFIX + chime.key
        createChannel(context, channelId, chime, isArabic)

        val message = if (isArabic) {
            "أمامك $customersAhead عملاء فقط. هذه معاينة لصوت التنبيه المختار."
        } else {
            "Only $customersAhead customers ahead. This previews your selected alert sound."
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_taboor_symbol)
            .setContentTitle(if (isArabic) "دورك اقترب!" else "Your turn is near!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        return true
    }

    private fun createChannel(context: Context, channelId: String, chime: Chime, isArabic: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(channelId) != null) return

        val soundUri = Uri.parse("android.resource://${context.packageName}/${chime.resourceId}")
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val channelName = if (isArabic) "تنبيهات الدور - ${chime.nameAr}" else "Queue alerts - ${chime.nameEn}"

        manager.createNotificationChannel(
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = if (isArabic) "تنبيهات اقتراب الدور ومعاينة نغمتها" else "Queue turn alerts and their sound preview"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 220, 120, 320)
            }
        )
    }

    private fun chimeFor(key: String): Chime = when (key) {
        "modern_bell" -> Chime("modern_bell", "جرس طابور الكلاسيكي", "Classic Taboor Bell", R.raw.taboor_turn_chime)
        "crystal_glass" -> Chime("crystal_glass", "رنين الزجاج السائل", "Liquid Glass Ping", R.raw.taboor_glass_chime)
        "zen_bell" -> Chime("zen_bell", "هدوء التأمل", "Zen Temple Bell", R.raw.taboor_zen_chime)
        "dreamy_celestial" -> Chime("dreamy_celestial", "نغم أثيري ناعم", "Dreamy Celestial", R.raw.taboor_dreamy_chime)
        else -> Chime("soft_marimba", "ماريمبا خشبية هادئة", "Warm Marimba", R.raw.taboor_marimba_chime)
    }

    private data class Chime(val key: String, val nameAr: String, val nameEn: String, val resourceId: Int)
}
