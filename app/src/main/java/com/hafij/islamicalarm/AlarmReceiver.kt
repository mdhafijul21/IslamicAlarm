package com.hafij.islamicalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hafij.islamicalarm.data.AlarmStore

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        val duration = intent.getIntExtra("duration_minutes", 5)
        val label = intent.getStringExtra("label") ?: ""

        // ফুলস্ক্রিন লক অ্যাক্টিভিটি চালু করা হচ্ছে
        val lockIntent = Intent(context, LockScreenActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra("duration_minutes", duration)
            putExtra("label", label)
        }
        context.startActivity(lockIntent)

        // প্রতিদিন রিপিট করার জন্য পরের দিনের জন্য আবার শিডিউল করা
        val alarms = AlarmStore.getAll(context)
        val item = alarms.firstOrNull { it.id == alarmId }
        if (item != null && item.enabled) {
            AlarmScheduler.rescheduleForTomorrow(context, item)
        }
    }
}
