package com.example.xiangshouzhengdao.ui.calendar

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.i18n.Loc
import com.example.xiangshouzhengdao.utils.SolarTermUtil
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class SolarCalendarActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvCurrentTerm: TextView
    private lateinit var tvNextTerm: TextView
    private lateinit var tvDaysUntil: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solar_calendar)

        initViews()
        loadSolarTermData()
    }

    private fun initViews() {
        findViewById<View>(R.id.toolbar)?.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerView)
        tvCurrentTerm = findViewById(R.id.tvCurrentTerm)
        tvNextTerm = findViewById(R.id.tvNextTerm)
        tvDaysUntil = findViewById(R.id.tvDaysUntil)

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadSolarTermData() {
        val today = LocalDate.now()
        val solarTerms = SolarTermUtil.getSolarTermsForYear(today.year)
            .map { SolarTermInfo(it.name, it.date.toString()) }
        val adapter = SolarTermCalendarAdapter(solarTerms)
        recyclerView.adapter = adapter

        val currentTerm = SolarTermUtil.getSolarTerm(today)
        val nextTerm = SolarTermUtil.getNextSolarTermDate(today)

        tvCurrentTerm.text = getString(R.string.lint_calendar_current, Loc.solarTerm(currentTerm))
        tvNextTerm.text = getString(R.string.lint_calendar_next, Loc.solarTerm(nextTerm.name))
        // 比较日历日期，不让当前几点钟影响“还有几天”。
        val daysUntil = ChronoUnit.DAYS.between(today, nextTerm.date).toInt()
        tvDaysUntil.text = resources.getQuantityString(R.plurals.lint_calendar_days, daysUntil, daysUntil)
    }
}

data class SolarTermInfo(
    val name: String,
    val date: String
)
