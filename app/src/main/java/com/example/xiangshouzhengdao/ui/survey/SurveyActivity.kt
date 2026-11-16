package com.example.xiangshouzhengdao.ui.survey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.local.DataRepository
import com.example.xiangshouzhengdao.data.model.Option
import com.example.xiangshouzhengdao.data.model.Question
import com.example.xiangshouzhengdao.databinding.ActivitySurveyBinding
import com.example.xiangshouzhengdao.engine.ScoreEngine
import com.example.xiangshouzhengdao.engine.SurveyEngine
import com.example.xiangshouzhengdao.i18n.Loc
import com.example.xiangshouzhengdao.ui.result.ResultActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class SurveyActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_CONSTRAINTS = "extra_constraints"
        const val MODE_QUICK = "quick"
        const val MODE_FULL = "full"

        private const val PREFS_SURVEY = "survey_progress"
        private const val PREFS_CONSTRAINTS = "user_constraints"
        private const val KEY_ANSWERS = "answers"
        private const val KEY_INDEX = "current_index"
        private const val KEY_MODE = "survey_mode"
        private const val KEY_CONSTRAINTS = "constraints"

        val CONSTRAINT_OPTIONS = listOf(
            "血瘀质/孕妇" to "血瘀质、孕妇",
            "阴虚火旺" to "阴虚火旺",
            "湿热质" to "湿热质",
            "特禀重度过敏" to "特禀重度过敏"
        )
    }

    private lateinit var binding: ActivitySurveyBinding
    private var surveyEngine: SurveyEngine? = null
    private var scoreEngine: ScoreEngine? = null
    private var questions: List<Question> = emptyList()
    private var currentMode: String = MODE_FULL
    private var userConstraints: List<String> = emptyList()
    private var currentUserId = DataRepository.GUEST_USER_ID
    private var progressPrefsName: String? = null
    private var submitting = false

    private val optionButtons by lazy {
        listOf(binding.btnOptionA, binding.btnOptionB, binding.btnOptionC,
               binding.btnOptionD, binding.btnOptionE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentMode = intent.getStringExtra(EXTRA_MODE) ?: MODE_FULL

        initViews()
        showLoading(true)
        lifecycleScope.launch {
            try {
                currentUserId = DataRepository.getInstance(this@SurveyActivity).getCurrentUserId()
                val suffix = if (Loc.lang == "en") "_en" else ""
                val file = if (currentMode == MODE_QUICK) "questions_quick$suffix.json" else "questions$suffix.json"
                val bytes = assets.open(file).use { it.readBytes() }
                val version = MessageDigest.getInstance("SHA-256").digest(bytes)
                    .joinToString("") { "%02x".format(it) }.take(16)
                // 题库版本、用户、语言、模式共同隔离缓存；旧题号和旧计分方向不能复用。
                progressPrefsName = "${PREFS_SURVEY}_v2_${currentUserId}_${Loc.lang}_${currentMode}_$version"
                userConstraints = getSharedPreferences("${PREFS_CONSTRAINTS}_$currentUserId", MODE_PRIVATE)
                    .getStringSet(KEY_CONSTRAINTS, emptySet())?.toList().orEmpty()
                when {
                    hasSavedProgress() && savedInstanceState != null -> initData(currentMode, restoreProgress = true)
                    hasSavedProgress() -> showResumeDialog()
                    else -> showConstraintDialog { initData(currentMode) }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Toast.makeText(this@SurveyActivity, getString(R.string.survey_load_fail), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun showConstraintDialog(onConfirmed: () -> Unit) {
        // 每次重新开始可修改声明；“继续测评”则使用已加载的同一用户声明。
        showConstraintDialogInternal(currentUserId, onConfirmed)
    }

    private fun showConstraintDialogInternal(userId: String, onConfirmed: () -> Unit) {
        val selectedConstraints = userConstraints.toMutableList()

        val dialog = AlertDialog.Builder(this@SurveyActivity)
            .setTitle(getString(R.string.survey_constraint_title))
            .setMessage(getString(R.string.survey_constraint_msg))
            .setPositiveButton(getString(R.string.survey_constraint_ok), null) // 先设null，后面手动处理
            .setNegativeButton(getString(R.string.survey_constraint_skip)) { _, _ ->
                userConstraints = emptyList()
                val prefsName = "${PREFS_CONSTRAINTS}_$userId"
                getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                    .edit { putStringSet(KEY_CONSTRAINTS, emptySet()) }
                onConfirmed()
            }
            .setCancelable(false)
            .create()

        val scrollView = android.widget.ScrollView(this@SurveyActivity)
        val layout = android.widget.LinearLayout(this@SurveyActivity).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        CONSTRAINT_OPTIONS.forEachIndexed { index, option ->
            val checkBox = android.widget.CheckBox(this@SurveyActivity).apply {
                text = Loc.constraint(option.second)
                isChecked = option.first in selectedConstraints
                textSize = 15f
                setPadding(16, 24, 16, 24)
                setOnCheckedChangeListener { _, isChecked ->
                    val key = option.first
                    if (isChecked) {
                        if (!selectedConstraints.contains(key)) {
                            selectedConstraints.add(key)
                        }
                    } else {
                        selectedConstraints.remove(key)
                    }
                }
            }
            layout.addView(checkBox)

            if (index < CONSTRAINT_OPTIONS.size - 1) {
                val divider = android.view.View(this@SurveyActivity).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                    )
                    setBackgroundColor("#EEEEEE".toColorInt())
                }
                layout.addView(divider)
            }
        }

        scrollView.addView(layout)
        dialog.setView(scrollView)

        // 手动设置 PositiveButton 点击事件，确保可以读取到选中状态
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                userConstraints = selectedConstraints.toList()
                val prefsName = "${PREFS_CONSTRAINTS}_$userId"
                getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                    .edit { putStringSet(KEY_CONSTRAINTS, selectedConstraints.toSet()) }
                dialog.dismiss()
                onConfirmed()
            }
        }

        dialog.show()
    }

    private fun showResumeDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.survey_resume_title))
            .setMessage(getString(R.string.survey_resume_msg))
            .setPositiveButton(getString(R.string.survey_resume_yes)) { _, _ ->
                initData(currentMode, restoreProgress = true)
            }
            .setNegativeButton(getString(R.string.survey_resume_no)) { _, _ ->
                clearSavedProgress()
                showConstraintDialog {
                    initData(currentMode, restoreProgress = false)
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun initData(mode: String, restoreProgress: Boolean = false) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val repository = DataRepository.getInstance(this@SurveyActivity)
                check(repository.initializeDatabase()) { "Database initialization failed" }

                val langSuffix = if (Loc.lang == "en") "_en" else ""
                val jsonFile = if (mode == MODE_QUICK) "questions_quick$langSuffix.json" else "questions$langSuffix.json"
                val json = assets.open(jsonFile).bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<Question>>() {}.type
                questions = Gson().fromJson(json, type) ?: emptyList()

                val surveyMode = if (mode == MODE_QUICK) {
                    SurveyEngine.SurveyMode.QUICK
                } else {
                    SurveyEngine.SurveyMode.FULL
                }

                surveyEngine = SurveyEngine(questions, surveyMode)
                scoreEngine = ScoreEngine(surveyEngine!!.questions, isQuickMode = (mode == MODE_QUICK))

                if (restoreProgress) {
                    restoreProgress()
                }

                showLoading(false)
                displayCurrentQuestion()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                e.printStackTrace()
                Toast.makeText(
                    this@SurveyActivity,
                    getString(R.string.survey_load_fail) + e.message,
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun initViews() {
        optionButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                onOptionSelected(index + 1)
            }
        }

        binding.btnPrev.setOnClickListener {
            surveyEngine?.let { engine ->
                if (engine.goBack()) {
                    displayCurrentQuestion()
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            submitSurvey()
        }
    }

    private fun showLoading(show: Boolean) {
        // 加载结束后这条横条还要显示答题进度，不能跟随加载状态一起隐藏。
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.isIndeterminate = show
        val enabled = !show
        optionButtons.forEach { it.isEnabled = enabled }
        binding.btnPrev.isEnabled = enabled
        binding.btnSubmit.isEnabled = enabled
    }

    private fun displayCurrentQuestion() {
        val engine = surveyEngine ?: return

        val state = engine.getCurrentQuestion()
        val question = state.question

        binding.tvQuestionNumber.text = getString(R.string.survey_question_no, state.index + 1, state.total)
        binding.progressBar.progress = state.progress

        binding.tvBodyTypeBadge.text = Loc.bodyName(question.bodyType)
        binding.tvBodyTypeBadge.setBackgroundColor(getBodyTypeColor(question.bodyType))

        binding.tvQuestionText.text = question.text

        optionButtons.forEachIndexed { index, button ->
            val option = Option.ALL[index]
            button.text = getString(R.string.lint_engine_option_label, option.label, getString(optTextRes(option.value)))

            if (state.selectedValue == option.value) {
                button.setBackgroundColor(getColor(R.color.selected))
                button.setStrokeColorResource(R.color.primary)
            } else {
                button.setBackgroundColor(getColor(R.color.bg_card))
                button.setStrokeColorResource(R.color.divider)
            }
        }

        binding.btnPrev.isEnabled = state.index > 0
    }

    private fun optTextRes(value: Int): Int = when (value) {
        1 -> R.string.opt_1
        2 -> R.string.opt_2
        3 -> R.string.opt_3
        4 -> R.string.opt_4
        else -> R.string.opt_5
    }

    private fun onOptionSelected(score: Int) {
        val engine = surveyEngine ?: return

        val result = engine.answer(score)
        saveProgress()

        when (result) {
            is SurveyEngine.NavigationResult.Next -> {
                displayCurrentQuestion()
            }
            is SurveyEngine.NavigationResult.Complete -> {
                if (result.ready) {
                    submitSurvey()
                }
            }
        }
    }

    private fun submitSurvey() {
        val engine = surveyEngine
        val scorer = scoreEngine

        if (engine == null || scorer == null) {
            Toast.makeText(this, getString(R.string.survey_loading), Toast.LENGTH_SHORT).show()
            return
        }

        if (!engine.checkAllAnswered()) {
            val unanswered = engine.getUnanswered()
            if (unanswered.isNotEmpty()) {
                val firstUnansweredId = unanswered.first()
                val targetIndex = engine.questions.indexOfFirst { it.id == firstUnansweredId }
                if (targetIndex >= 0) {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.survey_incomplete_title))
                        .setMessage(getString(R.string.survey_incomplete_msg, targetIndex + 1))
                        .setPositiveButton(getString(R.string.survey_goto)) { _, _ ->
                            engine.jumpTo(targetIndex)
                            displayCurrentQuestion()
                        }
                        .setCancelable(false)
                        .show()
                }
            }
            return
        }

        if (submitting) return
        submitting = true
        clearSavedProgress()

        val bodyResult = scorer.calculate(engine.answers)
        val bodyResultJson = Gson().toJson(bodyResult)

        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_BODY_RESULT, bodyResultJson)
            putExtra(ResultActivity.EXTRA_MODE, currentMode)
            putExtra(ResultActivity.EXTRA_ANSWERS, Gson().toJson(engine.answers))
            putExtra(ResultActivity.EXTRA_RECORD_ID, UUID.randomUUID().toString())
            putExtra(ResultActivity.EXTRA_TEST_DATE, System.currentTimeMillis())
            putExtra(ResultActivity.EXTRA_CONSTRAINTS, Gson().toJson(userConstraints))
        }
        startActivity(intent)
        finish()
    }

    private fun saveProgress() {
        val engine = surveyEngine ?: return
        val prefs = getSharedPreferences(progressPrefsName ?: return, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_ANSWERS, Gson().toJson(engine.answers))
            putInt(KEY_INDEX, engine.currentIndex)
            putString(KEY_MODE, currentMode)
        }
    }

    private fun restoreProgress() {
        val engine = surveyEngine ?: return
        val prefs = getSharedPreferences(progressPrefsName ?: return, Context.MODE_PRIVATE)

        val answersJson = prefs.getString(KEY_ANSWERS, null) ?: return
        val savedIndex = prefs.getInt(KEY_INDEX, 0)

        val type = object : TypeToken<Map<String, Int>>() {}.type
        val savedAnswers: Map<String, Int> = Gson().fromJson(answersJson, type) ?: emptyMap()

        val answers = savedAnswers.mapKeys { it.key.toInt() }.toMutableMap()
        engine.restore(answers, savedIndex)
    }

    private fun hasSavedProgress(): Boolean {
        val prefs = getSharedPreferences(progressPrefsName ?: return false, Context.MODE_PRIVATE)
        val savedMode = prefs.getString(KEY_MODE, null)
        return savedMode == currentMode && prefs.contains(KEY_ANSWERS)
    }

    private fun clearSavedProgress() {
        val prefs = getSharedPreferences(progressPrefsName ?: return, Context.MODE_PRIVATE)
        prefs.edit { clear() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (!submitting) saveProgress()
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Activity 销毁时自动保存进度（如果还没完成且至少答了一题）
        surveyEngine?.let { engine ->
            if (!engine.checkAllAnswered() && engine.answers.isNotEmpty()) {
                saveProgress()
            }
        }
    }

    private fun getBodyTypeColor(bodyTypeId: String): Int {
        return when (bodyTypeId) {
            "pinghe" -> getColor(R.color.pinghe)
            "qixu" -> getColor(R.color.qixu)
            "yangxu" -> getColor(R.color.yangxu)
            "yinxu" -> getColor(R.color.yinxu)
            "tanshi" -> getColor(R.color.tanshi)
            "shire" -> getColor(R.color.shire)
            "xueyu" -> getColor(R.color.xueyu)
            "qiyu" -> getColor(R.color.qiyu)
            "tebing" -> getColor(R.color.tebing)
            else -> getColor(R.color.primary)
        }
    }
}
