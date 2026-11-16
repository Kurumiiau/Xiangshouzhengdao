package com.example.xiangshouzhengdao.engine

import com.example.xiangshouzhengdao.data.model.*
import com.example.xiangshouzhengdao.i18n.Loc

class ScoreEngine(
    private val questions: List<Question>,
    private val isQuickMode: Boolean = false
) {

    // 题号只负责识别题目，归属以题库里的 bodyType 为准；中英文顺序可以不同。

    
    fun calculate(answers: Map<Int, Int>): BodyResult {
        // 1. 计算每种体质的原始分与转化分
        val scores = mutableMapOf<String, BodyScore>()

        BodyTypeConfig.ALL.forEach { (key, config) ->
            // 按题目自身的 bodyType 分组(数据资产中每道题都带有正确的分组与反向标记)
            val bodyQuestions = questions.filter { it.bodyType == key }
            var rawSum = 0
            var answeredCount = 0

            bodyQuestions.forEach { q ->
                val rawScore = answers[q.id]
                if (rawScore != null) {
                    answeredCount++
                    // 只有负向题才反向计分。现有平和题描述健康状态，题库已改为正向，健康答案应得高分。
                    val finalScore = if (q.reverse) 6 - rawScore else rawScore
                    rawSum += finalScore
                }
            }

            val count = bodyQuestions.size
            // 该模式下不存在此体质题目时使用中性分(仅理论兜底,正常答题不会触发)
            val transformScore = if (count == 0 || answeredCount == 0) {
                50.0
            } else {
                // 国标转化分公式,简版与完整版统一
                ((rawSum - count).toDouble() / (count * 4)) * 100
            }

            scores[key] = BodyScore(
                bodyTypeId = key,
                bodyName = config.name,
                color = config.color,
                rawScore = rawSum,
                transformScore = transformScore.coerceIn(0.0, 100.0),
                count = count
            )
        }

        // 2. 体质判定
        return judgeBodyType(scores)
    }

    
    private fun judgeBodyType(scores: Map<String, BodyScore>): BodyResult {
        val pinghe = scores["pinghe"] ?: return createDefaultResult(scores)
        val others = scores.filter { it.key != "pinghe" }.values.toList()

        // T≥40 为典型偏颇(主体质);30≤T<40 为轻度偏颇
        // 列表首项会被历史页用作主要体质，必须按得分排列，不能沿用九型配置顺序。
        val mainTypes = others.filter { it.transformScore >= 40 }.sortedByDescending { it.transformScore }
        val mildTypes = others.filter { it.transformScore >= 30 && it.transformScore < 40 }
            .sortedByDescending { it.transformScore }

        return when {
            // 场景1:纯平和质(平和 T≥60 且其余均 <30)
            pinghe.transformScore >= 60 && others.all { it.transformScore < 30 } -> {
                BodyResult(
                    type = BodyResult.ResultType.PURE_PINGHE,
                    title = Loc.pureTitle(),
                    description = Loc.pureDesc(),
                    scores = scores,
                    mainTypes = emptyList(),
                    mildTypes = emptyList(),
                    recommendation = Loc.pureRec(),
                    troubles = Loc.pureTroubles()
                )
            }

            // 场景2:单一典型体质
            mainTypes.size == 1 -> {
                val t = mainTypes[0]
                val tName = Loc.bodyName(t.bodyTypeId)
                BodyResult(
                    type = BodyResult.ResultType.SINGLE,
                    title = tName,
                    description = Loc.singleDesc(tName, Loc.bodyDesc(t.bodyTypeId)),
                    scores = scores,
                    mainTypes = mainTypes,
                    mildTypes = mildTypes,
                    recommendation = Loc.singleRec(tName, Loc.bodyAdvice(t.bodyTypeId)),
                    troubles = Loc.bodyDesc(t.bodyTypeId)
                )
            }

            // 场景3:复合体质(两项及以上 T≥40)
            mainTypes.size >= 2 -> {
                val names = mainTypes.joinToString(" + ") { Loc.bodyName(it.bodyTypeId) }
                val troublesText = mainTypes.joinToString(";") { Loc.bodyDesc(it.bodyTypeId) }
                BodyResult(
                    type = BodyResult.ResultType.COMPOSITE,
                    title = Loc.compositeTitle(names),
                    description = Loc.compositeDesc(names),
                    scores = scores,
                    mainTypes = mainTypes,
                    mildTypes = mildTypes,
                    recommendation = Loc.compositeRec(),
                    troubles = troublesText
                )
            }

            // 场景4:轻度偏颇(无主体质,仅有 30-40 分体质)
            mildTypes.isNotEmpty() -> {
                val topMild = mildTypes.maxByOrNull { it.transformScore }
                BodyResult(
                    type = BodyResult.ResultType.MILD,
                    title = Loc.mildTitle(),
                    description = Loc.mildDesc(),
                    scores = scores,
                    mainTypes = emptyList(),
                    mildTypes = mildTypes,
                    recommendation = Loc.mildRec(),
                    troubles = Loc.mildTroubles(topMild?.let { Loc.bodyDesc(it.bodyTypeId) })
                )
            }

            // 兜底:无明显偏颇迹象
            else -> {
                BodyResult(
                    type = BodyResult.ResultType.MILD,
                    title = Loc.noTrendTitle(),
                    description = Loc.noTrendDesc(),
                    scores = scores,
                    mainTypes = emptyList(),
                    mildTypes = emptyList(),
                    recommendation = Loc.noTrendRec(),
                    troubles = Loc.noTrendTroubles()
                )
            }
        }
    }

    
    private fun createDefaultResult(scores: Map<String, BodyScore>): BodyResult {
        val topScore = scores.values.maxByOrNull { it.transformScore }
        return BodyResult(
            type = BodyResult.ResultType.MILD,
            title = topScore?.let { Loc.bodyName(it.bodyTypeId) } ?: "—",
            description = if (topScore != null) Loc.mildDesc() else Loc.pick("Incomplete data. Please retake the assessment.", "数据不完整,请重新测评。"),
            scores = scores,
            mainTypes = topScore?.let { listOf(it) } ?: emptyList(),
            mildTypes = emptyList(),
            recommendation = if (topScore != null) Loc.mildRec() else Loc.pick("Please retake the assessment.", "建议重新完成测评。")
        )
    }

    
    fun verifyTransformScore(rawScore: Int, questionCount: Int): Double {
        return ((rawScore - questionCount).toDouble() / (questionCount * 4)) * 100
    }

    
    fun verifyReverseScore(rawScore: Int): Int {
        return 6 - rawScore
    }
}
