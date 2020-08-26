package com.myniprojects.swiperecycler

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val dataViewItems = MutableLiveData<MutableList<DataView>>()
    private lateinit var toast: Toast

    private fun showToast(text: Any) {
        if (this::toast.isInitialized)
            toast.cancel()
        toast = Toast.makeText(this, text.toString(), Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataViewItems.value = List(20) {
            DataView(it, it.toString())
        }.toMutableList()


        val swipeListener = SwipeListener(
            { id -> showToast("Click $id") }, // click
            { id -> showToast("Long click $id") }, // long click
            { id ->
                showToast("Delete $id")
                dataViewItems.value!!.remove(DataView(id, id.toString()))
                dataViewItems.value = dataViewItems.value
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

        adapter.selectedValues.observe(this, {
            showToast("Selected id: $it")
        })

        swipeRecyclerView.adapter = adapter
        swipeRecyclerView.addItemDecoration(TopSpacingItemDecoration(10))


        dataViewItems.observe(this, {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })
    }


}

