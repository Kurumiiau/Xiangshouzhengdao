package com.example.xiangshouzhengdao.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.i18n.Loc
import com.example.xiangshouzhengdao.ui.main.MainActivity

object NotificationHelper {

    private const val TAG = "NotificationHelper"
    private const val CHANNEL_ID = "solar_term_channel"

    // 渠道ID别随版本改，用户在系统里选的提醒设置绑在这个ID上。
    fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notify_channel),
                NotificationManager.IMPORTANCE_HIGH  // 使用 HIGH 确保可见
            ).apply {
                description = context.getString(R.string.notify_channel_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "通知渠道已创建: $CHANNEL_ID")
    }

    fun showSolarTermNotification(context: Context, solarTerm: String, theme: String): Boolean {
        // 广播也可能在用户撤回权限后执行，不能依赖主页之前的授权检查。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        if (!notificationManager.areNotificationsEnabled()) return false
        return try {
            Log.d(TAG, "开始显示通知: $solarTerm")

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = Loc.notifyTitle(Loc.solarTerm(solarTerm))
            val seasonKey = when (solarTerm) {
                "立春", "雨水", "惊蛰", "春分", "清明", "谷雨" -> "春疏肝"
                "立夏", "小满", "芒种", "夏至", "小暑", "大暑" -> "夏清心"
                "立秋", "处暑", "白露", "秋分", "寒露", "霜降" -> "秋润肺"
                else -> "冬温肾"
            }
            val content = Loc.notifyBody(seasonKey, Loc.solarTerm(solarTerm))

            // 构建通知 - 使用 setSmallIcon 必须有效
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 200, 500))

            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, notificationBuilder.build())

            Log.d(TAG, "通知已发送: ID=$notificationId, title=$title")
            true
        } catch (e: Exception) {
            Log.e(TAG, "发送通知失败: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }
}
