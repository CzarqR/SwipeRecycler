package com.myniprojects.swiperecycler.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_view_table")
data class DataView(
    val name: String,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)