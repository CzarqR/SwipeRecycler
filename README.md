# ReceyclerView with sliding left/right.

What is this RecyclerView doing?
- Enables to swipe left or right and shows a new panel
- Clicking on this panel can be handled
- Recycler detects click and long click
- When swipe is made to less than half of maximum size panel will collapse
- Sliding on right selects item, it is possible to select maximum size of selected items
- Selected items can be observed

**How it looks:**

![Swipe RecyclerView](https://i.imgur.com/KrFAEkC.mp4)

<hr>

**How to do it:**

Start project:  
- The database based on [`Room`](https://developer.android.com/topic/libraries/architecture/room). One `Entity` called `DataView` which holds `id`. `DAO` with standard queries like `insert`, `delete`, `clearAll`, and `getAll`
- MainActivity with ViewModel and ViewModelFactory. ViewModel has LiveData of all DataViews from the database. MainActivity layout just has a Recycler and button to insert a new item.
- 3 icons named: `plus`, `delete`, `selected`

**0. activity_main.xml:**
``` xml
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    >

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".ui.MainActivity"
        >

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/swipeRecyclerView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_marginStart="1dp"
            android:layout_marginEnd="1dp"
            android:splitMotionEvents="false"
            app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
            />

        <com.google.android.material.floatingactionbutton.FloatingActionButton
            android:id="@+id/butAddCar"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_margin="16dp"
            android:src="@drawable/plus"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            />

    </androidx.constraintlayout.widget.ConstraintLayout>
</layout>
```

File structure:

[![enter image description here][1]][1]


**1. Add Gradle dependencies to RecyclerView and CardView**
```
// RecyclerView
implementation 'androidx.recyclerview:recyclerview:1.1.0'

// CardView
implementation "androidx.cardview:cardview:1.0.0"
```

**2. Crete gradient background for Recycler item. The main view, delete panel, and selected panel. (of course, it doesn't have to be gradient but You need 3 backgrounds)**

colors.xml:
``` xml
<color name="recycler_left">#FFCDD2</color>
<color name="recycler_right">#C8E6C9</color>
<color name="recycler_delete_left">#E57373</color>
<color name="recycler_select_right">#81C784</color>
```

gradient_view.xml:
``` xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <gradient
        android:angle="0"
        android:endColor="@color/recycler_right"
        android:startColor="@color/recycler_left"
        android:type="linear" />

</shape>
```

gradient_view_delete.xml:
``` xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <gradient
        android:angle="0"
        android:endColor="@color/recycler_left"
        android:startColor="@color/recycler_delete_left"
        android:type="linear" />

</shape>
```

gradient_view_select.xml:
``` xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <gradient
        android:angle="0"
        android:endColor="@color/recycler_select_right"
        android:startColor="@color/recycler_right"
        android:type="linear" />

</shape>
```

**3. Create a layout for the Recycler item.** 

swipe_recycler_view.xml:
``` xml
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    >

    <data>

        <variable
            name="dataView"
            type="com.myniprojects.swiperecycler.database.DataView"
            />

        <variable
            name="clickListener"
            type="com.myniprojects.swiperecycler.recycler.SwipeListener"
            />

    </data>

    <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="6dp"
        app:cardElevation="4dp"
        app:cardPreventCornerOverlap="false"
        >

        <androidx.constraintlayout.widget.ConstraintLayout
            android:id="@+id/rootCL"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@drawable/gradient_view"
            android:onClick="@{()-> clickListener.onClick(dataView)}"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            >
            <!-- Delete panel-->
            <FrameLayout
                android:id="@+id/frameDelete"
                android:layout_width="1px"
                android:layout_height="0dp"
                android:layout_gravity="center"
                android:background="@drawable/gradient_view_delete"
                android:onClick="@{()-> clickListener.onDeleteClick(dataView)}"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toTopOf="parent"
                >

                <ImageView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:adjustViewBounds="true"
                    android:contentDescription="@string/delete"
                    android:padding="10dp"
                    app:srcCompat="@drawable/delete"
                    />

            </FrameLayout>

            <!-- Select panel-->
            <FrameLayout
                android:id="@+id/frameSelect"
                android:layout_width="1px"
                android:layout_height="0dp"
                android:layout_gravity="center"
                android:background="@drawable/gradient_view_select"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintTop_toTopOf="parent"
                >

                <ImageView
                    android:layout_width="wrap_content"
                    android:layout_height="match_parent"
                    android:layout_gravity="center"
                    android:adjustViewBounds="true"
                    android:contentDescription="@string/select"
                    android:padding="10dp"
                    app:srcCompat="@drawable/selected"
                    />

            </FrameLayout>

            <!-- Main panel-->
            <androidx.constraintlayout.widget.ConstraintLayout
                android:id="@+id/carBackground"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                app:layout_constraintEnd_toStartOf="@id/frameSelect"
                app:layout_constraintStart_toEndOf="@id/frameDelete"
                app:layout_constraintTop_toTopOf="parent"
                >

                <TextView
                    android:id="@+id/txtContent"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="16dp"
                    android:layout_marginTop="8dp"
                    android:layout_marginBottom="8dp"
                    android:text="@{Integer.toString(dataView.id)}"
                    android:textColor="#1B1919"
                    android:textSize="50sp"
                    app:layout_constraintBottom_toBottomOf="parent"
                    app:layout_constraintStart_toStartOf="parent"
                    app:layout_constraintTop_toTopOf="parent"
                    />

            </androidx.constraintlayout.widget.ConstraintLayout>

        </androidx.constraintlayout.widget.ConstraintLayout>

    </androidx.cardview.widget.CardView>

</layout>
```
How does it work? ConstraintLayout is the root of the whole layout. It holds two FrameLayouts on the left and right side and in the middle is another ConstrintLayout. FrameLayouts width is set to 1 so they are invisible. When someone will swipe on this item we can change FrameLayouth width to make it visible. 

**4. Create RecyclerViewAdapter. I use ListAdapter with DiffUtil. The adapter also needs Class which can handle clicking or swiping so also create new Class SwipeListener. Many parts of the code are commented.** 
``` kotlin
import android.annotation.SuppressLint
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myniprojects.swiperecycler.R
import com.myniprojects.swiperecycler.database.DataView
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
        const val MAX_SELECT_NUMBER: Int = 4 // maximum number of items that user can select

        var PANEL_SIZE = 125 // delete and select panel width, the base is 125 but in the constructor we can pass new value based on DP which override this
            private set
    }

    // LiveData which holds all selected items in Recycler
    private val _selectedValues: MutableLiveData<ArrayList<Int>> = MutableLiveData()
    val selectedValues: LiveData<ArrayList<Int>>
        get() = _selectedValues

    init
    {
        PANEL_SIZE = panelSize //
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
        private val swipeListener: SwipeListener // listener which enables to handle click etc.
    ) :
            RecyclerView.ViewHolder(binding.root), View.OnTouchListener
    {
        private var xStart = 0F // variables which track swiping in onTouch event
        private var lastY = 0F
        private var yStart = 0F
        private val handler: Handler = Handler() // Handler enable to detect long click
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

            private const val LONG_CLICK_TIME = 550L // time in millis to detect long click
            private const val CLICK_DISTANCE = 75 //distance in pixels to disable click/long click and enable scrolling or swiping
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
            if (!isValueSelected)
            {
                selectedItems.value!!.add(binding.dataView!!.id)
                selectedItems.value = selectedItems.value
            }
        }

        private fun removeItem()
        {
            if (isValueSelected)
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

        // here swiping, clicking and scrolling is detected. MotionEvent is tracked and function recognize what to do
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

// DiffUtil class, it helps to better calculate when to refresh Recycler
class SwipeDiffCallback : DiffUtil.ItemCallback<DataView>()
{
    override fun areItemsTheSame(oldItem: DataView, newItem: DataView): Boolean
    {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataView, newItem: DataView): Boolean
    {
        return oldItem == newItem
    }
}

// listener which can handle clicking, swiping, scrolling and selecting too many items
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
```

**5. Create Item decorator to add space between items in Recycler**
``` kotlin
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class TopSpacingItemDecoration(private val padding: Int) : RecyclerView.ItemDecoration()
{
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    )
    {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.top = padding
        outRect.bottom = padding
    }
}
```

**6. The final part, set everything in MainActivity**
``` kotlin
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
```


  [1]: https://i.stack.imgur.com/DDpLV.png
