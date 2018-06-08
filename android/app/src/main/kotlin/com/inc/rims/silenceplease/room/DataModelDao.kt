package com.inc.rims.silenceplease.room

import android.arch.persistence.db.SupportSQLiteQuery
import android.arch.persistence.room.*
import io.reactivex.Single

@Dao
interface DataModelDao {
    companion object {
        const val SUNDAY = 0
        const val MONDAY = 2
        const val TUESDAY = 4
        const val WEDNESDAY = 6
        const val THURSDAY = 8
        const val FRIDAY = 10
        const val SATURDAY = 12
    }

    @Query("SELECT * FROM timetable WHERE isActive = 1 AND days LIKE :str")
    fun getByDay(str: String): Single<List<DataModel>>

    @Query("SELECT * FROM timetable")
    fun getAll(): Single<List<DataModel>>

    @Query("SELECT * FROM timetable WHERE id = :id")
    fun getSingle(id: String): Single<DataModel>

    @RawQuery
    fun getNextSilence(query: SupportSQLiteQuery): Single<DataModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: DataModel): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(model: DataModel): Int

    @Delete
    fun delete(model: DataModel)
}