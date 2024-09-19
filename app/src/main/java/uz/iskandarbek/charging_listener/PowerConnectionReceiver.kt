package uz.iskandarbek.charging_listener

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class PowerConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        val isCharging: Boolean = action == Intent.ACTION_POWER_CONNECTED
        val message = if (isCharging) {
            "Quvvat olayapti"
        } else {
            "Quvvat uzildi"
        }

        // Bildirishnoma chiqarish
        context?.let {
            showNotification(it, message)
        }
    }

    private fun showNotification(context: Context, message: String) {
        val channelId = "charging_status_channel"
        val channelName = "Charging Status Notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Charging status updates"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Quvvat holati")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Yuqori ustuvorlik
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(0, notification)
    }
}
