package com.example.xiangshouzhengdao.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.databinding.ActivityMainBinding
import com.example.xiangshouzhengdao.notification.SolarTermScheduler
import com.example.xiangshouzhengdao.ui.home.HomeFragment
import com.example.xiangshouzhengdao.ui.profile.ProfileFragment
import com.example.xiangshouzhengdao.ui.survey.SurveyFragment

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val TAB_SURVEY = 0
        private const val TAB_HOME = 1
        private const val TAB_PROFILE = 2
        private const val STATE_CURRENT_TAB = "state_current_tab"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navItems: List<View>
    private lateinit var navIconBoxes: List<View>
    private lateinit var navIcons: List<ImageView>
    private lateinit var navLabels: List<TextView>
    private var currentTab = TAB_HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navItems = listOf(binding.navSurvey, binding.navHome, binding.navProfile)
        navIconBoxes = listOf(binding.navSurveyIconBox, binding.navHomeIconBox, binding.navProfileIconBox)
        navIcons = listOf(binding.navSurveyIcon, binding.navHomeIcon, binding.navProfileIcon)
        navLabels = listOf(binding.navSurveyLabel, binding.navHomeLabel, binding.navProfileLabel)

        setupBottomNavigation()

        currentTab = savedInstanceState?.getInt(STATE_CURRENT_TAB) ?: TAB_HOME
        if (savedInstanceState == null) {
            loadFragment(createFragment(currentTab))
        }
        applyNavState(currentTab, animate = false)

        // 检查并申请通知权限(Android 13+)
        checkNotificationPermission()
    }

    private fun setupBottomNavigation() {
        navItems.forEachIndexed { index, item ->
            item.setOnClickListener { switchTab(index) }
        }
    }

    // 重复点当前页不重建Fragment，避免重发天气请求和丢掉页面状态。
    private fun switchTab(index: Int) {
        if (index == currentTab) return
        currentTab = index
        applyNavState(index, animate = true)
        loadFragment(createFragment(index))
    }

    private fun createFragment(index: Int): androidx.fragment.app.Fragment = when (index) {
        TAB_SURVEY -> SurveyFragment()
        TAB_PROFILE -> ProfileFragment()
        else -> HomeFragment()
    }

    
    private fun applyNavState(selectedIndex: Int, animate: Boolean) {
        navItems.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            val icon = navIcons[index]
            val label = navLabels[index]
            val iconBox = navIconBoxes[index]

            item.isSelected = selected
            icon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, if (selected) R.color.primary else R.color.text_hint)
            )
            label.setTextColor(
                ContextCompat.getColor(this, if (selected) R.color.text_primary else R.color.text_hint)
            )
            label.alpha = if (selected) 1f else 0.92f
            iconBox.backgroundTintList = ColorStateList.valueOf(
                if (selected) ContextCompat.getColor(this, R.color.nav_selected_bg)
                else android.graphics.Color.TRANSPARENT
            )

            if (!animate) return@forEachIndexed

            iconBox.animate().cancel()
            icon.animate().cancel()
            if (selected) {
                iconBox.scaleX = 0.8f
                iconBox.scaleY = 0.8f
                icon.scaleX = 0.8f
                icon.scaleY = 0.8f
                iconBox.animate().scaleX(1f).scaleY(1f).setDuration(240)
                    .setInterpolator(OvershootInterpolator(2.4f)).start()
                icon.animate().scaleX(1f).scaleY(1f).setDuration(240)
                    .setInterpolator(OvershootInterpolator(2.4f)).start()
            } else {
                iconBox.animate().scaleX(1f).scaleY(1f).setDuration(140).start()
                icon.animate().scaleX(1f).scaleY(1f).setDuration(140).start()
            }
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.nav_fade_in,
                R.anim.nav_fade_out,
                R.anim.nav_fade_in,
                R.anim.nav_fade_out
            )
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_CURRENT_TAB, currentTab)
    }

    
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "已有通知权限,启动节气提醒")
                    setupSolarTermNotification()
                }
                else -> {
                    Log.d(TAG, "申请通知权限")
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        PERMISSION_REQUEST_CODE
                    )
                }
            }
        } else {
            setupSolarTermNotification()
        }
    }

    
    private fun setupSolarTermNotification() {
        SolarTermScheduler.scheduleSolarTermCheck(this)
        SolarTermScheduler.checkNow(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "通知权限已授予")
                    setupSolarTermNotification()
                } else {
                    Log.d(TAG, "通知权限被拒绝")
                    Toast.makeText(this, getString(R.string.toast_permission_notify), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}