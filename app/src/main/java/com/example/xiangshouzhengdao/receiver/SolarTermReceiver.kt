package com.example.xiangshouzhengdao.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import com.example.xiangshouzhengdao.notification.NotificationHelper
import com.example.xiangshouzhengdao.notification.SolarTermScheduler
import com.example.xiangshouzhengdao.utils.SolarTermUtil

class SolarTermReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CHECK_SOLAR_TERM = "com.example.xiangshouzhengdao.CHECK_SOLAR_TERM"
        const val PREFS_NAME = "solar_term_prefs"
        const val KEY_LAST_SOLAR_TERM = "last_solar_term"

        fun forceNotify(context: Context, solarTerm: String? = null) {
            val term = solarTerm ?: SolarTermUtil.getCurrentSolarTerm()
            val theme = SolarTermUtil.getSolarTermTheme(term)

            NotificationHelper.createNotificationChannel(context)

            NotificationHelper.showSolarTermNotification(context, term, theme)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_CHECK_SOLAR_TERM -> {
                try {
                    checkAndNotifySolarTerm(context)
                } finally {
                    SolarTermScheduler.scheduleSolarTermCheck(context)
                }
            }
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // 闹钟不会跨设备重启保留，更新安装后也补一次安排。
                SolarTermScheduler.scheduleSolarTermCheck(context)
            }
        }
    }

    private fun checkAndNotifySolarTerm(context: Context) {
        val currentSolarTerm = SolarTermUtil.getCurrentSolarTerm()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSolarTerm = prefs.getString(KEY_LAST_SOLAR_TERM, "")

        // 如果节气发生变化，发送通知
        if (currentSolarTerm != lastSolarTerm) {
            val theme = SolarTermUtil.getSolarTermTheme(currentSolarTerm)

            NotificationHelper.createNotificationChannel(context)

            // 未获通知权限时不标记已通知，之后授权仍能补发当前节气。
            if (NotificationHelper.showSolarTermNotification(context, currentSolarTerm, theme)) {
                prefs.edit { putString(KEY_LAST_SOLAR_TERM, currentSolarTerm) }
            }
        }
    }
}
