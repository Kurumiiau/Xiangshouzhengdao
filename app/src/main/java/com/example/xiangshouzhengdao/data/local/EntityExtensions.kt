package com.example.xiangshouzhengdao.data.local

import com.example.xiangshouzhengdao.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()

fun HerbEntity.toModel(): Herb {
    return Herb(
        herbId = herbId,
        name = name,
        pinyin = pinyin,
        nature = nature,
        meridian = meridian,
        effect = effect,
        role = role,
        bodyTypes = bodyTypes,
        solarTerms = solarTerms,
        contraindications = contraindications,
        volatility = volatility,
        category = category
    )
}

@JvmName("herbToModelList")
fun List<HerbEntity>.toModelList(): List<Herb> = map { it.toModel() }

fun BodyPrescriptionEntity.toModel(): BodyPrescription {
    val junType = object : TypeToken<List<BodyPrescription.HerbRatio>>() {}.type
    val chenType = object : TypeToken<List<BodyPrescription.HerbRatio>>() {}.type
    val zuoType = object : TypeToken<List<BodyPrescription.HerbRatio>>() {}.type
    val shiType = object : TypeToken<List<BodyPrescription.HerbRatio>>() {}.type

    return BodyPrescription(
        bodyTypeId = bodyTypeId,
        bodyName = bodyName,
        color = color,
        description = description,
        troubles = troubles,
        junHerbs = gson.fromJson(junHerbsJson, junType) ?: emptyList(),
        chenHerbs = gson.fromJson(chenHerbsJson, chenType) ?: emptyList(),
        zuoHerbs = gson.fromJson(zuoHerbsJson, zuoType) ?: emptyList(),
        shiHerbs = gson.fromJson(shiHerbsJson, shiType) ?: emptyList(),
        effect = effect,
        products = products,
        contraindications = contraindications
    )
}

@JvmName("bodyPrescriptionToModelList")
fun List<BodyPrescriptionEntity>.toModelList(): List<BodyPrescription> = map { it.toModel() }

fun SolarPrescriptionEntity.toModel(): SolarPrescription {
    return SolarPrescription(
        solarTerm = solarTerm,
        season = season,
        month = month,
        day = day,
        junHerbs = junHerbs,
        theme = theme,
        effect = effect
    )
}

@JvmName("solarPrescriptionToModelList")
fun List<SolarPrescriptionEntity>.toModelList(): List<SolarPrescription> = map { it.toModel() }

fun UserRecordEntity.toModel(): UserRecord {
    val answersType = object : TypeToken<Map<Int, Int>>() {}.type
    val scoresType = object : TypeToken<Map<String, BodyScore>>() {}.type
    val mainTypesType = object : TypeToken<List<String>>() {}.type
    val prescriptionType = object : TypeToken<Prescription>() {}.type

    return UserRecord(
        recordId = recordId,
        testDate = testDate,
        mode = mode,
        answers = gson.fromJson(answersJson, answersType) ?: emptyMap(),
        scores = gson.fromJson(scoresJson, scoresType) ?: emptyMap(),
        bodyType = bodyType,
        mainTypes = gson.fromJson(mainTypesJson, mainTypesType) ?: emptyList(),
        solarTerm = solarTerm,
        prescription = prescriptionJson?.let { gson.fromJson(it, prescriptionType) },
        bodyResultJson = bodyResultJson,
        constraints = gson.fromJson<List<String>>(constraintsJson, object : TypeToken<List<String>>() {}.type) ?: emptyList()
    )
}

@JvmName("userRecordToModelList")
fun List<UserRecordEntity>.toModelList(): List<UserRecord> = map { it.toModel() }

// 历史记录携带当次声明；动态更新节气时不能用空列表覆盖它。
fun UserRecord.toEntity(userId: String = "guest", bodyResultJson: String? = null): UserRecordEntity {
    return UserRecordEntity(
        recordId = recordId,
        userId = userId,
        testDate = testDate,
        mode = mode,
        answersJson = gson.toJson(answers),
        scoresJson = gson.toJson(scores),
        bodyType = bodyType,
        mainTypesJson = gson.toJson(mainTypes),
        solarTerm = solarTerm,
        prescriptionJson = prescription?.let { gson.toJson(it) },
        bodyResultJson = bodyResultJson ?: this.bodyResultJson,
        constraintsJson = gson.toJson(constraints)
    )
}