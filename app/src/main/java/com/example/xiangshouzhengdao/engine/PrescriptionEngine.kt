package com.example.xiangshouzhengdao.engine

import com.example.xiangshouzhengdao.data.model.*
import com.example.xiangshouzhengdao.i18n.Loc

class PrescriptionEngine(
    private val bodyPrescriptions: List<BodyPrescription>,
    private val solarPrescriptions: List<SolarPrescription>,
    private val herbs: List<Herb> = emptyList()
) {

    companion object {
        /** 定制页只选材料、不算比例；复用同一禁用规则，避免两个入口对同一个声明给出相反结果。 */
        fun filterAllowedHerbs(
            herbs: List<Herb>,
            userConstraints: List<String>,
            bodyTypeIds: List<String> = emptyList()
        ): List<Herb> {
            // 这里只运行材料规则，体质方和节气方不会参与，也不会调用 generate。
            val policy = PrescriptionEngine(emptyList(), emptyList(), herbs)
            val candidates = herbs.map {
                MutablePrescriptionHerb(it.name, it.role, policy.roleColorOf(it.role), 1.0, "", it.effect)
            }
            val allowedNames = policy.applyContraindications(candidates, userConstraints, bodyTypeIds)
                .first.map { it.name }.toSet()
            return herbs.filter { it.name in allowedNames }
        }
    }

    
    data class ContraindicationFeedback(
        val filteredHerbs: List<String>,          // 被禁用的药材名
        val reducedHerbs: List<String>,           // 被减量的药材名
        val reasons: List<String>                 // 处理原因说明
    )

    
    fun generate(
        bodyResult: BodyResult,
        solarTerm: String,
        userConstraints: List<String> = emptyList()
    ): Prescription {
        // 1. 获取节气基底方
        val solarBase = solarPrescriptions.find { it.solarTerm == solarTerm }
            ?: solarPrescriptions.first()

        // 轻度体质只能进辅助部分；没有典型偏颇时用平和基础方，不能把最高轻度分升成主体。
        val mainBodyTypes = bodyResult.mainTypes.filter { it.transformScore >= 40 && it.bodyTypeId != "pinghe" }
        val mildBodyTypes = bodyResult.mildTypes.filter { it.transformScore >= 30 && it.transformScore < 40 }

        val herbMap = mutableMapOf<String, MutablePrescriptionHerb>()

        // 3. 节气药材 45%(顺天时,时令为主)
        solarBase.junHerbs.forEach { herbName ->
            mergeHerb(herbMap, herbName, 45.0 / solarBase.junHerbs.size, "君", "节气", solarBase.effect)
        }

        // 4. 主体质药材 40%(调人和);复合体质按转化分占比分配
        val totalMain = mainBodyTypes.sumOf { it.transformScore }
        if (totalMain > 0) {
            mainBodyTypes.forEach { bodyScore ->
                val weight = bodyScore.transformScore / totalMain
                val bodyTotalWeight = 40.0 * weight
                val bodyPrescription = bodyPrescriptions.find { it.bodyTypeId == bodyScore.bodyTypeId }
                    ?: return@forEach
                addBodyHerbs(herbMap, bodyPrescription, bodyTotalWeight, bodyScore.bodyName, mild = false)
            }
        } else {
            bodyPrescriptions.find { it.bodyTypeId == "pinghe" }?.let {
                addBodyHerbs(herbMap, it, 40.0, Loc.bodyName("pinghe"), mild = false)
            }
        }

        // 5. 轻度偏颇体质 10%(辅助调理,按转化分占比分配)
        val totalMild = mildBodyTypes.sumOf { it.transformScore }
        if (totalMild > 0) {
            mildBodyTypes.forEach { mildScore ->
                val weight = mildScore.transformScore / totalMild
                val bodyTotalWeight = 10.0 * weight
                val bodyPrescription = bodyPrescriptions.find { it.bodyTypeId == mildScore.bodyTypeId }
                    ?: return@forEach
                addBodyHerbs(herbMap, bodyPrescription, bodyTotalWeight, mildScore.bodyName, mild = true)
            }
        }

        // 6. 调和使药固定 5%(榆皮粉 3% + 蜂蜜 2%)
        mergeHerb(herbMap, "榆皮粉", 3.0, "使", "调和", "粘合定香")
        mergeHerb(herbMap, "蜂蜜", 2.0, "使", "调和", "缓和药性")

        // 7. 禁忌处理:禁用(孕妇/血瘀等)+ 减少(阴虚火旺/湿热等,按资料"减少/降低"而非禁用)
        val (processed, feedback) = applyContraindications(
            herbMap.values.toList(),
            userConstraints,
            (mainBodyTypes + mildBodyTypes).map { it.bodyTypeId }
        )

        // 8. 收集体质核心困扰
        val troubles = collectTroubles(bodyResult)

        // 9. 归一化并转换为最终数据模型
        // 无轻度时原文没有定义那10%的去向，沿用按实际总量归一化；最大余数法补回整数取整损失。
        val positiveHerbs = processed.filter { it.ratio > 0 && it.ratio.isFinite() }
        val percentages = allocatePercentages(positiveHerbs)
        val normalizedHerbs = positiveHerbs.mapIndexed { index, herb ->
            PrescriptionHerb(
                name = herb.name,
                role = herb.role,
                roleColor = herb.roleColor,
                ratio = percentages[index],
                source = herb.source,
                effect = herb.effect
            )
        }.sortedByDescending { it.ratio }

        // 10. 推荐产品与功效
        val totalEffect = generateTotalEffect(bodyResult, solarBase)
        val recommendedProducts = generateRecommendedProducts(bodyResult)
        val contraindicationText = buildContraindicationText(feedback)

        return Prescription(
            solarTerm = solarTerm,
            herbs = normalizedHerbs,
            totalEffect = totalEffect,
            products = recommendedProducts,
            troubles = troubles,
            contraindications = contraindicationText
        )
    }

    
    private fun addBodyHerbs(
        map: MutableMap<String, MutablePrescriptionHerb>,
        bodyPrescription: BodyPrescription,
        totalWeight: Double,
        sourceName: String,
        mild: Boolean
    ) {
        // 基准方中各药材的 ratio 已编码君臣佐使配比(君40/臣30/佐20/使10,合计100),
        // 因此单味占比 = 总权重 × (ratio/100),不再重复乘角色系数
        bodyPrescription.junHerbs.forEach { herbRatio ->
            mergeHerb(map, herbRatio.name, totalWeight * (herbRatio.ratio / 100.0), if (mild) "佐" else "君", sourceName, bodyPrescription.effect)
        }
        bodyPrescription.chenHerbs.forEach { herbRatio ->
            mergeHerb(map, herbRatio.name, totalWeight * (herbRatio.ratio / 100.0), if (mild) "佐" else "臣", sourceName, bodyPrescription.effect)
        }
        bodyPrescription.zuoHerbs.forEach { herbRatio ->
            mergeHerb(map, herbRatio.name, totalWeight * (herbRatio.ratio / 100.0), "佐", sourceName, bodyPrescription.effect)
        }
        // “鹅梨汁炼蜜”是原文给出的复合材料，总占比10%；资料没给内部比例，不能擅自拆成各5%。
        bodyPrescription.shiHerbs.forEach { herbRatio ->
            mergeHerb(map, herbRatio.name, totalWeight * (herbRatio.ratio / 100.0), "使", sourceName, bodyPrescription.effect)
        }
    }

    
    private fun mergeHerb(
        map: MutableMap<String, MutablePrescriptionHerb>,
        name: String,
        ratio: Double,
        role: String,
        source: String,
        effect: String
    ) {
        val existing = map[name]
        if (existing != null) {
            existing.ratio += ratio
            val rolePriority = mapOf("君" to 4, "臣" to 3, "佐" to 2, "使" to 1)
            val existingPriority = rolePriority[existing.role] ?: 0
            val newPriority = rolePriority[role] ?: 0
            if (newPriority > existingPriority) {
                existing.role = role
                existing.roleColor = roleColorOf(role)
            }
            if (!existing.source.contains(source)) {
                existing.source = "${existing.source}+$source"
            }
        } else {
            map[name] = MutablePrescriptionHerb(
                name = name,
                role = role,
                roleColor = roleColorOf(role),
                ratio = ratio,
                source = source,
                effect = effect
            )
        }
    }

    private fun roleColorOf(role: String): String = when (role) {
        "君" -> "#20443B"
        "臣" -> "#2E5E52"
        "佐" -> "#6F9B8F"
        "使" -> "#A8A29A"
        else -> "#999999"
    }

    private fun allocatePercentages(herbs: List<MutablePrescriptionHerb>): List<Int> {
        if (herbs.isEmpty()) return emptyList()
        val total = herbs.sumOf { it.ratio }
        val exact = herbs.map { it.ratio / total * 100.0 }
        val whole = exact.map { kotlin.math.floor(it).toInt() }.toMutableList()
        val remainder = 100 - whole.sum()
        // 同余数时沿原药材顺序分配，重复生成同一香方不会随机变动。
        exact.indices.sortedByDescending { exact[it] - whole[it] }.take(remainder).forEach {
            whole[it]++
        }
        return whole
    }

    
    private fun applyContraindications(
        herbs: List<MutablePrescriptionHerb>,
        constraints: List<String>,
        bodyTypeIds: List<String>
    ): Pair<List<MutablePrescriptionHerb>, ContraindicationFeedback> {
        val forbiddenMap = mapOf(
            "xueyu" to listOf("川芎", "红花", "降真香"),
            "血瘀质" to listOf("川芎", "红花", "降真香"),
            "血瘀质/孕妇" to listOf("川芎", "红花", "降真香", "麝香", "桃仁", "三棱", "莪术"),
            "血瘀质、孕妇" to listOf("川芎", "红花", "降真香", "麝香", "桃仁", "三棱", "莪术"),
            "孕妇" to listOf("川芎", "红花", "降真香", "麝香", "桃仁", "三棱", "莪术"),
            "tebing" to listOf("麝香", "龙脑", "冰片"),
            "特禀质" to listOf("麝香", "龙脑", "冰片"),
            "特禀重度过敏" to listOf("麝香", "龙脑", "冰片", "辛夷", "花粉"),
            "高血压" to listOf("附子", "肉桂", "丁香"),
            "儿童" to listOf("附子", "肉桂", "麝香"),
            "哺乳期" to listOf("麝香", "冰片", "龙脑")
        )
        val reduceMap = mapOf(
            "yinxu" to listOf("肉桂", "丁香", "干姜", "附子", "艾叶"),
            "阴虚火旺" to listOf("肉桂", "丁香", "干姜", "附子", "艾叶"),
            "shire" to listOf("艾叶", "干姜", "肉桂", "附子"),
            "湿热质" to listOf("艾叶", "干姜", "肉桂", "附子")
        )
        val reduceFactor = 0.4

        val forbidden = mutableSetOf<String>()
        val reduced = mutableSetOf<String>()
        val reasons = mutableListOf<String>()

        // 用户声明和本次识别出的体质共同参与规则。孕妇只来自用户声明，不从体质方说明推断。
        val activeConditions = (constraints + bodyTypeIds).toMutableSet()
        if (constraints.any { it == "血瘀质/孕妇" || it == "血瘀质、孕妇" }) {
            activeConditions.add("孕妇")
        }
        if (constraints.contains("血瘀质")) activeConditions.add("xueyu")
        if (constraints.contains("湿热质")) activeConditions.add("shire")
        if (constraints.contains("阴虚火旺")) activeConditions.add("yinxu")
        activeConditions.forEach { constraint ->
            forbiddenMap[constraint]?.let { list ->
                if (list.isNotEmpty()) {
                    forbidden.addAll(list)
                    reasons.add(Loc.contraReasonForbidden(constraint, list.joinToString("、")))
                }
            }
            reduceMap[constraint]?.let { list ->
                if (list.isNotEmpty()) {
                    reduced.addAll(list)
                    reasons.add(Loc.contraReasonReduced(constraint, list.joinToString("、")))
                }
            }
        }

        // 这里读单味材料的明确禁用项，防止硬编码名单漏掉“附子：孕妇禁用”等已有数据。
        // 原文对阴虚既写过“禁用”又写“减量”，先保留融合算法的减量口径，不擅自扩大成全禁。
        val includedNames = herbs.map { it.name }.toSet()
        this.herbs.filter { it.name in includedNames }.forEach { herb ->
            herb.contraindications.forEach { declaration ->
                val isAbsolute = declaration.contains("禁用") || declaration.endsWith("忌")
                val conditionMatches = when {
                    declaration.contains("孕妇") -> "孕妇" in activeConditions
                    declaration.contains("特禀重度过敏") -> "特禀重度过敏" in activeConditions
                    declaration.contains("儿童") -> "儿童" in activeConditions
                    declaration.contains("哺乳期") -> "哺乳期" in activeConditions
                    declaration.contains("高血压") -> "高血压" in activeConditions
                    else -> false
                }
                if (isAbsolute && conditionMatches) {
                    forbidden.add(herb.name)
                    reasons.add(Loc.contraReasonForbidden(declaration, herb.name))
                }
            }
        }

        val processed = herbs
            .filter { it.name !in forbidden }
            .map { herb ->
                if (herb.name in reduced) {
                    herb.copy(ratio = herb.ratio * reduceFactor)
                } else {
                    herb
                }
            }

        val feedback = ContraindicationFeedback(
            filteredHerbs = herbs.map { it.name }.filter { it in forbidden }.distinct(),
            reducedHerbs = herbs.map { it.name }.filter { it in reduced && it !in forbidden }.distinct(),
            reasons = reasons.distinct()
        )

        return Pair(processed, feedback)
    }

    
    private fun collectTroubles(bodyResult: BodyResult): String {
        val troublesList = mutableListOf<String>()

        bodyResult.mainTypes.forEach { bodyScore ->
            val bodyPrescription = bodyPrescriptions.find { it.bodyTypeId == bodyScore.bodyTypeId }
            bodyPrescription?.troubles?.let { troublesList.addAll(it) }
        }
        bodyResult.mildTypes.forEach { bodyScore ->
            val bodyPrescription = bodyPrescriptions.find { it.bodyTypeId == bodyScore.bodyTypeId }
            bodyPrescription?.troubles?.let { troublesList.addAll(it) }
        }

        return if (troublesList.isNotEmpty()) {
            troublesList.distinct().joinToString("、")
        } else {
            bodyResult.troubles
        }
    }

    
    private fun buildContraindicationText(feedback: ContraindicationFeedback): String {
        val parts = mutableListOf<String>()
        if (feedback.filteredHerbs.isNotEmpty()) {
            parts.add(Loc.contraRemoved(feedback.filteredHerbs.joinToString("、")))
        }
        if (feedback.reducedHerbs.isNotEmpty()) {
            parts.add(Loc.contraReduced(feedback.reducedHerbs.joinToString("、")))
        }
        if (parts.isEmpty()) return ""
        return Loc.contraHeader() + parts.joinToString("; ") + ". " + feedback.reasons.joinToString("; ")
    }

    
    private fun generateRecommendedProducts(bodyResult: BodyResult): List<String> {
        val productMap = mapOf(
            "pinghe" to listOf("四季随身香囊", "居家线香"),
            "qixu" to listOf("办公香插", "随身香珠"),
            "yangxu" to listOf("热敷香包", "车载香薰"),
            "yinxu" to listOf("睡眠香包", "卧室香薰"),
            "tanshi" to listOf("居家盘香", "浴室香氛"),
            "shire" to listOf("无火香插", "随身香囊"),
            "xueyu" to listOf("佩戴香珠", "睡前香包"),
            "qiyu" to listOf("睡眠热敷包", "办公舒缓香薰"),
            "tebing" to listOf("防疫香珠", "防敏香插")
        )
        val typeIds = (bodyResult.mainTypes + bodyResult.mildTypes).map { it.bodyTypeId }.distinct()
        val products = typeIds.flatMap { productMap[it] ?: emptyList() }.distinct()
        return (if (products.isEmpty()) productMap["pinghe"]!! else products).map { Loc.product(it) }
    }

    
    private fun generateTotalEffect(bodyResult: BodyResult, solarBase: SolarPrescription): String {
        val mainTypeNames = bodyResult.mainTypes.joinToString("、") { Loc.bodyName(it.bodyTypeId) }
        val solarTheme = Loc.solarTheme(solarBase.theme)

        return if (bodyResult.mainTypes.isNotEmpty()) {
            Loc.totalEffect(solarTheme, mainTypeNames, bodyResult.recommendation)
        } else {
            Loc.totalEffectPeace(solarTheme)
        }
    }

    
    private data class MutablePrescriptionHerb(
        val name: String,
        var role: String,
        var roleColor: String,
        var ratio: Double,
        var source: String,
        val effect: String
    )
}
