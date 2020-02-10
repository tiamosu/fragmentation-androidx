package me.yokeyword.fragmentation_swipeback.core

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup

import androidx.annotation.FloatRange
import androidx.fragment.app.Fragment
import me.yokeyword.fragmentation.ISupportFragment
import me.yokeyword.fragmentation.SwipeBackLayout

/**
 * Created by YoKey on 17/6/29.
 */
class SwipeBackFragmentDelegate(swipeBackFragment: ISwipeBackFragment) {
    private var mFragment: Fragment
    private var mSupport: ISupportFragment
    private var mSwipeBackLayout: SwipeBackLayout? = null

    init {
        if (swipeBackFragment !is Fragment || swipeBackFragment !is ISupportFragment) {
            throw RuntimeException("Must extends Fragment and implements ISupportFragment!")
        }
        mFragment = swipeBackFragment
        mSupport = swipeBackFragment
    }

    @Suppress("UNUSED_PARAMETER")
    fun onCreate(savedInstanceState: Bundle?) {
        onFragmentCreate()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (view is SwipeBackLayout) {
            val childView = view.getChildAt(0)
            mSupport.getSupportDelegate().setBackground(childView)
        } else {
            mSupport.getSupportDelegate().setBackground(view)
        }
    }

    fun attachToSwipeBack(view: View): View? {
        if (mSwipeBackLayout?.parent != null) {
            onFragmentCreate()
        }
        mSwipeBackLayout?.attachToFragment(mSupport, view)
        return mSwipeBackLayout
    }

    fun setEdgeLevel(edgeLevel: SwipeBackLayout.EdgeLevel) {
        mSwipeBackLayout?.setEdgeLevel(edgeLevel)
    }

    fun setEdgeLevel(widthPixel: Int) {
        mSwipeBackLayout?.setEdgeLevel(widthPixel)
    }

    fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            mSwipeBackLayout?.hiddenFragment()
        }
    }

    fun getSwipeBackLayout(): SwipeBackLayout? {
        return mSwipeBackLayout
    }

    fun setSwipeBackEnable(enable: Boolean) {
        mSwipeBackLayout?.setEnableGesture(enable)
    }

    /**
     * Set the offset of the parallax slip.
     */
    fun setParallaxOffset(@FloatRange(from = 0.0, to = 1.0) offset: Float) {
        mSwipeBackLayout?.setParallaxOffset(offset)
    }

    fun onDestroyView() {
        mSwipeBackLayout?.internalCallOnDestroyView()
    }

    private fun onFragmentCreate() {
        if (mFragment.context == null) {
            return
        }
        mSwipeBackLayout = SwipeBackLayout(mFragment.context!!)
        val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mSwipeBackLayout!!.layoutParams = params
        mSwipeBackLayout!!.setBackgroundColor(Color.TRANSPARENT)
    }
}
