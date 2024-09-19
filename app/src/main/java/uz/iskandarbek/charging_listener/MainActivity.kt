package uz.iskandarbek.charging_listener

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var powerConnectionReceiver: PowerConnectionReceiver
    private var isInitialLoad = true // Dasturga kirishda animatsiyani faollashtirmaslik uchun

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Quvvat holatini tekshirish va o'rnatish
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = registerReceiver(null, intentFilter)
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging =
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // Quvvat darajasini olish
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = level * 100 / scale.toFloat()

        val message = if (isCharging) {
            "Quvvat olayapti"
        } else {
            "Quvvat olmayapti"
        }

        // Dasturga kirishda animatsiya yoqilmasligi uchun
        updateUI(isCharging, message, batteryPct)

        // BroadcastReceiver ni ro'yxatdan o'tkazish
        powerConnectionReceiver = PowerConnectionReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(powerConnectionReceiver, filter)
    }

    fun updateUI(isCharging: Boolean, message: String, batteryPct: Float) {
        val layout = findViewById<LinearLayout>(R.id.mainLayout)
        val statusTextView = findViewById<TextView>(R.id.statusTextView)
        val batteryLevelTextView = findViewById<TextView>(R.id.batteryLevelTextView)

        if (isInitialLoad) {
            // Dasturga kirishda birinchi holatni o'rnatish
            layout.setBackgroundColor(if (isCharging) Color.BLUE else Color.RED)
            isInitialLoad = false
        } else {
            // Rangni o'zgartirish animatsiyasi
            val colorFrom = if (isCharging) Color.RED else Color.BLUE
            val colorTo = if (isCharging) Color.BLUE else Color.RED
            val colorAnimation = ObjectAnimator.ofObject(
                layout, "backgroundColor", ArgbEvaluator(), colorFrom, colorTo
            )
            colorAnimation.duration = 3000 // 3 sekund
            colorAnimation.interpolator = DecelerateInterpolator()
            colorAnimation.start()
        }

        statusTextView.text = message
        batteryLevelTextView.text = "Quvvat darajasi: ${batteryPct.toInt()}%"
    }

    override fun onDestroy() {
        super.onDestroy()
        // BroadcastReceiver ni ro'yxatdan chiqarish
        unregisterReceiver(powerConnectionReceiver)
    }
}
