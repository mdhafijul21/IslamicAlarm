package com.hafij.islamicalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hafij.islamicalarm.data.AlarmItem
import java.util.Calendar

object AlarmScheduler {

    private fun pendingIntent(context: Context, item: AlarmItem): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", item.id)
            putExtra("duration_minutes", item.lockDurationMinutes)
            putExtra("label", item.label)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, item.id, intent, flags)
    }

    fun schedule(context: Context, item: AlarmItem) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, item.hour)
            set(Calendar.MINUTE, item.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // যদি সময়টা আজকের জন্য পার হয়ে গিয়ে থাকে, পরের দিনে সেট হবে
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val pi = pendingIntent(context, item)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            // পারমিশন না থাকলে inexact fallback (ইউজারকে সেটিংস থেকে অনুমতি দিতে হবে সঠিক সময়ের জন্য)
            am.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            return
        }

        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
    }

    fun cancel(context: Context, item: AlarmItem) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, item))
    }

    /** প্রতিদিন রিপিট করার জন্য পরের দিনের এলার্মও রি-শিডিউল করতে ব্যবহার হয় (রিসিভারে কল হয়) */
    fun rescheduleForTomorrow(context: Context, item: AlarmItem) {
        schedule(context, item)
    }
}
