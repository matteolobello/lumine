package it.matteolobello.lumine.ui.fragment.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import it.matteolobello.lumine.R
import it.matteolobello.lumine.extension.dpToPx
import it.matteolobello.lumine.extension.isRunningAtLeastM
import it.matteolobello.lumine.ui.activity.HomeActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_notes.*
import kotlin.math.abs

abstract class HomeFragment : Fragment() {

    abstract fun getLayoutRes(): Int

    abstract fun setupUi(rootView: View)

    val homeActivity: HomeActivity by lazy { activity as HomeActivity }

    private var isAnimatingBottomNavigationHide = false
    private var isAnimatingAlphaIn = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            LayoutInflater.from(context).inflate(getLayoutRes(), container, false)!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupScrollingView(recyclerView: RecyclerView) {
        recyclerView.apply {
            clipToPadding = false
            setPadding(paddingLeft, paddingTop, paddingRight,
                    resources.getDimensionPixelSize(R.dimen.bottomNavigationBarHeight) +
                            dpToPx(56f) + // Fab Height
                            dpToPx(16f))  // Fab Margin
        }
        recyclerView.setOnTouchListener { _, motionEvent ->
            with(homeActivity) {
                if (motionEvent.action == MotionEvent.ACTION_UP
                        || !recyclerView.canScrollVertically(1)) {
                    if (isAnimatingBottomNavigationHide) {
                        floatingActionButton.show()

                        bottomNavigation.animate()
                                .translationY(0f)
                                .setDuration(100)
                                .setListener(null)
                                .start()

                        isAnimatingBottomNavigationHide = false
                    }
                } else if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                    if (!isAnimatingBottomNavigationHide) {
                        floatingActionButton.hide()

                        bottomNavigation.animate()
                                .translationY(bottomNavigation.height.toFloat())
                                .setDuration(100)
                                .setListener(null)
                                .start()

                        isAnimatingBottomNavigationHide = true
                    }
                }
            }

            false
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                with(homeActivity) {
                    val newAppBarTranslationY = -notesRecyclerView.computeVerticalScrollOffset().toFloat()
                    appBarLayout.translationY = newAppBarTranslationY

                    if (isRunningAtLeastM()) {
                        if (abs(newAppBarTranslationY) == 0f) {
                            if (isAnimatingAlphaIn) {
                                statusBarShadowView.animate()
                                        .alpha(0f)
                                        .setDuration(200)
                                        .start()

                                isAnimatingAlphaIn = false
                            }
                        } else {
                            if (!isAnimatingAlphaIn) {
                                statusBarShadowView.animate()
                                        .alpha(1f)
                                        .setDuration(200)
                                        .start()

                                isAnimatingAlphaIn = true
                            }
                        }
                    }
                }
            }
        })
    }
}