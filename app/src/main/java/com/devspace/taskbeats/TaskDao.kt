package com.devspace.taskbeats

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface TaskDao {
    @Query("Select * from taskentity")
    fun getAll():(List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(taskEntities: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(taskEntities: TaskEntity)

    @Update
    fun update(taskEntities: TaskEntity)

    @Delete
    fun delete(taskEntities: TaskEntity)

    @Query("Select * from taskentity where category is :categoryName")
    fun getAllByCategoryName(categoryName:String):(List<TaskEntity>)

    @Delete
    fun deleteAll(taskEntities: List<TaskEntity>)
}