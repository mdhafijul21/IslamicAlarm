package com.hafij.islamicalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hafij.islamicalarm.data.AlarmStore

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarms = AlarmStore.getAll(context)
            alarms.filter { it.enabled }.forEach { AlarmScheduler.schedule(context, it) }
        }
    }
}
