package it.matteolobello.lumine.ui.view.indicator

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import it.matteolobello.lumine.R

class ScrollIndicatorView : LinearLayout {

    private var viewPager: ViewPager? = null
    private var layoutManager: LinearLayoutManager? = null

    private var selectedColor = Color.WHITE
    private var defaultColor = Color.WHITE

    private var totalItemsCount = -1

    constructor(context: Context) : super(context)

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun attach(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter
                ?: throw NullPointerException("Set the RecyclerView adapter first")
        totalItemsCount = adapter.itemCount

        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                ?: throw RuntimeException("RecyclerView LayoutManager is NOT a LinearLayoutManager")

        this.layoutManager = layoutManager

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    handleIndicators()
                }
            }
        })

        init()
    }

    fun attach(viewPager: ViewPager) {
        val adapter = viewPager.adapter
                ?: throw NullPointerException("Set the ViewPager adapter first")
        totalItemsCount = adapter.count

        this.viewPager = viewPager

        this.viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                handleIndicators()
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {}
        })

        init()
    }

    fun setCurrentPosition(currentPosition: Int) {
        for (i in 0 until childCount) {
            val isCurrentItem = i == currentPosition
            toggleIndicator(getChildAt(i) as ImageView, isCurrentItem)
        }
    }

    fun setSelectedItemColor(color: Int) {
        selectedColor = color

        invalidate()
    }

    fun setDefaultItemColor(color: Int) {
        defaultColor = color

        invalidate()
    }

    private fun init() {
        post {
            orientation = LinearLayout.HORIZONTAL

            clearIndicators()
            createIndicators()
            handleIndicators()
        }
    }

    private fun clearIndicators() {
        removeAllViews()
    }

    private fun createIndicators() {
        for (i in 0 until totalItemsCount) {
            val circleIndicator = LayoutInflater.from(context)
                    .inflate(R.layout.circle_indicator, this, false) as ImageView

            addView(circleIndicator)
        }
    }

    private fun handleIndicators() {
        for (i in 0 until childCount) {
            val isCurrentItem = i == if (layoutManager != null)
                layoutManager!!.findFirstVisibleItemPosition()
            else viewPager!!.currentItem
            toggleIndicator(getChildAt(i) as ImageView, isCurrentItem)
        }
    }

    private fun toggleIndicator(imageView: ImageView, on: Boolean) {
        imageView.colorFilter = PorterDuffColorFilter(if (on) selectedColor else defaultColor, PorterDuff.Mode.SRC_ATOP)
        imageView.alpha = if (on) 1.0f else 0.54f
        imageView.animate()
                .scaleX(if (on) 1.5f else 1.0f)
                .scaleY(if (on) 1.5f else 1.0f)
                .setDuration(100)
                .start()
    }
}