package com.hafij.islamicalarm

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hafij.islamicalarm.data.AlarmItem
import com.hafij.islamicalarm.data.AlarmStore
import com.hafij.islamicalarm.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AlarmAdapter(
            onToggle = { item, enabled ->
                item.enabled = enabled
                AlarmStore.addOrUpdate(this, item)
                if (enabled) AlarmScheduler.schedule(this, item) else AlarmScheduler.cancel(this, item)
            },
            onDelete = { item ->
                AlarmScheduler.cancel(this, item)
                AlarmStore.delete(this, item.id)
                refreshList()
            },
            onClick = { item -> showAddEditDialog(item) }
        )

        binding.alarmRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.alarmRecyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener { showAddEditDialog(null) }

        requestExactAlarmPermissionIfNeeded()
        refreshList()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val alarms = AlarmStore.getAll(this).sortedWith(compareBy({ it.hour }, { it.minute }))
        adapter.submitList(alarms)
        binding.emptyText.visibility = if (alarms.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    private fun showAddEditDialog(existing: AlarmItem?) {
        val cal = Calendar.getInstance()
        val hour = existing?.hour ?: cal.get(Calendar.HOUR_OF_DAY)
        val minute = existing?.minute ?: cal.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, h, m ->
            showDurationAndLabelDialog(existing, h, m)
        }, hour, minute, false).show()
    }

    private fun showDurationAndLabelDialog(existing: AlarmItem?, hour: Int, minute: Int) {
        val view = layoutInflater.inflate(R.layout.dialog_add_alarm, null)
        val labelInput = view.findViewById<EditText>(R.id.labelInput)
        val durationPicker = view.findViewById<NumberPicker>(R.id.durationPicker)

        durationPicker.minValue = 1
        durationPicker.maxValue = 60
        durationPicker.value = existing?.lockDurationMinutes ?: 5
        labelInput.setText(existing?.label ?: "")

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) R.string.add_alarm else R.string.edit_alarm)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                val item = existing?.copy(
                    hour = hour,
                    minute = minute,
                    lockDurationMinutes = durationPicker.value,
                    label = labelInput.text.toString()
                ) ?: AlarmItem(
                    id = AlarmStore.nextId(this),
                    hour = hour,
                    minute = minute,
                    lockDurationMinutes = durationPicker.value,
                    label = labelInput.text.toString(),
                    enabled = true
                )
                AlarmStore.addOrUpdate(this, item)
                AlarmScheduler.schedule(this, item)
                refreshList()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
