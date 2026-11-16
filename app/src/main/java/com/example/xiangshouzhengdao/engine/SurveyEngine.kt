package com.example.xiangshouzhengdao.engine

import com.example.xiangshouzhengdao.data.model.*

// 这里只管题目导航和原始答案。题号跨语言保持一致，反向计分交给 ScoreEngine，恢复时不要提前换分。
class SurveyEngine(
    private val allQuestions: List<Question>,
    private val mode: SurveyMode = SurveyMode.FULL
) {
    enum class SurveyMode { QUICK, FULL }

    var currentIndex: Int = 0
        private set

    // 答案记录 {题号: 原始分值}
    val answers: MutableMap<Int, Int> = mutableMapOf()

    val surveyMode: SurveyMode = mode

    val questions: List<Question> = when (surveyMode) {
        SurveyMode.QUICK -> {
            // 极速简版：使用独立的12题问卷
            allQuestions.filter { it.id in 1..12 }
        }
        SurveyMode.FULL -> allQuestions
    }

    val totalQuestions: Int = questions.size

    fun answer(score: Int): NavigationResult {
        val currentQuestion = questions[currentIndex]
        answers[currentQuestion.id] = score

        return if (currentIndex < totalQuestions - 1) {
            currentIndex++
            NavigationResult.Next(currentIndex)
        } else {
            NavigationResult.Complete(checkAllAnswered())
        }
    }

    fun goBack(): Boolean {
        if (currentIndex > 0) {
            currentIndex--
            return true
        }
        return false
    }

    fun jumpTo(index: Int): Boolean {
        if (index in 0 until totalQuestions) {
            currentIndex = index
            return true
        }
        return false
    }

    fun getCurrentQuestion(): QuestionState {
        val q = questions[currentIndex]
        return QuestionState(
            question = q,
            index = currentIndex,
            total = totalQuestions,
            progress = ((currentIndex + 1) * 100 / totalQuestions),
            hasAnswer = answers.containsKey(q.id),
            selectedValue = answers[q.id]
        )
    }

    fun checkAllAnswered(): Boolean {
        return questions.all { answers.containsKey(it.id) }
    }

    fun getUnanswered(): List<Int> {
        return questions
            .filter { !answers.containsKey(it.id) }
            .map { it.id }
    }

    fun getAnsweredCount(): Int = answers.size

    fun reset() {
        currentIndex = 0
        answers.clear()
    }

    fun restore(savedAnswers: Map<Int, Int>, savedIndex: Int) {
        answers.clear()
        answers.putAll(savedAnswers)
        currentIndex = savedIndex.coerceIn(0, totalQuestions - 1)
    }

    sealed class NavigationResult {
        data class Next(val newIndex: Int) : NavigationResult()
        data class Complete(val ready: Boolean) : NavigationResult()
    }

    data class QuestionState(
        val question: Question,
        val index: Int,
        val total: Int,
        val progress: Int,
        val hasAnswer: Boolean,
        val selectedValue: Int?
    )
}
