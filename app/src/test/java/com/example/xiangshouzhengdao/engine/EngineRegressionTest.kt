package com.example.xiangshouzhengdao.engine

import com.example.xiangshouzhengdao.data.model.*
import com.example.xiangshouzhengdao.i18n.Loc
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

/** 用真实题库和配方测用户能遇到的情形，避免只测公式本身、却漏掉数据和代码对不上。 */
class EngineRegressionTest {
    private val assetDir = sequenceOf(File("src/main/assets"), File("app/src/main/assets"))
        .first { it.isDirectory }
    private val gson = Gson()
    private val bodies: List<BodyPrescription> = load("body_prescriptions.json")
    private val solar: List<SolarPrescription> = load("solar_prescriptions.json")
    private val herbs: List<Herb> = load("herbs.json")
    private val engine = PrescriptionEngine(bodies, solar, herbs)

    @Before
    fun useChineseForPredictableFeedback() {
        Loc.lang = "zh"
    }

    private inline fun <reified T> load(name: String): T = gson.fromJson(
        File(assetDir, name).readText(), object : TypeToken<T>() {}.type
    )

    private fun result(main: Map<String, Double>, mild: Map<String, Double> = emptyMap()): BodyResult {
        val scores = BodyTypeConfig.ALL.mapValues { (id, info) ->
            BodyScore(id, info.name, info.color, 0, main[id] ?: mild[id] ?: 0.0, 6)
        }
        return BodyResult(
            type = if (main.isEmpty()) BodyResult.ResultType.MILD else BodyResult.ResultType.SINGLE,
            title = "测试结果", description = "", scores = scores,
            mainTypes = main.keys.map { scores.getValue(it) },
            mildTypes = mild.keys.map { scores.getValue(it) }, recommendation = ""
        )
    }

    @Test
    fun healthyAnswersProduceHighBalancedScoreInBothLanguagesAndModes() {
        for (name in listOf("questions.json", "questions_en.json", "questions_quick.json", "questions_quick_en.json")) {
            val questions: List<Question> = load(name)
            val answers = questions.associate { it.id to if (it.bodyType == "pinghe") 5 else 1 }
            val actual = ScoreEngine(questions).calculate(answers)
            assertEquals(name, BodyResult.ResultType.PURE_PINGHE, actual.type)
            assertEquals(name, 100.0, actual.scores.getValue("pinghe").transformScore, 0.0001)
        }
    }

    @Test
    fun reportingNoHealthySignsDoesNotProducePerfectBalancedConstitution() {
        val questions: List<Question> = load("questions.json")
        val actual = ScoreEngine(questions).calculate(questions.associate { it.id to 1 })
        assertEquals(0.0, actual.scores.getValue("pinghe").transformScore, 0.0001)
        assertNotEquals(BodyResult.ResultType.PURE_PINGHE, actual.type)
    }

    @Test
    fun explicitlyNegativeQuestionsStillSupportReverseScoring() {
        val question = Question(1, "负向测试题", "qixu", true, 2)
        val actual = ScoreEngine(listOf(question)).calculate(mapOf(1 to 5))
        assertEquals(0.0, actual.scores.getValue("qixu").transformScore, 0.0001)
    }

    @Test
    fun mainAndMildListsAreOrderedByScoreInsteadOfConfigurationOrder() {
        val questions: List<Question> = load("questions.json")
        val answers = questions.associate { it.id to if (it.bodyType == "pinghe") 5 else 1 }.toMutableMap()
        val values = mapOf(
            "qixu" to listOf(2, 2, 3, 3, 3, 3),
            "qiyu" to listOf(4, 4, 5, 5, 5, 5),
            "yinxu" to listOf(2, 2, 2, 2, 3, 3),
            "shire" to listOf(2, 2, 2, 3, 3, 3)
        )
        values.forEach { (id, choices) ->
            questions.filter { it.bodyType == id }.forEachIndexed { index, q -> answers[q.id] = choices[index] }
        }
        val actual = ScoreEngine(questions).calculate(answers)
        assertEquals(listOf("qiyu", "qixu"), actual.mainTypes.map { it.bodyTypeId })
        assertEquals(listOf("shire", "yinxu"), actual.mildTypes.map { it.bodyTypeId })
    }

    @Test
    fun eachQuestionIdKeepsItsConstitutionAndMeaningAcrossLanguages() {
        for (suffix in listOf("", "_quick")) {
            val zh: List<Question> = load("questions$suffix.json")
            val en: List<Question> = load("questions${suffix}_en.json")
            assertEquals(zh.map { Triple(it.id, it.bodyType, it.reverse) }, en.map { Triple(it.id, it.bodyType, it.reverse) })
        }
        val fullEn: List<Question> = load("questions_en.json")
        // 旧英文第2题曾经是睡眠题，不能再拿它接收中文第2题的气虚答案。
        assertTrue(fullEn.first { it.id == 2 }.text.startsWith("Even light activity"))
        assertTrue(fullEn.first { it.id == 7 }.text.startsWith("I fall asleep quickly"))
    }

    @Test
    fun mildOnlyResultUsesBalancedBaseAndKeepsMildHerbsAsAssistants() {
        val actual = engine.generate(result(emptyMap(), mapOf("yinxu" to 100.0 / 3)), "春分")
        assertEquals(2, actual.herbs.first { it.name == "百合" }.ratio)
        assertEquals("佐", actual.herbs.first { it.name == "百合" }.role)
        assertTrue(actual.herbs.any { it.name == "老山檀香" })
        assertEquals(100, actual.herbs.sumOf { it.ratio })
    }

    @Test
    fun nonPregnantBloodStasisUserDoesNotLoseRoseBecauseOfPregnancyWarning() {
        val actual = engine.generate(result(mapOf("xueyu" to 75.0)), "春分")
        assertTrue(actual.herbs.any { it.name == "玫瑰花" })
        assertFalse(actual.contraindications.contains("孕妇"))
    }

    @Test
    fun yangDeficientUserWithoutConflictingConditionsKeepsNormalMugwortWeight() {
        val actual = engine.generate(result(mapOf("yangxu" to 75.0)), "春分")
        assertTrue(actual.herbs.first { it.name == "艾叶" }.ratio >= 17)
        assertFalse(actual.contraindications.contains("已减量"))
    }

    @Test
    fun pregnancyDeclarationRemovesMaterialMarkedForbiddenInActualHerbLibrary() {
        val actual = engine.generate(result(mapOf("qixu" to 75.0)), "小寒", listOf("血瘀质/孕妇"))
        assertFalse(actual.herbs.any { it.name == "附子" })
        assertTrue(actual.contraindications.contains("附子"))
    }

    @Test
    fun detectedDampHeatAppliesReductionEvenWithoutSelfDeclaredCondition() {
        val actual = engine.generate(result(mapOf("shire" to 75.0)), "小雪")
        assertTrue(actual.herbs.first { it.name == "艾叶" }.ratio <= 15)
        assertTrue(actual.herbs.first { it.name == "肉桂" }.ratio <= 15)
        assertTrue(actual.contraindications.contains("已减量"))
    }

    @Test
    fun severeAllergyRemovesNasalMaterialAndDoesNotReportForbiddenAsReduced() {
        val actual = engine.generate(result(mapOf("tebing" to 75.0)), "春分", listOf("特禀重度过敏"))
        assertFalse(actual.herbs.any { it.name == "辛夷" })
        assertEquals(100, actual.herbs.sumOf { it.ratio })
    }

    @Test
    fun customCandidatesUseTheSamePregnancyAndAllergyExclusionsAsAssessment() {
        val allowed = PrescriptionEngine.filterAllowedHerbs(herbs, listOf("血瘀质/孕妇", "特禀重度过敏"))
        val names = allowed.map { it.name }.toSet()
        assertFalse("附子" in names)
        assertFalse("麝香" in names)
        assertFalse("辛夷" in names)
        assertTrue("老山檀香" in names)
    }

    @Test
    fun allDocumentedSolarTermsAndBodyTypesProduceExactlyOneHundredPercent() {
        val declarations = listOf(emptyList(), listOf("血瘀质/孕妇"), listOf("阴虚火旺", "湿热质", "特禀重度过敏"))
        for (id in BodyTypeConfig.ALL.keys) {
            val body = if (id == "pinghe") result(emptyMap()) else result(mapOf(id to 75.0))
            for (term in solar) for (conditions in declarations) {
                val first = engine.generate(body, term.solarTerm, conditions)
                assertEquals("$id/${term.solarTerm}/$conditions", 100, first.herbs.sumOf { it.ratio })
                assertTrue(first.herbs.all { it.ratio >= 0 })
                assertEquals(first, engine.generate(body, term.solarTerm, conditions))
            }
        }
    }

    @Test
    fun basePrescriptionsOnlyUseNamesThatCanBeFoundInHerbLibrary() {
        val known = herbs.map { it.name }.toSet()
        val used = bodies.flatMap { it.junHerbs + it.chenHerbs + it.zuoHerbs + it.shiHerbs }.map { it.name } +
            solar.flatMap { it.junHerbs }
        assertTrue("Unmatched materials: ${used.toSet() - known}", used.all { it in known })
    }

    @Test
    fun restoresMaterialsExplicitlyListedInOriginalPrescriptionsWithoutInventingInternalRatios() {
        val yinGuide = bodies.first { it.bodyTypeId == "yinxu" }.shiHerbs
        assertEquals(listOf(BodyPrescription.HerbRatio("鹅梨汁炼蜜", 10)), yinGuide)
        assertEquals(setOf("川芎", "羌活", "菊花"), solar.first { it.solarTerm == "霜降" }.junHerbs.toSet())
        assertEquals(setOf("乳香", "苍术", "降真香"), solar.first { it.solarTerm == "大寒" }.junHerbs.toSet())
    }
}
