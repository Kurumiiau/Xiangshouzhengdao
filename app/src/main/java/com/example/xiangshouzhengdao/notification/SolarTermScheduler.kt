package com.example.xiangshouzhengdao.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.xiangshouzhengdao.receiver.SolarTermReceiver
import java.util.Calendar

object SolarTermScheduler {

    private const val REQUEST_CODE = 1001
    private const val INTERVAL_MILLIS = 24 * 60 * 60 * 1000L // 24小时

    fun scheduleSolarTermCheck(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SolarTermReceiver::class.java).apply {
            action = SolarTermReceiver.ACTION_CHECK_SOLAR_TERM
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 每次广播结束后继续安排下一个 8 点，精确闹钟本身并不会重复。
        val triggerTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 需要 SCHEDULE_EXACT_ALARM 权限
            if (alarmManager.canScheduleExactAlarms()) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } catch (_: SecurityException) {
                    // 权限可能在检查后被撤回，至少保留普通提醒。
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerTime, INTERVAL_MILLIS, pendingIntent)
                }
            } else {
                // 使用非精确闹钟
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    INTERVAL_MILLIS,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                INTERVAL_MILLIS,
                pendingIntent
            )
        }
    }

    fun cancelSolarTermCheck(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SolarTermReceiver::class.java).apply {
            // action 也是 PendingIntent 身份的一部分，必须与安排时一致。
            action = SolarTermReceiver.ACTION_CHECK_SOLAR_TERM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun checkNow(context: Context) {
        val intent = Intent(context, SolarTermReceiver::class.java).apply {
            action = SolarTermReceiver.ACTION_CHECK_SOLAR_TERM
        }
        context.sendBroadcast(intent)
    }
}
