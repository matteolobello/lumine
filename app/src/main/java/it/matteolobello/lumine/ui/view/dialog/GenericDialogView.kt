package it.matteolobello.lumine.ui.view.dialog

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import it.matteolobello.lumine.R
import it.matteolobello.lumine.extension.setNavigationBarColor
import it.matteolobello.lumine.extension.setStatusBarColor

open class GenericDialogView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {

        fun create(
                activity: Activity,
                title: String,
                message: String,
                positiveButtonText: String,
                negativeButtonText: String,
                onPositiveButtonClickListener: (dialogView: GenericDialogView) -> Unit,
                onNegativeButtonClickListener: (dialogView: GenericDialogView) -> Unit) {
            val dialogView = GenericDialogView(activity)
            val rootLayout = dialogView.generateWithLayoutRes(activity, R.layout.dialog)

            val titleTextView = rootLayout.findViewById<TextView>(R.id.dialogTitleTextView)
            val messageTextView = rootLayout.findViewById<TextView>(R.id.dialogMessageTextView)
            val positiveButton = rootLayout.findViewById<Button>(R.id.dialogPositiveButton)
            val negativeButton = rootLayout.findViewById<Button>(R.id.dialogNegativeButton)

            titleTextView.text = title
            messageTextView.text = message

            positiveButton.text = positiveButtonText
            negativeButton.text = negativeButtonText

            positiveButton.setOnClickListener {
                onPositiveButtonClickListener(dialogView)
            }

            negativeButton.setOnClickListener {
                onNegativeButtonClickListener(dialogView)
            }

            dialogView.setOnClickListener {
                // Nothing to do
            }

            dialogView.addInActivityRootViewAndAnimateIn(activity)
        }

        fun findDialogInActivity(activity: Activity): GenericDialogView? {
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            val dialogRootView = rootView.findViewById<CardView>(R.id.dialogRootView) ?: return null

            val dialog = dialogRootView.parent
            if (dialog != null) {
                return dialog as GenericDialogView
            }

            return null
        }
    }

    private val defaultStatusBarColor = (context as? Activity)?.window?.statusBarColor
            ?: Color.BLACK
    private val defaultNavigationBarColor = (context as? Activity)?.window?.navigationBarColor
            ?: Color.BLACK

    fun generateWithLayoutRes(activity: Activity, @LayoutRes layoutRes: Int): View {
        val dimColor = ContextCompat.getColor(activity, R.color.backgroundDim)
        val dimColorNoTransparency = ContextCompat.getColor(activity, R.color.backgroundDimNoTransparency)

        setBackgroundColor(dimColor)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        elevation = Float.MAX_VALUE

        activity.setStatusBarColor(dimColorNoTransparency)
        activity.setNavigationBarColor(dimColorNoTransparency)

        return LayoutInflater.from(activity).inflate(layoutRes, this)
    }

    fun addInActivityRootViewAndAnimateIn(activity: Activity) {
        // Double check if the View is already in layout
        activity.findViewById<ViewGroup>(android.R.id.content).removeView(this)

        activity.findViewById<ViewGroup>(android.R.id.content).addView(this)
        animateIn()
    }

    fun dismiss() {
        animateOut {
            with(context as? Activity) {
                this?.findViewById<ViewGroup>(android.R.id.content)?.removeView(this@GenericDialogView)

                this?.window?.statusBarColor = defaultStatusBarColor
                this?.window?.navigationBarColor = defaultNavigationBarColor
            }
        }
    }

    private fun animateIn(callback: () -> Unit = {}) {
        alpha = 0f

        animate()
                .alpha(1f)
                .setDuration(200)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animator: Animator?) {
                    }

                    override fun onAnimationEnd(animator: Animator?) {
                        callback()
                    }

                    override fun onAnimationCancel(animator: Animator?) {
                    }

                    override fun onAnimationStart(animator: Animator?) {
                    }
                })
                .start()
    }

    private fun animateOut(callback: () -> Unit = {}) {
        alpha = 1f

        animate()
                .alpha(0f)
                .setDuration(200)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animator: Animator?) {
                    }

                    override fun onAnimationEnd(animator: Animator?) {
                        callback()
                    }

                    override fun onAnimationCancel(animator: Animator?) {
                    }

                    override fun onAnimationStart(animator: Animator?) {
                    }
                })
                .start()
    }
}