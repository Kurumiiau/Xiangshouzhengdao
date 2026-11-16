package com.example.xiangshouzhengdao.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.model.UserRecord
import com.example.xiangshouzhengdao.i18n.Loc
import com.example.xiangshouzhengdao.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (UserRecord) -> Unit,
    private val onDeleteClick: ((UserRecord) -> Unit)? = null
) : ListAdapter<UserRecord, HistoryAdapter.ViewHolder>(DiffCallback()) {

    var isManageMode: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            // 切换管理状态只改变现有行的按钮和点击事件，列表内容和数量都没有变化。
            notifyItemRangeChanged(0, itemCount)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), isManageMode)
    }

    inner class ViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // 旧记录的mainTypes不一定排过序，展示时还要自己找一次最高分。
        fun bind(record: UserRecord, manageMode: Boolean) {
            val date = Date(record.testDate)
            val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
            val monthFormat = SimpleDateFormat(Loc.monthPattern(), Loc.dateLocale())

            binding.tvDay.text = dayFormat.format(date)
            binding.tvMonth.text = monthFormat.format(date)

            val mainTypeName = record.mainTypes.mapNotNull { record.scores[it] }
                .maxByOrNull { it.transformScore }?.let { Loc.bodyName(it.bodyTypeId) } ?: record.bodyType
            binding.tvBodyType.text = mainTypeName

            binding.tvSolarTerm.text = binding.root.context.getString(
                R.string.lint_engine_history_term, Loc.solarTerm(record.solarTerm), Loc.recordTag()
            )

            if (manageMode) {
                binding.tvArrow.visibility = View.GONE
                binding.tvDelete.visibility = View.VISIBLE

                binding.tvDelete.setOnClickListener {
                    onDeleteClick?.invoke(record)
                }

                binding.root.setOnClickListener(null)
            } else {
                binding.tvArrow.visibility = View.VISIBLE
                binding.tvDelete.visibility = View.GONE

                binding.root.setOnClickListener {
                    onItemClick(record)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<UserRecord>() {
        override fun areItemsTheSame(oldItem: UserRecord, newItem: UserRecord): Boolean {
            return oldItem.recordId == newItem.recordId
        }

        override fun areContentsTheSame(oldItem: UserRecord, newItem: UserRecord): Boolean {
            return oldItem == newItem
        }
    }
}
