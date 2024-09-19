package uz.iskandarbek.charging_listener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class PowerConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        val isCharging: Boolean = action == Intent.ACTION_POWER_CONNECTED

        // Quvvat darajasini olish
        val batteryStatus = context?.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = level * 100 / scale.toFloat()

        val message = if (isCharging) {
            "Quvvat olayapti"
        } else {
            "Quvvat olmayapti"
        }

        val activity = context as MainActivity
        activity.updateUI(isCharging, message, batteryPct)
    }
}
