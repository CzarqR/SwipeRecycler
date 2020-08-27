package com.myniprojects.swiperecycler.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myniprojects.swiperecycler.database.DataViewDAO

class MainActivityViewModelFactory(
    private val database: DataViewDAO
) : ViewModelProvider.Factory
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T
    {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java))
        {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}