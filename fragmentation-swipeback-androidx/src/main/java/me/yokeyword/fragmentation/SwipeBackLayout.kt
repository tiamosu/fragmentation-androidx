package me.yokeyword.fragmentation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import me.yokeyword.fragmentation_swipeback.R
import me.yokeyword.fragmentation_swipeback.core.ISwipeBackActivity
import java.util.*
import kotlin.math.abs

/**
 * Thx https://github.com/ikew0ng/SwipeBackLayout.
 *
 * Created by YoKey on 16/4/19.
 */
@Suppress("unused")
class SwipeBackLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private var scrollFinishThreshold = DEFAULT_SCROLL_THRESHOLD

    /**
     * Get ViewDragHelper
     */
    private var viewDragHelper: ViewDragHelper
    private var scrollPercent = 0f
    private var scrimOpacity = 0f

    private var activity: FragmentActivity? = null
    private var contentView: View? = null
    private var fragmentF: ISupportFragment? = null
    private var preFragment: Fragment? = null

    private var shadowLeft: Drawable? = null
    private var shadowRight: Drawable? = null
    private val tmpRect = Rect()

    private var edgeFlag = 0
    private var enable = true
    private var currentSwipeOrientation = 0
    private var parallaxOffset = DEFAULT_PARALLAX

    private var callOnDestroyView = false
    private var inLayout = false

    private var contentLeft = 0
    private var contentTop = 0
    private var swipeAlpha = 0.5f

    /**
     * The set of listeners to be sent events through.
     */
    private var listeners: MutableList<OnSwipeListener>? = null

    init {
        viewDragHelper = ViewDragHelper.create(this, ViewDragCallback())
        setEdgeOrientation(EDGE_LEFT)
        setShadow(R.drawable.shadow_left, EDGE_LEFT)
    }

    fun getViewDragHelper(): ViewDragHelper {
        return viewDragHelper
    }

    /**
     * 滑动中，上一个页面View的阴影透明度
     *
     * @param alpha 0.0f:无阴影, 1.0f:较重的阴影, 默认:0.5f
     */
    fun setSwipeAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        this.swipeAlpha = alpha
    }

    /**
     * Set scroll threshold, we will close the activity, when scrollPercent over
     * this value
     *
     * @param threshold
     */
    fun setScrollThresHold(@FloatRange(from = 0.0, to = 1.0) threshold: Float) {
        require(!(threshold >= 1.0f || threshold <= 0)) { "Threshold value should be between 0 and 1.0" }
        scrollFinishThreshold = threshold
    }

    fun setParallaxOffset(offset: Float) {
        this.parallaxOffset = offset
    }

    /**
     * Enable edge tracking for the selected edges of the parent view.
     * The callback's [ViewDragHelper.Callback.onEdgeTouched] and
     * [ViewDragHelper.Callback.onEdgeDragStarted] methods will only be invoked
     * for edges for which edge tracking has been enabled.
     *
     * @param orientation Combination of edge flags describing the edges to watch
     * @see EDGE_LEFT
     *
     * @see EDGE_RIGHT
     */
    fun setEdgeOrientation(@EdgeOrientation orientation: Int) {
        edgeFlag = orientation
        viewDragHelper.setEdgeTrackingEnabled(orientation)

        if (orientation == EDGE_RIGHT || orientation == EDGE_ALL) {
            setShadow(R.drawable.shadow_right, EDGE_RIGHT)
        }
    }

    /**
     * Set a drawable used for edge shadow.
     */
    @Suppress("DEPRECATION")
    fun setShadow(resId: Int, edgeFlag: Int) {
        setShadow(resources.getDrawable(resId), edgeFlag)
    }

    /**
     * Set a drawable used for edge shadow.
     */
    fun setShadow(shadow: Drawable, edgeFlag: Int) {
        if (edgeFlag and EDGE_LEFT != 0) {
            shadowLeft = shadow
        } else if (edgeFlag and EDGE_RIGHT != 0) {
            shadowRight = shadow
        }
        invalidate()
    }

    /**
     * Add a callback to be invoked when a swipe event is sent to this view.
     *
     * @param listener the swipe listener to attach to this view
     */
    fun addSwipeListener(listener: OnSwipeListener) {
        if (listeners == null) {
            listeners = ArrayList()
        }
        listeners?.add(listener)
    }

    /**
     * Removes a listener from the set of listeners
     *
     * @param listener
     */
    fun removeSwipeListener(listener: OnSwipeListener) {
        listeners?.remove(listener)
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val isDrawView = child === contentView
        val drawChild = super.drawChild(canvas, child, drawingTime)
        if (isDrawView && scrimOpacity > 0 && viewDragHelper.viewDragState != ViewDragHelper.STATE_IDLE) {
            drawShadow(canvas, child)
            drawScrim(canvas, child)
        }
        return drawChild
    }

    private fun drawShadow(canvas: Canvas, child: View) {
        val childRect = tmpRect
        child.getHitRect(childRect)

        if (currentSwipeOrientation and EDGE_LEFT != 0) {
            shadowLeft?.also {
                it.setBounds(childRect.left - it.intrinsicWidth,
                        childRect.top, childRect.left, childRect.bottom)
                it.alpha = (scrimOpacity * FULL_ALPHA).toInt()
                it.draw(canvas)
            }
        } else if (currentSwipeOrientation and EDGE_RIGHT != 0) {
            shadowRight?.also {
                it.setBounds(childRect.right, childRect.top,
                        childRect.right + it.intrinsicWidth, childRect.bottom)
                it.alpha = (scrimOpacity * FULL_ALPHA).toInt()
                it.draw(canvas)
            }
        }
    }

    private fun drawScrim(canvas: Canvas, child: View) {
        val baseAlpha = (DEFAULT_SCRIM_COLOR and -0x1000000).ushr(24)
        val alpha = (baseAlpha * scrimOpacity * swipeAlpha).toInt()
        val color = alpha shl 24

        if (currentSwipeOrientation and EDGE_LEFT != 0) {
            canvas.clipRect(0, 0, child.left, height)
        } else if (currentSwipeOrientation and EDGE_RIGHT != 0) {
            canvas.clipRect(child.right, 0, right, height)
        }
        canvas.drawColor(color)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        inLayout = true
        contentView?.apply {
            layout(contentLeft, contentTop, contentLeft + measuredWidth, contentTop + measuredHeight)
        }
        inLayout = false
    }

    override fun requestLayout() {
        if (!inLayout) {
            super.requestLayout()
        }
    }

    override fun computeScroll() {
        if (enable) {
            scrimOpacity = 1 - scrollPercent
            if (scrimOpacity < 0) {
                return
            }
            if (viewDragHelper.continueSettling(true)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
            if (preFragment?.view != null) {
                if (callOnDestroyView) {
                    preFragment?.view?.x = 0f
                    return
                }
                if (viewDragHelper.capturedView != null) {
                    val leftOffset = ((viewDragHelper.capturedView!!.left - width).toFloat()
                            * parallaxOffset * scrimOpacity).toInt()
                    preFragment?.view?.x = (if (leftOffset > 0) 0 else leftOffset).toFloat()
                }
            }
        }
    }

    /**
     * hide
     */
    fun internalCallOnDestroyView() {
        callOnDestroyView = true
    }

    fun setFragment(fragment: ISupportFragment, view: View) {
        this.fragmentF = fragment
        contentView = view
    }

    fun hiddenFragment() {
        preFragment?.view?.visibility = View.GONE
    }

    fun attachToActivity(activity: FragmentActivity) {
        this.activity = activity
        val typedArray = activity.theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
        val background = typedArray.getResourceId(0, 0)
        typedArray.recycle()

        val decor = activity.window.decorView as ViewGroup
        val decorChild = decor.getChildAt(0) as ViewGroup
        decorChild.setBackgroundResource(background)
        decor.removeView(decorChild)
        addView(decorChild)
        setContentView(decorChild)
        decor.addView(this)
    }

    fun attachToFragment(fragment: ISupportFragment, view: View) {
        addView(view)
        setFragment(fragment, view)
    }

    private fun setContentView(view: View) {
        contentView = view
    }

    fun setEnableGesture(enable: Boolean) {
        this.enable = enable
    }

    fun setEdgeLevel(edgeLevel: EdgeLevel) {
        validateEdgeLevel(-1, edgeLevel)
    }

    fun setEdgeLevel(widthPixel: Int) {
        validateEdgeLevel(widthPixel, null)
    }

    private fun validateEdgeLevel(widthPixel: Int, edgeLevel: EdgeLevel?) {
        try {
            val metrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(metrics)
            val mEdgeSize = viewDragHelper.javaClass.getDeclaredField("mEdgeSize")
            mEdgeSize.isAccessible = true
            if (widthPixel >= 0) {
                mEdgeSize.setInt(viewDragHelper, widthPixel)
            } else {
                when (edgeLevel) {
                    EdgeLevel.MAX -> mEdgeSize.setInt(viewDragHelper, metrics.widthPixels)
                    EdgeLevel.MED -> mEdgeSize.setInt(viewDragHelper, metrics.widthPixels / 2)
                    else -> mEdgeSize.setInt(viewDragHelper, (20 * metrics.density + 0.5f).toInt())
                }
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    private fun onDragFinished() {
        listeners?.also {
            for (listener in it) {
                listener.onDragStateChange(STATE_FINISHED)
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!enable) {
            return super.onInterceptTouchEvent(ev)
        }
        try {
            return viewDragHelper.shouldInterceptTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!enable) {
            return super.onTouchEvent(event)
        }
        try {
            viewDragHelper.processTouchEvent(event)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    enum class EdgeLevel {
        MAX, MIN, MED
    }

    @IntDef(EDGE_LEFT, EDGE_RIGHT, EDGE_ALL)
    @Retention(AnnotationRetention.SOURCE)
    annotation class EdgeOrientation

    interface OnSwipeListener {
        /**
         * Invoke when state change
         *
         * @param state flag to describe scroll state
         * @see .STATE_IDLE
         *
         * @see .STATE_DRAGGING
         *
         * @see .STATE_SETTLING
         *
         * @see .STATE_FINISHED
         */
        fun onDragStateChange(state: Int)

        /**
         * Invoke when edge touched
         *
         * @param oritentationEdgeFlag edge flag describing the edge being touched
         * @see .EDGE_LEFT
         *
         * @see .EDGE_RIGHT
         */
        fun onEdgeTouch(oritentationEdgeFlag: Int)

        /**
         * Invoke when scroll percent over the threshold for the first time
         *
         * @param scrollPercent scroll percent of this view
         */
        fun onDragScrolled(scrollPercent: Float)
    }

    private inner class ViewDragCallback : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            val dragEnable = viewDragHelper.isEdgeTouched(edgeFlag, pointerId)
            if (!dragEnable) {
                return false
            }

            if (viewDragHelper.isEdgeTouched(EDGE_LEFT, pointerId)) {
                currentSwipeOrientation = EDGE_LEFT
            } else if (viewDragHelper.isEdgeTouched(EDGE_RIGHT, pointerId)) {
                currentSwipeOrientation = EDGE_RIGHT
            }

            listeners?.also {
                for (listener in it) {
                    listener.onEdgeTouch(currentSwipeOrientation)
                }
            }

            if (preFragment == null) {
                val fragmentTemp = fragmentF as? Fragment
                val fragmentList = fragmentTemp?.fragmentManager?.fragments
                if (fragmentList != null && fragmentList.size > 1) {
                    val index = fragmentList.indexOf(fragmentTemp)
                    for (i in index - 1 downTo 0) {
                        val fragment = fragmentList[i]
                        if (fragment?.view != null) {
                            fragment.view?.visibility = View.VISIBLE
                            preFragment = fragment
                            break
                        }
                    }
                }
            } else {
                val preView = preFragment?.view
                if (preView != null && preView.visibility != View.VISIBLE) {
                    preView.visibility = View.VISIBLE
                }
            }
            return dragEnable
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            var ret = 0
            if (currentSwipeOrientation and EDGE_LEFT != 0) {
                ret = child.width.coerceAtMost(left.coerceAtLeast(0))
            } else if (currentSwipeOrientation and EDGE_RIGHT != 0) {
                ret = 0.coerceAtMost(left.coerceAtLeast(-child.width))
            }
            return ret
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)

            if (currentSwipeOrientation and EDGE_LEFT != 0 && contentView != null && shadowLeft != null) {
                scrollPercent = abs(left.toFloat() / (contentView!!.width + shadowLeft!!.intrinsicWidth))
            } else if (currentSwipeOrientation and EDGE_RIGHT != 0 && contentView != null && shadowRight != null) {
                scrollPercent = abs(left.toFloat() / (contentView!!.width + shadowRight!!.intrinsicWidth))
            }

            contentLeft = left
            contentTop = top
            invalidate()

            if (listeners != null
                    && viewDragHelper.viewDragState == STATE_DRAGGING
                    && scrollPercent <= 1
                    && scrollPercent > 0) {
                for (listener in listeners!!) {
                    listener.onDragScrolled(scrollPercent)
                }
            }

            if (scrollPercent > 1) {
                if (fragmentF != null) {
                    if (callOnDestroyView) {
                        return
                    }
                    if ((fragmentF as? Fragment)?.isDetached == false) {
                        onDragFinished()
                        fragmentF?.getSupportDelegate()?.popQuiet()
                    }
                } else {
                    if (activity?.isFinishing == false) {
                        onDragFinished()
                        activity?.finish()
                        activity?.overridePendingTransition(0, 0)
                    }
                }
            }
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            if (fragmentF != null) {
                return 1
            }
            return if (activity is ISwipeBackActivity && (activity as ISwipeBackActivity).swipeBackPriority()) {
                1
            } else 0
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val childWidth = releasedChild.width
            var left = 0
            val top = 0
            if (currentSwipeOrientation and EDGE_LEFT != 0 && shadowLeft != null) {
                left = if (xvel > 0 || xvel == 0f && scrollPercent > scrollFinishThreshold)
                    childWidth + shadowLeft!!.intrinsicWidth + OVERSCROLL_DISTANCE
                else
                    0
            } else if (currentSwipeOrientation and EDGE_RIGHT != 0 && shadowRight != null) {
                left = if (xvel < 0 || xvel == 0f && scrollPercent > scrollFinishThreshold)
                    -(childWidth + shadowRight!!.intrinsicWidth + OVERSCROLL_DISTANCE)
                else
                    0
            }

            viewDragHelper.settleCapturedViewAt(left, top)
            invalidate()
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)

            listeners?.also {
                for (listener in it) {
                    listener.onDragStateChange(state)
                }
            }
        }

        override fun onEdgeTouched(edgeFlags: Int, pointerId: Int) {
            super.onEdgeTouched(edgeFlags, pointerId)
            if (edgeFlag and edgeFlags != 0) {
                currentSwipeOrientation = edgeFlags
            }
        }
    }

    companion object {

        /**
         * Edge flag indicating that the left edge should be affected.
         */
        const val EDGE_LEFT = ViewDragHelper.EDGE_LEFT

        /**
         * Edge flag indicating that the right edge should be affected.
         */
        const val EDGE_RIGHT = ViewDragHelper.EDGE_RIGHT

        const val EDGE_ALL = EDGE_LEFT or EDGE_RIGHT


        /**
         * A view is not currently being dragged or animating as a result of a
         * fling/snap.
         */
        const val STATE_IDLE = ViewDragHelper.STATE_IDLE

        /**
         * A view is currently being dragged. The position is currently changing as
         * a result of user input or simulated user input.
         */
        const val STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING

        /**
         * A view is currently settling into place as a result of a fling or
         * predefined non-interactive motion.
         */
        const val STATE_SETTLING = ViewDragHelper.STATE_SETTLING

        /**
         * A view is currently drag finished.
         */
        const val STATE_FINISHED = 3

        private const val DEFAULT_SCRIM_COLOR = -0x67000000
        private const val DEFAULT_PARALLAX = 0.33f
        private const val FULL_ALPHA = 255
        private const val DEFAULT_SCROLL_THRESHOLD = 0.4f
        private const val OVERSCROLL_DISTANCE = 10
    }
}
