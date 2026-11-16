package com.example.xiangshouzhengdao.utils

import java.time.LocalDate

object SolarTermUtil {

    
    private val SOLAR_TERMS = listOf(
        SolarTermData("小寒", 1, 5),
        SolarTermData("大寒", 1, 20),
        SolarTermData("立春", 2, 4),
        SolarTermData("雨水", 2, 19),
        SolarTermData("惊蛰", 3, 5),
        SolarTermData("春分", 3, 21),
        SolarTermData("清明", 4, 5),
        SolarTermData("谷雨", 4, 20),
        SolarTermData("立夏", 5, 5),
        SolarTermData("小满", 5, 21),
        SolarTermData("芒种", 6, 6),
        SolarTermData("夏至", 6, 21),
        SolarTermData("小暑", 7, 7),
        SolarTermData("大暑", 7, 23),
        SolarTermData("立秋", 8, 7),
        SolarTermData("处暑", 8, 23),
        SolarTermData("白露", 9, 7),
        SolarTermData("秋分", 9, 23),
        SolarTermData("寒露", 10, 8),
        SolarTermData("霜降", 10, 23),
        SolarTermData("立冬", 11, 7),
        SolarTermData("小雪", 11, 22),
        SolarTermData("大雪", 12, 7),
        SolarTermData("冬至", 12, 22)
    )

    
    fun getCurrentSolarTerm(): String = getSolarTerm(LocalDate.now())

    data class SolarTermDate(val name: String, val date: LocalDate)

    // 各页面共用同一份日期，年初要能向前找到上一年的冬至。
    fun getSolarTermsForYear(year: Int): List<SolarTermDate> = SOLAR_TERMS.map {
        SolarTermDate(it.name, LocalDate.of(year, it.month, it.day))
    }

    fun getSolarTerm(date: LocalDate): String =
        (getSolarTermsForYear(date.year - 1) + getSolarTermsForYear(date.year))
            .last { !it.date.isAfter(date) }.name

    fun getNextSolarTermDate(date: LocalDate = LocalDate.now()): SolarTermDate =
        (getSolarTermsForYear(date.year) + getSolarTermsForYear(date.year + 1))
            .first { it.date.isAfter(date) }

    
    fun getSolarTerm(month: Int, day: Int): String {
        return getSolarTerm(LocalDate.of(LocalDate.now().year, month, day))
    }

    
    fun getNextSolarTerm(): String {
        return getNextSolarTermDate().name
    }

    
    fun getCurrentSeason(): String {
        val currentTerm = getCurrentSolarTerm()
        return SOLAR_TERMS.find { it.name == currentTerm }?.season ?: "春"
    }

    
    fun getSolarTermTheme(solarTerm: String): String {
        return when (solarTerm) {
            "立春", "雨水", "惊蛰", "春分", "清明", "谷雨" -> "春疏肝"
            "立夏", "小满", "芒种", "夏至", "小暑", "大暑" -> "夏清心"
            "立秋", "处暑", "白露", "秋分", "寒露", "霜降" -> "秋润肺"
            "立冬", "小雪", "大雪", "冬至", "小寒", "大寒" -> "冬温肾"
            else -> "四季调和"
        }
    }

    
    fun getAllSolarTerms(): List<String> {
        return SOLAR_TERMS.map { it.name }
    }

    private data class SolarTermData(
        val name: String,
        val month: Int,
        val day: Int,
        val season: String = getSeasonByMonth(month)
    )

    private fun getSeasonByMonth(month: Int): String {
        return when (month) {
            2, 3, 4 -> "春"
            5, 6, 7 -> "夏"
            8, 9, 10 -> "秋"
            11, 12, 1 -> "冬"
            else -> "春"
        }
    }
}
