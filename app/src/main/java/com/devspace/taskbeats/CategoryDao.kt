package com.devspace.taskbeats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface CategoryDao {
    @Query("Select * from categoryenity")
    fun getAll():(List<CategoryEnity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insetAll(categoryEnity: List<CategoryEnity>)
}