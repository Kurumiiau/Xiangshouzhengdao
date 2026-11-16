package com.example.xiangshouzhengdao.ui.solar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.model.SolarPrescription
import com.example.xiangshouzhengdao.i18n.Loc

class SolarPrescriptionAdapter(
    private val onItemClick: (SolarPrescription) -> Unit
) : ListAdapter<SolarPrescription, SolarPrescriptionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solar_prescription, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: (SolarPrescription) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvSolarTermName: TextView = itemView.findViewById(R.id.tvSolarTermName)
        private val tvTheme: TextView = itemView.findViewById(R.id.tvTheme)
        private val tvEffect: TextView = itemView.findViewById(R.id.tvEffect)
        private val tvJunHerbs: TextView = itemView.findViewById(R.id.tvJunHerbs)

        private var currentItem: SolarPrescription? = null

        init {
            itemView.setOnClickListener {
                currentItem?.let { onItemClick(it) }
            }
        }

        // 列表和详情都用同一条资产数据，别在适配器里再拼一个方子。
        fun bind(item: SolarPrescription) {
            currentItem = item
            tvSolarTermName.text = Loc.solarTerm(item.solarTerm)
            tvTheme.text = Loc.solarTheme(item.theme)
            tvEffect.text = Loc.solarEffect(item.solarTerm)
            tvJunHerbs.text = itemView.context.getString(R.string.lint_solar_herbs, item.junHerbs.joinToString("、"))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SolarPrescription>() {
        override fun areItemsTheSame(oldItem: SolarPrescription, newItem: SolarPrescription): Boolean {
            return oldItem.solarTerm == newItem.solarTerm
        }

        override fun areContentsTheSame(oldItem: SolarPrescription, newItem: SolarPrescription): Boolean {
            return oldItem == newItem
        }
    }
}