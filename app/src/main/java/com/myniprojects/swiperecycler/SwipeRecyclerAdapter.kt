package com.myniprojects.swiperecycler

import android.annotation.SuppressLint
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.swiperecycler.databinding.SwipeRecyclerViewBinding
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SwipeRecyclerAdapter(
    private val swipeListener: SwipeListener, panelSize: Int
) : ListAdapter<DataView, SwipeRecyclerAdapter.ViewHolder>(
    SwipeDiffCallback()
)
{

    companion object
    {
        const val MAX_SELECT_NUMBER: Int = 4

        var PANEL_SIZE = 125
            private set
    }

    private val _selectedValues: MutableLiveData<ArrayList<Int>> = MutableLiveData()
    val selectedValues: LiveData<ArrayList<Int>>
        get() = _selectedValues

    init
    {
        PANEL_SIZE = panelSize
        _selectedValues.value = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        return ViewHolder.from(
            parent,
            _selectedValues,
            swipeListener
        )
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(getItem(position)!!, swipeListener)
    }


    class ViewHolder private constructor(
        private val binding: SwipeRecyclerViewBinding,
        private val selectedItems: MutableLiveData<ArrayList<Int>>,
        private val swipeListener: SwipeListener
    ) :
            RecyclerView.ViewHolder(binding.root), View.OnTouchListener
    {

        private var xStart = 0F
        private var lastY = 0F
        private var yStart = 0F
        private val handler: Handler = Handler()
        private var isLongClickCanceled = false
        private var wasLongClicked = false
        private var startScrolling = false
        private var status = 0
            set(value)
            {
                field = when
                {
                    value > 0 ->
                    {
                        min(value, PANEL_SIZE)
                    }
                    value < 0 ->
                    {
                        max(value, -PANEL_SIZE)
                    }
                    else ->
                    {
                        value
                    }
                }
                setSizes()
            }

        companion object
        {
            fun from(
                parent: ViewGroup,
                selectedCar: MutableLiveData<ArrayList<Int>>,
                swipeListener: SwipeListener
            ): ViewHolder
            {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SwipeRecyclerViewBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(
                    binding, selectedCar, swipeListener
                )
            }

            private const val LONG_CLICK_TIME = 550L
            private const val CLICK_DISTANCE = 75
        }


        private val isValueSelected: Boolean
            get()
            {
                return selectedItems.value!!.contains(binding.dataView!!.id)
            }

        private val canAdd: Boolean
            get()
            {
                return selectedItems.value!!.size < MAX_SELECT_NUMBER
            }

        private fun addItem()
        {
            if (!selectedItems.value!!.contains(binding.dataView!!.id))
            {
                selectedItems.value!!.add(binding.dataView!!.id)
                selectedItems.value = selectedItems.value
            }
        }

        private fun removeItem()
        {
            if (selectedItems.value!!.contains(binding.dataView!!.id))
            {
                selectedItems.value!!.remove(binding.dataView!!.id)
                selectedItems.value = selectedItems.value
            }
        }


        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            dataView: DataView,
            swipeListener: SwipeListener
        )
        {
            binding.dataView = dataView
            binding.clickListener = swipeListener

            binding.rootCL.setOnLongClickListener {
                swipeListener.clickLongListener(dataView.id)
                true
            }

            binding.rootCL.setOnTouchListener(this)

            status = if (isValueSelected) //value was selected, show right panel
            {
                -PANEL_SIZE
            }
            else
            {
                0
            }

            binding.executePendingBindings()
        }

        private val leftPanel = binding.rootCL.getChildAt(0)
        private val rightPanel = binding.rootCL.getChildAt(1)
        private val centerPanel = binding.rootCL.getChildAt(2)

        private fun setSizes()
        {
            when
            {
                status == 0 -> // center
                {
                    leftPanel.layoutParams.width = 1
                    rightPanel.layoutParams.width = 1
                }
                status > 0 -> //right
                {
                    leftPanel.layoutParams.width = status
                    rightPanel.layoutParams.width = 1
                }
                else -> //left
                {
                    leftPanel.layoutParams.width = 1
                    rightPanel.layoutParams.width = -status
                }
            }

            leftPanel.requestLayout()
            rightPanel.requestLayout()

            centerPanel.setBackgroundResource(R.drawable.gradient_view)
            leftPanel.setBackgroundResource(R.drawable.gradient_view_delete)
            rightPanel.setBackgroundResource(R.drawable.gradient_view_select)
        }


        override fun onTouch(v: View?, event: MotionEvent?): Boolean
        {
            if (v != null && event != null)
            {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action)
                {
                    MotionEvent.ACTION_DOWN ->
                    {
                        xStart = event.x
                        yStart = event.y
                        isLongClickCanceled = false
                        wasLongClicked = false
                        startScrolling = false
                        handler.postDelayed({ //long click
                            wasLongClicked = true
                            v.performLongClick()
                                            }, LONG_CLICK_TIME)
                    }
                    MotionEvent.ACTION_UP ->
                    {
                        handler.removeCallbacksAndMessages(null)
                        if (!startScrolling && !isLongClickCanceled && !wasLongClicked)
                        {
                            if ((event.eventTime - event.downTime) < LONG_CLICK_TIME) //click
                            {
                                if (status == 0)
                                {
                                    v.performClick()
                                }
                                else
                                {
                                    status = 0
                                    removeItem()
                                }
                            }
                        }
                        else if (!startScrolling)
                        {
                            when
                            {
                                status > (PANEL_SIZE / 2) -> //show left
                                {
                                    status = PANEL_SIZE
                                    removeItem()
                                }
                                status < -(PANEL_SIZE / 2) -> //show right
                                {

                                    if (canAdd)//car can be added
                                    {
                                        status = -PANEL_SIZE
                                        addItem()
                                    }
                                    else
                                    {
                                        status = 0
                                        swipeListener.cannotSelectValue()
                                    }
                                }
                                else ->
                                {
                                    status = 0
                                    removeItem()
                                }
                            }
                        }
                    }
                    MotionEvent.ACTION_MOVE ->
                    {
                        if (startScrolling)
                        {
                            swipeListener.scroll((lastY - event.rawY).toInt())
                            lastY = event.rawY
                        }
                        else
                        {
                            if (!wasLongClicked)
                            {

                                if (isLongClickCanceled)
                                {
                                    val deltaX = (event.x - xStart).toInt()

                                    if (abs(deltaX) > 75)
                                    {

                                        if (deltaX > 0)
                                        {
                                            status = (deltaX - CLICK_DISTANCE)
                                        }
                                        else if (deltaX < 0)
                                        {
                                            status = (deltaX + CLICK_DISTANCE)
                                        }

                                    }
                                }
                                else if (abs(yStart - event.y) > CLICK_DISTANCE)
                                {
                                    lastY = event.rawY
                                    startScrolling = true
                                    handler.removeCallbacksAndMessages(null)
                                }
                                else if (!isLongClickCanceled && abs(xStart - event.x) >= CLICK_DISTANCE)
                                {
                                    isLongClickCanceled = true
                                    handler.removeCallbacksAndMessages(null)
                                }
                            }
                        }

                    }
                }
            }
            return true
        }
    }


}

class SwipeDiffCallback : DiffUtil.ItemCallback<DataView>()
{
    override fun areItemsTheSame(oldItem: DataView, newItem: DataView): Boolean
    {
        Log.d("AppDebug", "areItemsTheSame")
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataView, newItem: DataView): Boolean
    {
        Log.d("AppDebug", "areContentsTheSame")
        return oldItem == newItem
    }
}


class SwipeListener(
    val clickListener: (dataViewId: Int) -> Unit,
    val clickLongListener: (dataViewId: Int) -> Unit,
    val clickDeleteListener: (dataViewId: Int) -> Unit,
    val scroll: (dy: Int) -> Unit,
    val cannotSelectValue: () -> Unit
)
{
    fun onClick(dataView: DataView) = clickListener(dataView.id)
    fun onDeleteClick(dataView: DataView) = clickDeleteListener(dataView.id)
}

