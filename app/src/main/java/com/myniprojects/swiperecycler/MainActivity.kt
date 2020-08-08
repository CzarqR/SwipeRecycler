package com.myniprojects.swiperecycler

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
{
    fun AppCompatActivity.showToast(text: Any)
    {
        Toast.makeText(this, text.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val swipeListener = SwipeListener(
                { id -> showToast("Click $id")}, // click
                { id -> showToast("Long click $id") }, // long click
                { id -> showToast("Delete $id") }, // delete
                { dy -> swipeRecyclerView.scrollBy(0, dy) }, // scroll
                {
                    showToast("Cannot select value. Max number: ${SwipeRecyclerAdapter.MAX_SELECT_NUMBER}")
                }
        )

    }
}