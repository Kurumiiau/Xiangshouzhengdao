package com.example.xiangshouzhengdao.data.local

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun register(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getById(userId: String): UserEntity?

    @Query("UPDATE users SET lastLoginAt = :timestamp WHERE userId = :userId")
    suspend fun updateLastLogin(userId: String, timestamp: Long)

    @Query("UPDATE users SET nickname = :nickname WHERE userId = :userId")
    suspend fun updateNickname(userId: String, nickname: String)

    @Query("UPDATE users SET passwordHash = :passwordHash WHERE userId = :userId")
    suspend fun updatePassword(userId: String, passwordHash: String)

    @Query("SELECT * FROM users ORDER BY lastLoginAt DESC LIMIT 1")
    suspend fun getLastLoggedInUser(): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getCount(): Int
}

@Dao
interface HerbDao {
    @Query("SELECT * FROM herbs")
    suspend fun getAll(): List<HerbEntity>

    @Query("SELECT * FROM herbs WHERE herbId = :herbId")
    suspend fun getById(herbId: String): HerbEntity?

    @Query("SELECT * FROM herbs WHERE :bodyType IN (bodyTypes)")
    suspend fun getByBodyType(bodyType: String): List<HerbEntity>

    @Query("SELECT * FROM herbs WHERE :solarTerm IN (solarTerms)")
    suspend fun getBySolarTerm(solarTerm: String): List<HerbEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(herbs: List<HerbEntity>)

    @Query("DELETE FROM herbs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM herbs")
    suspend fun getCount(): Int
}

@Dao
interface BodyPrescriptionDao {
    @Query("SELECT * FROM body_prescriptions")
    suspend fun getAll(): List<BodyPrescriptionEntity>

    @Query("SELECT * FROM body_prescriptions WHERE bodyTypeId = :bodyTypeId")
    suspend fun getById(bodyTypeId: String): BodyPrescriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prescriptions: List<BodyPrescriptionEntity>)

    @Query("DELETE FROM body_prescriptions")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM body_prescriptions")
    suspend fun getCount(): Int
}

@Dao
interface SolarPrescriptionDao {
    @Query("SELECT * FROM solar_prescriptions")
    suspend fun getAll(): List<SolarPrescriptionEntity>

    @Query("SELECT * FROM solar_prescriptions WHERE solarTerm = :solarTerm")
    suspend fun getByTerm(solarTerm: String): SolarPrescriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prescriptions: List<SolarPrescriptionEntity>)

    @Query("DELETE FROM solar_prescriptions")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM solar_prescriptions")
    suspend fun getCount(): Int
}

@Dao
interface UserRecordDao {
    // guest也是有效归属，列表和写入必须使用相同的userId。
    @Query("SELECT * FROM user_records WHERE userId = :userId ORDER BY testDate DESC")
    suspend fun getByUser(userId: String): List<UserRecordEntity>

    @Query("SELECT * FROM user_records WHERE recordId = :recordId")
    suspend fun getById(recordId: String): UserRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: UserRecordEntity)

    @Delete
    suspend fun delete(record: UserRecordEntity)

    @Query("DELETE FROM user_records WHERE recordId = :recordId")
    suspend fun deleteById(recordId: String)

    @Query("SELECT COUNT(*) FROM user_records WHERE userId = :userId")
    suspend fun getCountByUser(userId: String): Int
}

@Dao
interface LocalCacheDao {
    @Query("SELECT * FROM local_cache WHERE cacheKey = :key")
    suspend fun getByKey(key: String): LocalCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: LocalCacheEntity)

    @Query("DELETE FROM local_cache WHERE cacheKey = :key")
    suspend fun deleteByKey(key: String)
}

@Dao
interface FavoritePrescriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoritePrescriptionEntity)

    @Query("SELECT * FROM favorite_prescriptions WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getByUser(userId: String): List<FavoritePrescriptionEntity>

    @Query("SELECT * FROM favorite_prescriptions WHERE favoriteId = :favoriteId")
    suspend fun getById(favoriteId: String): FavoritePrescriptionEntity?

    @Query("DELETE FROM favorite_prescriptions WHERE favoriteId = :favoriteId")
    suspend fun deleteById(favoriteId: String)

    @Query("DELETE FROM favorite_prescriptions WHERE userId = :userId")
    suspend fun deleteByUser(userId: String)

    @Query("SELECT COUNT(*) FROM favorite_prescriptions WHERE userId = :userId")
    suspend fun getCountByUser(userId: String): Int
}