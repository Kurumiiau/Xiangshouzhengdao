package com.example.xiangshouzhengdao.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.i18n.Loc
import com.example.xiangshouzhengdao.data.local.DataRepository
import com.example.xiangshouzhengdao.data.local.toModel
import com.example.xiangshouzhengdao.data.model.BodyResult
import com.example.xiangshouzhengdao.databinding.ActivityHistoryBinding
import com.example.xiangshouzhengdao.ui.result.ResultActivity
import com.example.xiangshouzhengdao.ui.survey.SurveyActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter
    private var isManageMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        loadRecords()
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = HistoryAdapter(
            onItemClick = { record ->
                // 节气可动态更新，用户声明和原答卷必须跟着记录传递。
                val bodyResultJson = record.bodyResultJson
                val intent = if (!bodyResultJson.isNullOrBlank()) {
                    Intent(this, ResultActivity::class.java).apply {
                        putExtra(ResultActivity.EXTRA_BODY_RESULT, bodyResultJson)
                        putExtra(ResultActivity.EXTRA_MODE, record.mode)
                        putExtra(ResultActivity.EXTRA_FROM_HISTORY, true)
                            putExtra(ResultActivity.EXTRA_RECORD_ID, record.recordId)
                            putExtra(ResultActivity.EXTRA_TEST_DATE, record.testDate)
                            putExtra(ResultActivity.EXTRA_ANSWERS, Gson().toJson(record.answers))
                            putExtra(ResultActivity.EXTRA_CONSTRAINTS, Gson().toJson(record.constraints))
                    }
                } else {
                    // 兼容旧数据：重新构建BodyResult
                    val bodyResult = BodyResult(
                        type = BodyResult.ResultType.MILD,
                        title = record.bodyType,
                        description = Loc.pick("Past assessment record", "历史测评记录"),
                        scores = record.scores,
                        mainTypes = record.mainTypes.mapNotNull { record.scores[it] },
                        mildTypes = emptyList(),
                        recommendation = ""
                    )
                    Intent(this, ResultActivity::class.java).apply {
                        putExtra(ResultActivity.EXTRA_BODY_RESULT, Gson().toJson(bodyResult))
                        putExtra(ResultActivity.EXTRA_MODE, record.mode)
                        putExtra(ResultActivity.EXTRA_FROM_HISTORY, true)
                            putExtra(ResultActivity.EXTRA_RECORD_ID, record.recordId)
                            putExtra(ResultActivity.EXTRA_TEST_DATE, record.testDate)
                            putExtra(ResultActivity.EXTRA_ANSWERS, Gson().toJson(record.answers))
                            putExtra(ResultActivity.EXTRA_CONSTRAINTS, Gson().toJson(record.constraints))
                    }
                }
                startActivity(intent)
            },
            onDeleteClick = { record ->
                showDeleteConfirmDialog(record)
            }
        )

        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = adapter

        binding.btnGoSurvey.setOnClickListener {
            startActivity(Intent(this, SurveyActivity::class.java))
        }

        binding.tvManage.setOnClickListener {
            toggleManageMode()
        }
    }

    private fun toggleManageMode() {
        lifecycleScope.launch {
            val repository = DataRepository.getInstance(this@HistoryActivity)
            val records = repository.getCurrentUserRecords()

            if (records.isEmpty()) {
                Toast.makeText(this@HistoryActivity, getString(R.string.history_empty), Toast.LENGTH_SHORT).show()
                return@launch
            }

            isManageMode = !isManageMode
            adapter.isManageMode = isManageMode
            binding.tvManage.text = if (isManageMode) getString(R.string.history_done) else getString(R.string.history_manage)
        }
    }

    private fun showDeleteConfirmDialog(record: com.example.xiangshouzhengdao.data.model.UserRecord) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.history_delete_title))
            .setMessage(getString(R.string.history_delete_msg))
            .setPositiveButton(getString(R.string.history_delete)) { _, _ ->
                lifecycleScope.launch {
                    val repository = DataRepository.getInstance(this@HistoryActivity)
                    repository.deleteUserRecord(record.recordId)
                    Toast.makeText(this@HistoryActivity, getString(R.string.history_deleted), Toast.LENGTH_SHORT).show()
                    loadRecords()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun loadRecords() {
        lifecycleScope.launch {
            val repository = DataRepository.getInstance(this@HistoryActivity)
            // 获取当前登录用户的记录，实现数据隔离
            val records = repository.getCurrentUserRecords()
                .map { it.toModel() }
                .sortedByDescending { it.testDate }

            if (records.isEmpty()) {
                binding.recyclerHistory.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
                binding.tvManage.visibility = View.GONE
                if (isManageMode) {
                    isManageMode = false
                    adapter.isManageMode = false
                    binding.tvManage.text = getString(R.string.history_manage)
                }
            } else {
                binding.recyclerHistory.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
                binding.tvManage.visibility = View.VISIBLE
                adapter.submitList(records)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }
}