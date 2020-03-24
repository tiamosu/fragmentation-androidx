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
    private var fragment: Fragment
    private var supportF: ISupportFragment
    private var swipeBackLayout: SwipeBackLayout? = null

    init {
        if (swipeBackFragment !is Fragment || swipeBackFragment !is ISupportFragment) {
            throw RuntimeException("Must extends Fragment and implements ISupportFragment!")
        }
        fragment = swipeBackFragment
        supportF = swipeBackFragment
    }

    @Suppress("UNUSED_PARAMETER")
    fun onCreate(savedInstanceState: Bundle?) {
        onFragmentCreate()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (view is SwipeBackLayout) {
            val childView = view.getChildAt(0)
            supportF.getSupportDelegate().setBackground(childView)
        } else {
            supportF.getSupportDelegate().setBackground(view)
        }
    }

    fun attachToSwipeBack(view: View): View? {
        if (swipeBackLayout?.parent != null) {
            onFragmentCreate()
        }
        swipeBackLayout?.attachToFragment(supportF, view)
        return swipeBackLayout
    }

    fun setEdgeLevel(edgeLevel: SwipeBackLayout.EdgeLevel) {
        swipeBackLayout?.setEdgeLevel(edgeLevel)
    }

    fun setEdgeLevel(widthPixel: Int) {
        swipeBackLayout?.setEdgeLevel(widthPixel)
    }

    fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            swipeBackLayout?.hiddenFragment()
        }
    }

    fun getSwipeBackLayout(): SwipeBackLayout? {
        return swipeBackLayout
    }

    fun setSwipeBackEnable(enable: Boolean) {
        swipeBackLayout?.setEnableGesture(enable)
    }

    /**
     * Set the offset of the parallax slip.
     */
    fun setParallaxOffset(@FloatRange(from = 0.0, to = 1.0) offset: Float) {
        swipeBackLayout?.setParallaxOffset(offset)
    }

    fun onDestroyView() {
        swipeBackLayout?.internalCallOnDestroyView()
    }

    private fun onFragmentCreate() {
        if (fragment.context == null) return

        swipeBackLayout = SwipeBackLayout(fragment.context!!)
        val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        swipeBackLayout?.layoutParams = params
        swipeBackLayout?.setBackgroundColor(Color.TRANSPARENT)
    }
}
