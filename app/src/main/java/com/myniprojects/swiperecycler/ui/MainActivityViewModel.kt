package com.myniprojects.swiperecycler.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.myniprojects.swiperecycler.database.DataView
import com.myniprojects.swiperecycler.database.DataViewDAO
import kotlinx.coroutines.*

class MainActivityViewModel(private val database: DataViewDAO) : ViewModel()
{

    val dataViewItems: LiveData<List<DataView>>
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init
    {
        setDefaultDatabase()
        dataViewItems = database.getAll()
    }


    private fun setDefaultDatabase()
    {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.clear()// clear database every time
            }

            repeat(20) {
                insertNextValueToDB()
            }
        }
    }

    fun insertNextValueToDB()
    {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.insert(DataView())
            }
        }
    }

    fun delete(dataViewId: Int)
    {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.delete(dataViewId)
            }
        }
    }

    override fun onCleared()
    {
        super.onCleared()
        viewModelJob.cancel()
    }

}