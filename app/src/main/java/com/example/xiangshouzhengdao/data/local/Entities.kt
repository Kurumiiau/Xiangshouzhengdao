package com.example.xiangshouzhengdao.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,           // UUID
    val username: String,         // 用户名（唯一）
    val passwordHash: String,     // 密码哈希（SHA-256）
    val nickname: String,         // 昵称
    val createdAt: Long,          // 注册时间
    val lastLoginAt: Long         // 最后登录时间
)

@Entity(tableName = "herbs")
@TypeConverters(Converters::class)
data class HerbEntity(
    @PrimaryKey
    val herbId: String,
    val name: String,
    val pinyin: String,
    val nature: String,
    val meridian: String,
    val effect: String,
    val role: String,
    val bodyTypes: List<String>,
    val solarTerms: List<String>,
    val contraindications: List<String>,
    val volatility: String,
    val category: String
)

@Entity(tableName = "body_prescriptions")
@TypeConverters(Converters::class)
data class BodyPrescriptionEntity(
    @PrimaryKey
    val bodyTypeId: String,
    val bodyName: String,
    val color: String,
    val description: String,
    val troubles: List<String>,
    val junHerbsJson: String,
    val chenHerbsJson: String,
    val zuoHerbsJson: String,
    val shiHerbsJson: String,
    val effect: String,
    val products: List<String>,
    val contraindications: List<String>
)

@Entity(tableName = "solar_prescriptions")
@TypeConverters(Converters::class)
data class SolarPrescriptionEntity(
    @PrimaryKey
    val solarTerm: String,
    val season: String,
    val month: Int,
    val day: Int,
    val junHerbs: List<String>,
    val theme: String,
    val effect: String
)

@Entity(tableName = "user_records")
@TypeConverters(Converters::class)
data class UserRecordEntity(
    @PrimaryKey
    val recordId: String,
    val userId: String,             // 关联用户ID
    val testDate: Long,
    val mode: String,
    val answersJson: String,
    val scoresJson: String,
    val bodyType: String,
    val mainTypesJson: String,
    val solarTerm: String,
    val prescriptionJson: String?,
    val bodyResultJson: String?,    // 完整的BodyResult JSON，用于历史记录正确显示
    // 默认值也写进Room schema，保证老数据库迁移后的字段一致。
    @ColumnInfo(defaultValue = "'[]'")
    val constraintsJson: String = "[]"
)

@Entity(tableName = "local_cache")
data class LocalCacheEntity(
    @PrimaryKey
    val cacheKey: String,
    val cacheValue: String,
    val timestamp: Long
)

@Entity(tableName = "favorite_prescriptions")
@TypeConverters(Converters::class)
data class FavoritePrescriptionEntity(
    @PrimaryKey
    val favoriteId: String,         // UUID
    val userId: String,             // 关联用户ID
    val bodyType: String,           // 体质类型名称
    val solarTerm: String,          // 节气
    val totalEffect: String,        // 配方功效
    val herbsJson: String,          // 药材列表JSON
    val productsJson: String,       // 推荐产品JSON
    val contraindications: String, // 禁忌说明
    val createdAt: Long             // 收藏时间
)