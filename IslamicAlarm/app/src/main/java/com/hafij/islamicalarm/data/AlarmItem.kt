package com.hafij.islamicalarm.data

data class AlarmItem(
    val id: Int,
    var hour: Int,
    var minute: Int,
    var lockDurationMinutes: Int,
    var label: String = "",
    var enabled: Boolean = true
) {
    fun timeText(): String {
        val h = if (hour % 12 == 0) 12 else hour % 12
        val ampm = if (hour < 12) "AM" else "PM"
        return String.format("%02d:%02d %s", h, minute, ampm)
    }
}
