package com.example.xiangshouzhengdao

import com.example.xiangshouzhengdao.utils.SolarTermUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class SolarTermUtilTest {
    // 覆盖上一年冬至和下一年小寒，避免日历重新出现负数倒计时。
    @Test
    fun januaryFindsPreviousWinterSolstice() {
        val date = LocalDate.of(2026, 1, 1)
        assertEquals("冬至", SolarTermUtil.getSolarTerm(date))
        assertEquals(LocalDate.of(2026, 1, 5), SolarTermUtil.getNextSolarTermDate(date).date)
        assertEquals("小寒", SolarTermUtil.getSolarTerm(LocalDate.of(2026, 1, 10)))
    }

    @Test
    fun decemberLooksIntoTheNextYear() {
        val date = LocalDate.of(2026, 12, 25)
        val next = SolarTermUtil.getNextSolarTermDate(date)
        assertEquals("冬至", SolarTermUtil.getSolarTerm(date))
        assertEquals("小寒", next.name)
        assertEquals(LocalDate.of(2027, 1, 5), next.date)
        assertEquals(11L, ChronoUnit.DAYS.between(date, next.date))
    }

    @Test
    fun countdownUsesWholeCalendarDates() {
        val date = LocalDate.of(2026, 11, 16)
        assertEquals(6L, ChronoUnit.DAYS.between(date, SolarTermUtil.getNextSolarTermDate(date).date))
        assertEquals("小雪", SolarTermUtil.getSolarTerm(LocalDate.of(2026, 11, 22)))
    }

    @Test
    fun annualDatesAreSortedAndNextTermAlwaysIsInTheFuture() {
        val annual = SolarTermUtil.getSolarTermsForYear(2024)
        assertEquals(24, annual.size)
        assertTrue(annual.zipWithNext().all { (a, b) -> a.date.isBefore(b.date) })
        var date = LocalDate.of(2024, 1, 1)
        repeat(366) {
            assertTrue(SolarTermUtil.getNextSolarTermDate(date).date.isAfter(date))
            date = date.plusDays(1)
        }
    }
}
