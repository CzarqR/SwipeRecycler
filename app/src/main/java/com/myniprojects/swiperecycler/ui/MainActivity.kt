package com.myniprojects.swiperecycler.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.myniprojects.swiperecycler.R
import com.myniprojects.swiperecycler.database.AppDatabase
import com.myniprojects.swiperecycler.databinding.ActivityMainBinding
import com.myniprojects.swiperecycler.recycler.SwipeListener
import com.myniprojects.swiperecycler.recycler.SwipeRecyclerAdapter
import com.myniprojects.swiperecycler.recycler.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity()
{
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var toast: Toast
    private lateinit var binding: ActivityMainBinding

    // simple function which only show one toast without accumulation
    private fun showToast(text: Any)
    {
        if (this::toast.isInitialized)
            toast.cancel()
        toast = Toast.makeText(this, text.toString(), Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Init view model
        val database = AppDatabase.getInstance(application).dataViewDAO
        val viewModelFactory = MainActivityViewModelFactory(database)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainActivityViewModel::class.java)

        // set listener to handle events like click, long click, swipe left/right, selecting too many items and scrolling
        val swipeListener = SwipeListener(
            { id -> showToast("Click $id") }, // click
            { id -> showToast("Long click $id") }, // long click
            { id ->
                viewModel.delete(id)
            }, // delete
            { dy -> swipeRecyclerView.scrollBy(0, dy) }, // scroll
            {
                showToast("You can select up to  ${SwipeRecyclerAdapter.MAX_SELECT_NUMBER}")
            }
        )

        val adapter = SwipeRecyclerAdapter(
            swipeListener,
            resources.displayMetrics.widthPixels / 8 //maximum size of left/right panel
        )

        // observe items in database and update RecyclerView
        viewModel.dataViewItems.observe(this, {
            adapter.submitList(it)
        })

        // observe selected items in RecyclerView
        adapter.selectedValues.observe(this, {
            showToast("Selected id: $it")
        })

        binding.swipeRecyclerView.adapter = adapter
        binding.swipeRecyclerView.addItemDecoration(TopSpacingItemDecoration(10)) //setting space between items in RecyclerView

        // add new value to RecyclerView
        binding.butAddCar.setOnClickListener {
            viewModel.insertNextValueToDB()
        }
    }
}

