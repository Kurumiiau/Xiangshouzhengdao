package com.example.xiangshouzhengdao.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        HerbEntity::class,
        BodyPrescriptionEntity::class,
        SolarPrescriptionEntity::class,
        UserRecordEntity::class,
        LocalCacheEntity::class,
        FavoritePrescriptionEntity::class
    ],
    version = 4, // 新增记录约束字段，保留v3的用户与历史数据
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun herbDao(): HerbDao
    abstract fun bodyPrescriptionDao(): BodyPrescriptionDao
    abstract fun solarPrescriptionDao(): SolarPrescriptionDao
    abstract fun userRecordDao(): UserRecordDao
    abstract fun localCacheDao(): LocalCacheDao
    abstract fun favoritePrescriptionDao(): FavoritePrescriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_records ADD COLUMN constraintsJson TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("UPDATE user_records SET userId = 'guest' WHERE userId = ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // 第二次检查防止并发首次调用创建多个Room实例。
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xiangshou_database"
                ).addMigrations(MIGRATION_3_4)
                    // 未知旧版本宁可报告缺少迁移，也不能删除用户档案。
                    .build().also { INSTANCE = it }
            }
        }
    }
}
