package com.myniprojects.swiperecycler.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DataViewDAO
{
    @Insert
    fun insert(dataView: DataView)

    @Query("DELETE FROM data_view_table WHERE id = :dataViewId")
    fun delete(dataViewId: Int)

    @Query("SELECT * FROM data_view_table ORDER BY id ASC")
    fun getAll(): LiveData<List<DataView>>

    @Query("DELETE FROM data_view_table")
    fun clear()
}