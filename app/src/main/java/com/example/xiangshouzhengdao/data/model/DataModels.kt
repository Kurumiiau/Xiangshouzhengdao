package com.example.xiangshouzhengdao.data.model

import com.google.gson.annotations.SerializedName

data class Herb(
    @SerializedName("herbId") val herbId: String,
    @SerializedName("name") val name: String,
    @SerializedName("pinyin") val pinyin: String,
    @SerializedName("nature") val nature: String,
    @SerializedName("meridian") val meridian: String,
    @SerializedName("effect") val effect: String,
    @SerializedName("role") val role: String, // 君/臣/佐/使
    @SerializedName("bodyTypes") val bodyTypes: List<String>,
    @SerializedName("solarTerms") val solarTerms: List<String>,
    @SerializedName("contraindications") val contraindications: List<String>,
    @SerializedName("volatility") val volatility: String,
    @SerializedName("category") val category: String
)

data class BodyPrescription(
    @SerializedName("bodyTypeId") val bodyTypeId: String,
    @SerializedName("bodyName") val bodyName: String,
    @SerializedName("color") val color: String,
    @SerializedName("description") val description: String,
    @SerializedName("troubles") val troubles: List<String>,
    @SerializedName("junHerbs") val junHerbs: List<HerbRatio>,
    @SerializedName("chenHerbs") val chenHerbs: List<HerbRatio>,
    @SerializedName("zuoHerbs") val zuoHerbs: List<HerbRatio>,
    @SerializedName("shiHerbs") val shiHerbs: List<HerbRatio>,
    @SerializedName("effect") val effect: String,
    @SerializedName("products") val products: List<String>,
    @SerializedName("contraindications") val contraindications: List<String>
) {
    data class HerbRatio(
        @SerializedName("name") val name: String,
        @SerializedName("ratio") val ratio: Int
    )
}

data class SolarPrescription(
    @SerializedName("solarTerm") val solarTerm: String,
    @SerializedName("season") val season: String,
    @SerializedName("month") val month: Int,
    @SerializedName("day") val day: Int,
    @SerializedName("junHerbs") val junHerbs: List<String>,
    @SerializedName("theme") val theme: String,
    @SerializedName("effect") val effect: String
)

data class Question(
    @SerializedName("id") val id: Int,
    @SerializedName("text") val text: String,
    @SerializedName("bodyType") val bodyType: String,
    @SerializedName("reverse") val reverse: Boolean,
    @SerializedName("group") val group: Int
)

data class Option(
    val value: Int,      // 1-5
    val label: String,   // A-E
    val text: String     // 描述文字
) {
    companion object {
        val ALL = listOf(
            Option(1, "A", "完全不符合"),
            Option(2, "B", "不太符合"),
            Option(3, "C", "一般"),
            Option(4, "D", "比较符合"),
            Option(5, "E", "完全符合")
        )
    }
}

data class BodyScore(
    val bodyTypeId: String,
    val bodyName: String,
    val color: String,
    val rawScore: Int,           // 原始分
    val transformScore: Double,  // 转化分 (0-100)
    val count: Int               // 题目数量
)

data class BodyResult(
    val type: ResultType,              // 判定类型
    val title: String,                  // 结果标题
    val description: String,             // 结果描述
    val scores: Map<String, BodyScore>, // 九型分数
    val mainTypes: List<BodyScore>,      // 主体质
    val mildTypes: List<BodyScore>,      // 轻度偏颇
    val recommendation: String,         // 调理建议
    val troubles: String = ""            // 体质对应核心困扰
) {
    enum class ResultType {
        PURE_PINGHE,   // 纯平和质
        SINGLE,        // 单一偏颇
        COMPOSITE,     // 复合体质
        MILD           // 轻度偏颇
    }
}

data class PrescriptionHerb(
    val name: String,
    val role: String,      // 君/臣/佐/使
    val roleColor: String, // 角色对应颜色
    val ratio: Int,        // 百分比
    val source: String,    // 来源：节气/体质名
    val effect: String = "" // 功效描述
)

data class Prescription(
    val solarTerm: String,
    val herbs: List<PrescriptionHerb>,
    val totalEffect: String,
    val products: List<String>,
    val troubles: String = "",           // 体质对应核心困扰
    val contraindications: String = "" // 禁忌反馈说明
)

// 历史要保留答卷和当时的结果，后面调整算法时不能悄悄改掉用户已经看过的报告。
data class UserRecord(
    val recordId: String,
    val testDate: Long,           // 时间戳
    val mode: String,             // quick / full
    val answers: Map<Int, Int>,   // {题号: 分值}
    val scores: Map<String, BodyScore>,
    val bodyType: String,         // 判定类型
    val mainTypes: List<String>,
    val solarTerm: String,
    val prescription: Prescription?,
    val bodyResultJson: String? = null, // 保存判定快照，避免旧记录依赖重新计分
    val constraints: List<String> = emptyList() // 节气更新时仍须保留当次用户声明
)

object BodyTypeConfig {
    val ALL = mapOf(
        "pinghe" to BodyTypeInfo("pinghe", "平和质", "#4CAF50"),
        "qixu" to BodyTypeInfo("qixu", "气虚质", "#FF9800"),
        "yangxu" to BodyTypeInfo("yangxu", "阳虚质", "#F44336"),
        "yinxu" to BodyTypeInfo("yinxu", "阴虚质", "#9C27B0"),
        "tanshi" to BodyTypeInfo("tanshi", "痰湿质", "#795548"),
        "shire" to BodyTypeInfo("shire", "湿热质", "#FF5722"),
        "xueyu" to BodyTypeInfo("xueyu", "血瘀质", "#607D8B"),
        "qiyu" to BodyTypeInfo("qiyu", "气郁质", "#3F51B5"),
        "tebing" to BodyTypeInfo("tebing", "特禀质", "#00BCD4")
    )

    data class BodyTypeInfo(
        val id: String,
        val name: String,
        val color: String
    )
}
