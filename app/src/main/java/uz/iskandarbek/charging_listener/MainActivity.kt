package uz.iskandarbek.charging_listener

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 1

    private lateinit var powerConnectionReceiver: BroadcastReceiver
    private var isInitialLoad = true // Dasturga kirishda animatsiyani faollashtirmaslik uchun

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkNotificationPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission()
            }
        }
        // Quvvat darajasini kuzatish uchun BroadcastReceiver ni yaratish
        powerConnectionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Quvvat darajasi va holatini yangilash
                updateBatteryStatus(intent)
            }
        }

        // BroadcastReceiver ni ro'yxatdan o'tkazish
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(powerConnectionReceiver, filter)

        // PowerConnectionReceiver dan Broadcastni tinglash
        LocalBroadcastManager.getInstance(this).registerReceiver(
            powerConnectionReceiver,
            IntentFilter("POWER_STATUS_ACTION")
        )
    }

    private fun requestNotificationPermission() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Foydalanuvchi ruxsat berdi
            } else {
                // Foydalanuvchi ruxsat bermadi
            }
        }

        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun checkNotificationPermission() {
        // Agar ruxsat berilmagan bo'lsa, ruxsatni so'rash
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    // Ruxsat natijasini olish
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Ruxsat berildi
            } else {
                // Ruxsat rad etildi
            }
        }
    }

    private fun updateBatteryStatus(intent: Intent?) {
        if (intent == null) return

        // Quvvat holatini olish
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging =
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // Quvvat darajasini olish
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = level * 100 / scale.toFloat()

        val message = if (isCharging) {
            "Quvvat olayapti"
        } else {
            "Quvvat olmayapti"
        }

        // Dasturga kirishda animatsiya yoqilmasligi uchun
        updateUI(isCharging, message, batteryPct)
    }

    fun updateUI(isCharging: Boolean, message: String, batteryPct: Float) {
        val layout = findViewById<View>(R.id.mainLayout)
        val statusTextView = findViewById<TextView>(R.id.statusTextView)
        val batteryLevelTextView = findViewById<TextView>(R.id.batteryLevelTextView)

        if (isInitialLoad) {
            // Dasturga kirishda birinchi holatni o'rnatish
            layout.setBackgroundColor(if (isCharging) Color.BLUE else Color.RED)
            isInitialLoad = false
        } else {
            // Rangni o'zgartirish animatsiyasi
            val colorFrom = (layout.background as ColorDrawable).color
            val colorTo = if (isCharging) Color.BLUE else Color.RED
            val colorAnimation = ObjectAnimator.ofObject(
                layout, "backgroundColor", ArgbEvaluator(), colorFrom, colorTo
            )
            colorAnimation.duration = 2000 // 2 sekund
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
