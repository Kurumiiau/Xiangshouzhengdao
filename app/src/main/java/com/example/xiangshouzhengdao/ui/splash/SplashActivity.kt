package com.example.xiangshouzhengdao.ui.splash

import android.annotation.SuppressLint
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.net.toUri
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.local.DataRepository
import com.example.xiangshouzhengdao.i18n.Loc
import com.example.xiangshouzhengdao.ui.auth.AuthActivity
import com.example.xiangshouzhengdao.ui.main.MainActivity
import kotlinx.coroutines.launch

// 已接系统SplashScreen；本页还要展示定位授权。Lint仅按类名中的Splash匹配，无法识别这层路由。
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val LOCATION_TIMEOUT_MS = 8000L // 定位超时 8 秒
    }

    private var locationFetched = false
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private var navigationStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // 系统负责启动动画；本页只处理授权和路由，不再额外停一遍品牌页。
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // 数据库首开可能很慢，生命周期协程避免阻塞启动页，也能随页面销毁取消。
        lifecycleScope.launch {
            ensureDefaultUser()
            checkLocationPermission()
        }
    }

    private suspend fun ensureDefaultUser() {
        val repository = DataRepository.getInstance(this@SplashActivity)
        val userCount = repository.getUserCount()
        if (userCount == 0) {
            repository.register("admin", "123456", Loc.pick("Admin", "管理员"))
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun checkLocationPermission() {
        when {
            // 用户只允许“大致位置”也能查天气，不必强迫开启精确定位。
            hasLocationPermission() -> {
                requestRealtimeLocation()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.splash_loc_title))
                    .setMessage(getString(R.string.splash_loc_msg))
                    .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                        requestLocationPermission()
                    }
                    .setNegativeButton(getString(R.string.splash_skip)) { _, _ ->
                        saveLocation(39.9042, 116.4074)
                        navigateWhenReady()
                    }
                    .setCancelable(false)
                    .show()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestRealtimeLocation() {
        try {
            val lm = locationManager ?: run {
                saveLocation(39.9042, 116.4074)
                navigateWhenReady()
                return
            }

            val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                Toast.makeText(this, getString(R.string.splash_loc_off), Toast.LENGTH_SHORT).show()
                saveLocation(39.9042, 116.4074)
                navigateWhenReady()
                return
            }

            // 设置超时：如果 8 秒内没获取到位置，使用默认坐标
            timeoutRunnable = Runnable {
                if (!locationFetched) {
                    removeLocationUpdates()
                    Toast.makeText(this, getString(R.string.splash_loc_timeout), Toast.LENGTH_SHORT).show()
                    saveLocation(39.9042, 116.4074)
                    navigateWhenReady()
                }
            }
            handler.postDelayed(timeoutRunnable!!, LOCATION_TIMEOUT_MS)

            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (!locationFetched) {
                        locationFetched = true
                        removeLocationUpdates()
                        timeoutRunnable?.let { handler.removeCallbacks(it) }
                        saveLocation(location.latitude, location.longitude)
                        navigateWhenReady()
                    }
                }

                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            if (hasLocationPermission()) {
                val lastGps = if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && isGpsEnabled) lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null
                val lastNetwork = if (isNetworkEnabled) lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) else null

                val validLocation = getNewestValidLocation(lastGps, lastNetwork)
                if (validLocation != null) {
                    locationFetched = true
                    removeLocationUpdates()
                    timeoutRunnable?.let { handler.removeCallbacks(it) }
                    saveLocation(validLocation.latitude, validLocation.longitude)
                    navigateWhenReady()
                    return
                }
            }

            if (isGpsEnabled && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,  // 最小时间间隔 1 秒
                    10f,    // 最小距离 10 米
                    locationListener!!,
                    Looper.getMainLooper()
                )
            }

            if (isNetworkEnabled && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    10f,
                    locationListener!!,
                    Looper.getMainLooper()
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            if (!locationFetched) {
                saveLocation(39.9042, 116.4074)
                navigateWhenReady()
            }
        }
    }

    private fun getNewestValidLocation(gps: Location?, network: Location?): Location? {
        val now = System.currentTimeMillis()
        val maxAge = 5 * 60 * 1000L // 5 分钟

        val validGps = gps?.takeIf { now - it.time < maxAge }
        val validNetwork = network?.takeIf { now - it.time < maxAge }

        return when {
            validGps != null && validNetwork != null -> {
                if (validGps.time > validNetwork.time) validGps else validNetwork
            }
            validGps != null -> validGps
            validNetwork != null -> validNetwork
            else -> null
        }
    }

    private fun removeLocationUpdates() {
        try {
            locationListener?.let { listener ->
                locationManager?.removeUpdates(listener)
            }
            locationListener = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveLocation(lat: Double, lon: Double) {
        val prefs = getSharedPreferences("location_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putFloat("latitude", lat.toFloat())
            putFloat("longitude", lon.toFloat())
            apply()
        }
        locationFetched = true
    }

    private fun navigateWhenReady() {
        if (navigationStarted || isFinishing || isDestroyed) return
        navigationStarted = true
        timeoutRunnable?.let { handler.removeCallbacks(it) }
        removeLocationUpdates()

        lifecycleScope.launch {
            val isLoggedIn = DataRepository.getInstance(this@SplashActivity).isLoggedIn()
            if (isFinishing || isDestroyed) return@launch
            val intent = if (isLoggedIn) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, AuthActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (hasLocationPermission()) {
                    requestRealtimeLocation()
                } else {
                    // 判断用户是否选择了"拒绝且不再询问"
                    val permanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    if (permanentlyDenied) {
                        AlertDialog.Builder(this)
                            .setTitle(getString(R.string.splash_loc_denied_forever_title))
                            .setMessage(getString(R.string.splash_loc_denied_forever_msg))
                            .setPositiveButton(getString(R.string.splash_goto_settings)) { _, _ ->
                                val intent = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS.let {
                                    android.content.Intent(it, "package:$packageName".toUri())
                                }
                                startActivity(intent)
                                saveLocation(39.9042, 116.4074)
                                navigateWhenReady()
                            }
                            .setNegativeButton(getString(R.string.splash_skip)) { _, _ ->
                                saveLocation(39.9042, 116.4074)
                                navigateWhenReady()
                            }
                            .setCancelable(false)
                            .show()
                    } else {
                        AlertDialog.Builder(this)
                            .setTitle(getString(R.string.splash_loc_denied_title))
                            .setMessage(getString(R.string.splash_loc_denied_msg))
                            .setPositiveButton(getString(R.string.splash_reapply)) { _, _ ->
                                requestLocationPermission()
                            }
                            .setNegativeButton(getString(R.string.splash_skip)) { _, _ ->
                                saveLocation(39.9042, 116.4074)
                                navigateWhenReady()
                            }
                            .setCancelable(false)
                            .show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        removeLocationUpdates()
        locationManager = null
    }
}
