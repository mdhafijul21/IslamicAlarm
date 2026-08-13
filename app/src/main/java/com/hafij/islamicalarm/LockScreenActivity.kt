package com.hafij.islamicalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.TelephonyManager
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.hafij.islamicalarm.databinding.ActivityLockScreenBinding

class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenBinding
    private var countDownTimer: CountDownTimer? = null
    private var remainingMillis: Long = 0
    private var lockActive = false

    private val callStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING,
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // কল আসলে বা রিসিভ করলে সাময়িকভাবে লক-টাস্ক বন্ধ করে
                    // ফোন অ্যাপকে সামনে আসতে দেওয়া হচ্ছে
                    releaseLockTaskIfNeeded()
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // কল শেষ, বাকি সময় থাকলে আবার লক-স্ক্রিনে ফিরিয়ে আনা
                    if (remainingMillis > 0) {
                        val bringBack = Intent(this@LockScreenActivity, LockScreenActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        }
                        startActivity(bringBack)
                        reapplyLockTask()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowFlags()
        hideSystemBars()

        val durationMinutes = intent.getIntExtra("duration_minutes", 5)
        val label = intent.getStringExtra("label") ?: ""
        binding.labelText.text = if (label.isNotBlank()) label else getString(R.string.prayer_time_now)

        remainingMillis = durationMinutes * 60_000L
        startCountdown(remainingMillis)

        registerReceiver(callStateReceiver, IntentFilter("android.intent.action.PHONE_STATE"))

        enterLockTask()
    }

    private fun setupWindowFlags() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }

    private fun hideSystemBars() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }

    private fun startCountdown(millis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(msLeft: Long) {
                remainingMillis = msLeft
                val totalSec = msLeft / 1000
                val m = totalSec / 60
                val s = totalSec % 60
                binding.countdownText.text = String.format("%02d:%02d", m, s)
            }

            override fun onFinish() {
                remainingMillis = 0
                finishLockScreen()
            }
        }.start()
    }

    private fun enterLockTask() {
        try {
            startLockTask()
            lockActive = true
        } catch (e: Exception) {
            // ডিভাইসে screen pinning অনুমতি না থাকলে সিস্টেম নিজে থেকেই
            // প্রথমবার একটা কনফার্মেশন ডায়ালগ দেখাবে; ব্যবহারকারীকে
            // Settings > Security > "App pinning" চালু করতে বলা যেতে পারে।
        }
    }

    private fun releaseLockTaskIfNeeded() {
        if (lockActive) {
            try {
                stopLockTask()
            } catch (_: Exception) { }
            lockActive = false
        }
    }

    private fun reapplyLockTask() {
        if (!lockActive) enterLockTask()
    }

    private fun finishLockScreen() {
        releaseLockTaskIfNeeded()
        countDownTimer?.cancel()
        try {
            unregisterReceiver(callStateReceiver)
        } catch (_: Exception) { }
        finish()
    }

    // ব্যাক বাটন সম্পূর্ণভাবে নিষ্ক্রিয় — সময় শেষ না হওয়া পর্যন্ত কিছুই হবে না
    override fun onBackPressed() {
        // ইচ্ছাকৃতভাবে খালি — ব্যাক বাটন কাজ করবে না
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        try {
            unregisterReceiver(callStateReceiver)
        } catch (_: Exception) { }
        super.onDestroy()
    }
}
