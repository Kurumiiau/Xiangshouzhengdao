package com.example.xiangshouzhengdao.utils

import android.content.Context
import android.location.Geocoder
import com.example.xiangshouzhengdao.i18n.Loc
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.Executors

object WeatherUtil {

    private const val TAG = "WeatherUtil"
    private const val API_URL = "https://api.open-meteo.com/v1/forecast"
    private val executor = Executors.newFixedThreadPool(2)

    data class WeatherInfo(
        val temperature: Double,    // 温度 °C
        val description: String,    // 天气描述
        val iconName: String,       // 图标名称（对应 drawable）
        val cityName: String        // 城市名
    )

    fun fetchWeatherByLocation(
        lat: Double,
        lon: Double,
        context: Context,
        callback: (WeatherInfo?) -> Unit
    ) {
        val appContext = context.applicationContext
        executor.execute {
            // 同步地理编码也可能访问网络，和天气请求一起放在共享后台线程池。
            var connection: HttpURLConnection? = null
            val weatherInfo = try {
                val cityName = getCityName(appContext, lat, lon)
                val url = URL(
                    "$API_URL?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code&timezone=auto"
                )
                val request = url.openConnection() as HttpURLConnection
                connection = request
                request.apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("Accept", "application/json")
                }

                val responseCode = request.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = request.inputStream.bufferedReader().use { it.readText() }
                    parseWeatherJson(response, cityName)
                } else {
                    Log.e(TAG, "HTTP error: $responseCode")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fetch weather failed: ${e.message}")
                null
            } finally {
                connection?.disconnect()
            }
            callback(weatherInfo)
        }
    }

    private fun getCityName(context: Context, lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Loc.dateLocale())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // 优先使用城市名，其次使用区县名
                address.locality ?: address.subAdminArea ?: address.adminArea ?: if (Loc.lang == "en") "Unknown" else "未知城市"
            } else {
                if (Loc.lang == "en") "Unknown" else "未知城市"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get city name failed: ${e.message}")
            if (Loc.lang == "en") "Unknown" else "未知城市"
        }
    }

    private fun parseWeatherJson(json: String, cityName: String): WeatherInfo? {
        return try {
            val root = JSONObject(json)
            val current = root.getJSONObject("current")
            val temperature = current.getDouble("temperature_2m")
            val weatherCode = current.getInt("weather_code")

            val (description, iconName) = getWeatherInfo(weatherCode)

            WeatherInfo(
                temperature = temperature,
                description = description,
                iconName = iconName,
                cityName = cityName
            )
        } catch (e: Exception) {
            Log.e(TAG, "Parse weather JSON failed: ${e.message}")
            null
        }
    }

    private fun getWeatherInfo(code: Int): Pair<String, String> {
        return when (code) {
            0 -> Loc.pick("Clear", "晴朗") to "weather_sunny"
            1, 2, 3 -> Loc.pick("Cloudy", "多云") to "weather_cloudy"
            45, 48 -> Loc.pick("Fog", "雾") to "weather_fog"
            51, 53, 55 -> Loc.pick("Drizzle", "毛毛雨") to "weather_drizzle"
            56, 57 -> Loc.pick("Freezing rain", "冻雨") to "weather_sleet"
            61, 63, 65 -> Loc.pick("Rain", "下雨") to "weather_rain"
            66, 67 -> Loc.pick("Sleet", "雨夹雪") to "weather_sleet"
            71, 73, 75 -> Loc.pick("Snow", "下雪") to "weather_snow"
            77 -> Loc.pick("Snow grains", "雪粒") to "weather_snow"
            80, 81, 82 -> Loc.pick("Showers", "阵雨") to "weather_rain"
            85, 86 -> Loc.pick("Snow showers", "阵雪") to "weather_snow"
            95 -> Loc.pick("Thunderstorm", "雷雨") to "weather_thunder"
            96, 99 -> Loc.pick("Thunderstorm", "雷暴") to "weather_thunder"
            else -> Loc.pick("Unknown", "未知") to "weather_unknown"
        }
    }
}
