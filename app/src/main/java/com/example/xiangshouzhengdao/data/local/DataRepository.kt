package com.example.xiangshouzhengdao.data.local

import android.app.Application
import android.content.Context
import androidx.room.withTransaction
import com.example.xiangshouzhengdao.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

class DataRepository private constructor(
    private val application: Application,
    private val database: AppDatabase
) {

    private val gson = Gson()

    private val userDao = database.userDao()
    private val herbDao = database.herbDao()
    private val bodyPrescriptionDao = database.bodyPrescriptionDao()
    private val solarPrescriptionDao = database.solarPrescriptionDao()
    private val userRecordDao = database.userRecordDao()
    private val localCacheDao = database.localCacheDao()
    private val favoritePrescriptionDao = database.favoritePrescriptionDao()

    companion object {
        @Volatile
        private var INSTANCE: DataRepository? = null

        const val GUEST_USER_ID = "guest"

        fun getInstance(context: Context): DataRepository {
            return INSTANCE ?: synchronized(this) {
                // 仓库跨页面存活，只持有Application，避免把首次启动Activity留在内存里。
                INSTANCE ?: DataRepository(context.applicationContext as Application, AppDatabase.getDatabase(context.applicationContext))
                    .also { INSTANCE = it }
            }
        }
    }

    suspend fun getCurrentUserId(): String = getCache("current_user_id") ?: GUEST_USER_ID

    suspend fun register(username: String, password: String, nickname: String): String? = withContext(Dispatchers.IO) {
        try {
            if (userDao.getByUsername(username) != null) {
                return@withContext null  // 用户名已存在
            }

            val userId = UUID.randomUUID().toString()
            val user = UserEntity(
                userId = userId,
                username = username,
                passwordHash = hashPassword(password),
                nickname = nickname,
                createdAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )
            userDao.register(user)
            userId
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun login(username: String, password: String): UserEntity? = withContext(Dispatchers.IO) {
        val user = userDao.getByUsername(username)
        if (user != null && user.passwordHash == hashPassword(password)) {
            userDao.updateLastLogin(user.userId, System.currentTimeMillis())
            saveCache("current_user_id", user.userId)
            user
        } else {
            null
        }
    }

    suspend fun getCurrentUser(): UserEntity? {
        val userId = getCache("current_user_id") ?: return null
        return userDao.getById(userId)
    }

    suspend fun logout() {
        deleteCache("current_user_id")
    }

    suspend fun isLoggedIn(): Boolean {
        return getCurrentUser() != null
    }

    suspend fun changePassword(userId: String, oldPassword: String, newPassword: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getById(userId) ?: return@withContext false
            if (user.passwordHash != hashPassword(oldPassword)) {
                return@withContext false // 旧密码不正确
            }
            userDao.updatePassword(userId, hashPassword(newPassword))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateNickname(userId: String, newNickname: String): Boolean = withContext(Dispatchers.IO) {
        try {
            userDao.updateNickname(userId, newNickname)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getUserCount(): Int = withContext(Dispatchers.IO) {
        try {
            userDao.getCount()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    suspend fun initializeDatabase(): Boolean = withContext(Dispatchers.IO) {
        try {
            val herbsJson = loadAssetString("herbs.json")
            val bodyJson = loadAssetString("body_prescriptions.json")
            val solarJson = loadAssetString("solar_prescriptions.json")
            val herbs: List<Herb> = gson.fromJson(herbsJson, object : TypeToken<List<Herb>>() {}.type)
            val bodies: List<BodyPrescription> = gson.fromJson(bodyJson, object : TypeToken<List<BodyPrescription>>() {}.type)
            val solars: List<SolarPrescription> = gson.fromJson(solarJson, object : TypeToken<List<SolarPrescription>>() {}.type)
            require(herbs.isNotEmpty() && bodies.isNotEmpty() && solars.isNotEmpty())
            val fingerprint = hashPassword(herbsJson + bodyJson + solarJson)
            database.withTransaction {
                val complete = herbDao.getCount() == herbs.size &&
                    bodyPrescriptionDao.getCount() == bodies.size && solarPrescriptionDao.getCount() == solars.size
                if (!complete || getCache("seed_fingerprint") != fingerprint) {
                    // 三表和完成标记一起提交；中断可重试，资产更新也能进入已安装数据库。
                    herbDao.deleteAll()
                    bodyPrescriptionDao.deleteAll()
                    solarPrescriptionDao.deleteAll()
                    herbDao.insertAll(herbs.map { it.toEntity() })
                    bodyPrescriptionDao.insertAll(bodies.map { it.toEntity() })
                    solarPrescriptionDao.insertAll(solars.map { it.toEntity() })
                    saveCache("seed_fingerprint", fingerprint)
                }
            }
            true
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            e.printStackTrace()
            false
        }
    }

    suspend fun getAllHerbs(): List<HerbEntity> = herbDao.getAll()

    suspend fun getHerbById(herbId: String): HerbEntity? = herbDao.getById(herbId)

    suspend fun getBodyPrescriptionById(bodyTypeId: String): BodyPrescriptionEntity? =
        bodyPrescriptionDao.getById(bodyTypeId)

    suspend fun getAllBodyPrescriptions(): List<BodyPrescriptionEntity> =
        bodyPrescriptionDao.getAll()

    suspend fun getSolarPrescriptionByTerm(solarTerm: String): SolarPrescriptionEntity? =
        solarPrescriptionDao.getByTerm(solarTerm)

    suspend fun getAllSolarPrescriptions(): List<SolarPrescriptionEntity> =
        solarPrescriptionDao.getAll()

    suspend fun saveUserRecord(record: UserRecordEntity) {
        userRecordDao.insert(record)
    }

    suspend fun getCurrentUserRecords(): List<UserRecordEntity> {
        val userId = getCurrentUserId()
        return userRecordDao.getByUser(userId)
    }

    suspend fun getUserRecords(userId: String): List<UserRecordEntity> =
        userRecordDao.getByUser(userId)

    suspend fun getUserRecordById(recordId: String): UserRecordEntity? =
        userRecordDao.getById(recordId)

    suspend fun deleteUserRecord(recordId: String) {
        userRecordDao.deleteById(recordId)
    }

    suspend fun saveFavoritePrescription(favorite: FavoritePrescriptionEntity) {
        favoritePrescriptionDao.insert(favorite)
    }

    suspend fun getCurrentUserFavorites(): List<FavoritePrescriptionEntity> {
        val userId = getCurrentUserId()
        return favoritePrescriptionDao.getByUser(userId)
    }

    suspend fun getUserFavorites(userId: String): List<FavoritePrescriptionEntity> =
        favoritePrescriptionDao.getByUser(userId)

    suspend fun deleteFavoritePrescription(favoriteId: String) {
        favoritePrescriptionDao.deleteById(favoriteId)
    }

    suspend fun isFavoritePrescriptionExists(favoriteId: String): Boolean {
        return favoritePrescriptionDao.getById(favoriteId) != null
    }

    suspend fun saveCache(key: String, value: String) {
        localCacheDao.insert(
            LocalCacheEntity(
                cacheKey = key,
                cacheValue = value,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun getCache(key: String): String? {
        return localCacheDao.getByKey(key)?.cacheValue
    }

    suspend fun deleteCache(key: String) {
        localCacheDao.deleteByKey(key)
    }

    private fun loadAssetString(fileName: String): String {
        return application.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun Herb.toEntity(): HerbEntity {
        return HerbEntity(
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

    private fun BodyPrescription.toEntity(): BodyPrescriptionEntity {
        return BodyPrescriptionEntity(
            bodyTypeId = bodyTypeId,
            bodyName = bodyName,
            color = color,
            description = description,
            troubles = troubles,
            junHerbsJson = gson.toJson(junHerbs),
            chenHerbsJson = gson.toJson(chenHerbs),
            zuoHerbsJson = gson.toJson(zuoHerbs),
            shiHerbsJson = gson.toJson(shiHerbs),
            effect = effect,
            products = products,
            contraindications = contraindications
        )
    }

    private fun SolarPrescription.toEntity(): SolarPrescriptionEntity {
        return SolarPrescriptionEntity(
            solarTerm = solarTerm,
            season = season,
            month = month,
            day = day,
            junHerbs = junHerbs,
            theme = theme,
            effect = effect
        )
    }
}