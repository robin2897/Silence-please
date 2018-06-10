package com.inc.rims.silenceplease.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import java.util.*


@Database(entities = [(DataModel::class)], version = 3,
        exportSchema = false)
@TypeConverters(DaysListConverter::class)
abstract class DataDatabase : RoomDatabase() {

    companion object {
        private const val DB_NAME = "main.db"
        @Volatile
        private var instance: DataDatabase? = null

        @Synchronized
        fun getInstance(context: Context): DataDatabase? {
            if (instance == null) {
                instance = create(context)
            }
            return instance
        }

        private fun create(context: Context): DataDatabase {
            return Room.databaseBuilder(context, DataDatabase::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }

    abstract fun dataModelDao(): DataModelDao

    fun getAllModelsAtParticularDay(day: Int): List<DataModel> {
        val str = StringBuilder("#############")
        when (day) {
            Calendar.MONDAY -> str.replace(DataModelDao.MONDAY, DataModelDao.MONDAY + 1,
                    "1")
            Calendar.TUESDAY -> str.replace(DataModelDao.TUESDAY, DataModelDao.TUESDAY + 1,
                    "1")
            Calendar.WEDNESDAY ->
                str.replace(DataModelDao.WEDNESDAY, DataModelDao.WEDNESDAY + 1, "1")
            Calendar.THURSDAY -> str.replace(DataModelDao.THURSDAY, DataModelDao.THURSDAY + 1,
                    "1")
            Calendar.FRIDAY -> str.replace(DataModelDao.FRIDAY, DataModelDao.FRIDAY + 1,
                    "1")
            Calendar.SATURDAY -> str.replace(DataModelDao.SATURDAY, DataModelDao.SATURDAY + 1,
                    "1")
            Calendar.SUNDAY -> str.replace(DataModelDao.SUNDAY, DataModelDao.SUNDAY + 1,
                    "1")
        }
        val finalStr = str.toString().replace("#","_")
        return dataModelDao().getByDay(finalStr).blockingGet()
    }
}