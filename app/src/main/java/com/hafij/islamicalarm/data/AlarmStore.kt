package com.hafij.islamicalarm.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * খুব ছোট JSON-ভিত্তিক লোকাল স্টোরেজ। Room ব্যবহার না করে
 * SharedPreferences এ JSON আকারে সব এলার্ম সেভ রাখা হয়েছে,
 * যাতে প্রজেক্ট সহজ ও ডিপেন্ডেন্সি-লাইট থাকে।
 */
object AlarmStore {
    private const val PREFS = "islamic_alarm_prefs"
    private const val KEY_ALARMS = "alarms_json"
    private const val KEY_NEXT_ID = "next_id"

    fun getAll(context: Context): MutableList<AlarmItem> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ALARMS, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val result = mutableListOf<AlarmItem>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            result.add(
                AlarmItem(
                    id = o.getInt("id"),
                    hour = o.getInt("hour"),
                    minute = o.getInt("minute"),
                    lockDurationMinutes = o.getInt("duration"),
                    label = o.optString("label", ""),
                    enabled = o.optBoolean("enabled", true)
                )
            )
        }
        return result
    }

    fun saveAll(context: Context, alarms: List<AlarmItem>) {
        val arr = JSONArray()
        for (a in alarms) {
            val o = JSONObject()
            o.put("id", a.id)
            o.put("hour", a.hour)
            o.put("minute", a.minute)
            o.put("duration", a.lockDurationMinutes)
            o.put("label", a.label)
            o.put("enabled", a.enabled)
            arr.put(o)
        }
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ALARMS, arr.toString()).apply()
    }

    fun nextId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = prefs.getInt(KEY_NEXT_ID, 1)
        prefs.edit().putInt(KEY_NEXT_ID, id + 1).apply()
        return id
    }

    fun addOrUpdate(context: Context, item: AlarmItem) {
        val all = getAll(context)
        val idx = all.indexOfFirst { it.id == item.id }
        if (idx >= 0) all[idx] = item else all.add(item)
        saveAll(context, all)
    }

    fun delete(context: Context, id: Int) {
        val all = getAll(context).filterNot { it.id == id }
        saveAll(context, all)
    }
}
