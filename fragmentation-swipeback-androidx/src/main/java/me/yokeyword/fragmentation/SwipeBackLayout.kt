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
import androidx.fragment.app.FragmentationMagician
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
class SwipeBackLayout @JvmOverloads constructor(private val mContext: Context,
                                                attrs: AttributeSet? = null,
                                                defStyleAttr: Int = 0) : FrameLayout(mContext, attrs, defStyleAttr) {

    private var mScrollFinishThreshold = DEFAULT_SCROLL_THRESHOLD

    /**
     * Get ViewDragHelper
     */
    private var mViewDragHelper: ViewDragHelper
    private var mScrollPercent: Float = 0.toFloat()
    private var mScrimOpacity: Float = 0.toFloat()

    private var mActivity: FragmentActivity? = null
    private var mContentView: View? = null
    private var mFragment: ISupportFragment? = null
    private var mPreFragment: Fragment? = null

    private var mShadowLeft: Drawable? = null
    private var mShadowRight: Drawable? = null
    private val mTmpRect = Rect()

    private var mEdgeFlag: Int = 0
    private var mEnable = true
    private var mCurrentSwipeOrientation: Int = 0
    private var mParallaxOffset = DEFAULT_PARALLAX

    private var mCallOnDestroyView: Boolean = false

    private var mInLayout: Boolean = false

    private var mContentLeft: Int = 0
    private var mContentTop: Int = 0
    private var mSwipeAlpha = 0.5f

    /**
     * The set of listeners to be sent events through.
     */
    private var mListeners: MutableList<OnSwipeListener>? = null

    init {
        mViewDragHelper = ViewDragHelper.create(this, ViewDragCallback())
        setEdgeOrientation(EDGE_LEFT)
        setShadow(R.drawable.shadow_left, EDGE_LEFT)
    }

    fun getViewDragHelper(): ViewDragHelper {
        return mViewDragHelper
    }

    /**
     * 滑动中，上一个页面View的阴影透明度
     *
     * @param alpha 0.0f:无阴影, 1.0f:较重的阴影, 默认:0.5f
     */
    fun setSwipeAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        this.mSwipeAlpha = alpha
    }

    /**
     * Set scroll threshold, we will close the activity, when scrollPercent over
     * this value
     *
     * @param threshold
     */
    fun setScrollThresHold(@FloatRange(from = 0.0, to = 1.0) threshold: Float) {
        require(!(threshold >= 1.0f || threshold <= 0)) { "Threshold value should be between 0 and 1.0" }
        mScrollFinishThreshold = threshold
    }

    fun setParallaxOffset(offset: Float) {
        this.mParallaxOffset = offset
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
        mEdgeFlag = orientation
        mViewDragHelper.setEdgeTrackingEnabled(orientation)

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
            mShadowLeft = shadow
        } else if (edgeFlag and EDGE_RIGHT != 0) {
            mShadowRight = shadow
        }
        invalidate()
    }

    /**
     * Add a callback to be invoked when a swipe event is sent to this view.
     *
     * @param listener the swipe listener to attach to this view
     */
    fun addSwipeListener(listener: OnSwipeListener) {
        if (mListeners == null) {
            mListeners = ArrayList()
        }
        mListeners!!.add(listener)
    }

    /**
     * Removes a listener from the set of listeners
     *
     * @param listener
     */
    fun removeSwipeListener(listener: OnSwipeListener) {
        mListeners?.remove(listener)
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val isDrawView = child === mContentView
        val drawChild = super.drawChild(canvas, child, drawingTime)
        if (isDrawView && mScrimOpacity > 0 && mViewDragHelper.viewDragState != ViewDragHelper.STATE_IDLE) {
            drawShadow(canvas, child)
            drawScrim(canvas, child)
        }
        return drawChild
    }

    private fun drawShadow(canvas: Canvas, child: View) {
        val childRect = mTmpRect
        child.getHitRect(childRect)

        if (mCurrentSwipeOrientation and EDGE_LEFT != 0) {
            mShadowLeft!!.setBounds(childRect.left - mShadowLeft!!.intrinsicWidth,
                    childRect.top, childRect.left, childRect.bottom)
            mShadowLeft!!.alpha = (mScrimOpacity * FULL_ALPHA).toInt()
            mShadowLeft!!.draw(canvas)
        } else if (mCurrentSwipeOrientation and EDGE_RIGHT != 0) {
            mShadowRight!!.setBounds(childRect.right, childRect.top,
                    childRect.right + mShadowRight!!.intrinsicWidth, childRect.bottom)
            mShadowRight!!.alpha = (mScrimOpacity * FULL_ALPHA).toInt()
            mShadowRight!!.draw(canvas)
        }
    }

    private fun drawScrim(canvas: Canvas, child: View) {
        val baseAlpha = (DEFAULT_SCRIM_COLOR and -0x1000000).ushr(24)
        val alpha = (baseAlpha * mScrimOpacity * mSwipeAlpha).toInt()
        val color = alpha shl 24

        if (mCurrentSwipeOrientation and EDGE_LEFT != 0) {
            canvas.clipRect(0, 0, child.left, height)
        } else if (mCurrentSwipeOrientation and EDGE_RIGHT != 0) {
            canvas.clipRect(child.right, 0, right, height)
        }
        canvas.drawColor(color)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        mInLayout = true
        mContentView?.apply {
            layout(mContentLeft, mContentTop, mContentLeft + measuredWidth, mContentTop + measuredHeight)
        }
        mInLayout = false
    }

    override fun requestLayout() {
        if (!mInLayout) {
            super.requestLayout()
        }
    }

    override fun computeScroll() {
        mScrimOpacity = 1 - mScrollPercent
        if (mScrimOpacity < 0) {
            return
        }
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
        if (mPreFragment?.view != null) {
            if (mCallOnDestroyView) {
                mPreFragment!!.view!!.x = 0f
                return
            }
            if (mViewDragHelper.capturedView != null) {
                val leftOffset = ((mViewDragHelper.capturedView!!.left - width).toFloat()
                        * mParallaxOffset * mScrimOpacity).toInt()
                mPreFragment!!.view!!.x = (if (leftOffset > 0) 0 else leftOffset).toFloat()
            }
        }
    }

    /**
     * hide
     */
    fun internalCallOnDestroyView() {
        mCallOnDestroyView = true
    }

    fun setFragment(fragment: ISupportFragment, view: View) {
        this.mFragment = fragment
        mContentView = view
    }

    fun hiddenFragment() {
        if (mPreFragment?.view != null) {
            mPreFragment!!.view!!.visibility = View.GONE
        }
    }

    fun attachToActivity(activity: FragmentActivity) {
        mActivity = activity
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
        mContentView = view
    }

    fun setEnableGesture(enable: Boolean) {
        mEnable = enable
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
            val windowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(metrics)
            val mEdgeSize = mViewDragHelper.javaClass.getDeclaredField("mEdgeSize")
            mEdgeSize.isAccessible = true
            if (widthPixel >= 0) {
                mEdgeSize.setInt(mViewDragHelper, widthPixel)
            } else {
                when (edgeLevel) {
                    EdgeLevel.MAX -> mEdgeSize.setInt(mViewDragHelper, metrics.widthPixels)
                    EdgeLevel.MED -> mEdgeSize.setInt(mViewDragHelper, metrics.widthPixels / 2)
                    else -> mEdgeSize.setInt(mViewDragHelper, (20 * metrics.density + 0.5f).toInt())
                }
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    private fun onDragFinished() {
        if (mListeners != null) {
            for (listener in mListeners!!) {
                listener.onDragStateChange(STATE_FINISHED)
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!mEnable) {
            return super.onInterceptTouchEvent(ev)
        }
        try {
            return mViewDragHelper.shouldInterceptTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!mEnable) {
            return super.onTouchEvent(event)
        }
        try {
            mViewDragHelper.processTouchEvent(event)
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
            val dragEnable = mViewDragHelper.isEdgeTouched(mEdgeFlag, pointerId)
            if (!dragEnable) {
                return false
            }

            if (mViewDragHelper.isEdgeTouched(EDGE_LEFT, pointerId)) {
                mCurrentSwipeOrientation = EDGE_LEFT
            } else if (mViewDragHelper.isEdgeTouched(EDGE_RIGHT, pointerId)) {
                mCurrentSwipeOrientation = EDGE_RIGHT
            }

            if (mListeners != null) {
                for (listener in mListeners!!) {
                    listener.onEdgeTouch(mCurrentSwipeOrientation)
                }
            }

            if (mPreFragment == null) {
                if (mFragment != null) {
                    val fragmentTemp = mFragment as Fragment
                    val fragmentList = FragmentationMagician.getActiveFragments(
                            fragmentTemp.fragmentManager)
                    if (fragmentList != null && fragmentList.size > 1) {
                        val index = fragmentList.indexOf(fragmentTemp)
                        for (i in index - 1 downTo 0) {
                            val fragment = fragmentList[i]
                            if (fragment?.view != null) {
                                fragment.view!!.visibility = View.VISIBLE
                                mPreFragment = fragment
                                break
                            }
                        }
                    }
                }
            } else {
                val preView = mPreFragment!!.view
                if (preView != null && preView.visibility != View.VISIBLE) {
                    preView.visibility = View.VISIBLE
                }
            }
            return dragEnable
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            var ret = 0
            if (mCurrentSwipeOrientation and EDGE_LEFT != 0) {
                ret = child.width.coerceAtMost(left.coerceAtLeast(0))
            } else if (mCurrentSwipeOrientation and EDGE_RIGHT != 0) {
                ret = 0.coerceAtMost(left.coerceAtLeast(-child.width))
            }
            return ret
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)

            if (mCurrentSwipeOrientation and EDGE_LEFT != 0) {
                mScrollPercent = abs(left.toFloat() / (mContentView!!.width + mShadowLeft!!.intrinsicWidth))
            } else if (mCurrentSwipeOrientation and EDGE_RIGHT != 0) {
                mScrollPercent = abs(left.toFloat() / (mContentView!!.width + mShadowRight!!.intrinsicWidth))
            }
            mContentLeft = left
            mContentTop = top
            invalidate()

            if (mListeners != null
                    && mViewDragHelper.viewDragState == STATE_DRAGGING
                    && mScrollPercent <= 1
                    && mScrollPercent > 0) {
                for (listener in mListeners!!) {
                    listener.onDragScrolled(mScrollPercent)
                }
            }

            if (mScrollPercent > 1) {
                if (mFragment != null) {
                    if (mCallOnDestroyView) {
                        return
                    }
                    if (!(mFragment as Fragment).isDetached) {
                        onDragFinished()
                        mFragment!!.getSupportDelegate().popQuiet()
                    }
                } else {
                    if (!mActivity!!.isFinishing) {
                        onDragFinished()
                        mActivity!!.finish()
                        mActivity!!.overridePendingTransition(0, 0)
                    }
                }
            }
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            if (mFragment != null) {
                return 1
            }
            return if (mActivity is ISwipeBackActivity && (mActivity as ISwipeBackActivity).swipeBackPriority()) {
                1
            } else 0
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val childWidth = releasedChild.width
            var left = 0
            val top = 0
            if (mCurrentSwipeOrientation and EDGE_LEFT != 0) {
                left = if (xvel > 0 || xvel == 0f && mScrollPercent > mScrollFinishThreshold)
                    childWidth + mShadowLeft!!.intrinsicWidth + OVERSCROLL_DISTANCE
                else
                    0
            } else if (mCurrentSwipeOrientation and EDGE_RIGHT != 0) {
                left = if (xvel < 0 || xvel == 0f && mScrollPercent > mScrollFinishThreshold)
                    -(childWidth + mShadowRight!!.intrinsicWidth + OVERSCROLL_DISTANCE)
                else
                    0
            }

            mViewDragHelper.settleCapturedViewAt(left, top)
            invalidate()
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            if (mListeners != null) {
                for (listener in mListeners!!) {
                    listener.onDragStateChange(state)
                }
            }
        }

        override fun onEdgeTouched(edgeFlags: Int, pointerId: Int) {
            super.onEdgeTouched(edgeFlags, pointerId)
            if (mEdgeFlag and edgeFlags != 0) {
                mCurrentSwipeOrientation = edgeFlags
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
