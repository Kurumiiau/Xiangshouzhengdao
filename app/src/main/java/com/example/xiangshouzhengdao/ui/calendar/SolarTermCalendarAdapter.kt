package com.example.xiangshouzhengdao.ui.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.i18n.Loc

class SolarTermCalendarAdapter(
    private val solarTerms: List<SolarTermInfo>
) : RecyclerView.Adapter<SolarTermCalendarAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTermName: TextView = itemView.findViewById(R.id.tvTermName)
        val tvTermDate: TextView = itemView.findViewById(R.id.tvTermDate)
        val tvThreePeriods: TextView = itemView.findViewById(R.id.tvThreePeriods)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solar_term_calendar, parent, false)
        return ViewHolder(view)
    }

    // 日期由日历页统一算好，这里只负责展示，别再维护一套节气表。
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val term = solarTerms[position]
        holder.tvTermName.text = Loc.solarTerm(term.name)
        holder.tvTermDate.text = term.date
        holder.tvThreePeriods.text = Loc.solarThreePeriods(term.name)
        holder.tvDescription.text = Loc.solarDesc(term.name)
    }

    override fun getItemCount(): Int = solarTerms.size
}