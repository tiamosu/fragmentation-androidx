package me.yokeyword.fragmentation_swipeback.core

import android.view.View

import androidx.annotation.FloatRange
import me.yokeyword.fragmentation.SwipeBackLayout

/**
 * Created by YoKey on 17/6/29.
 */
interface ISwipeBackFragment {

    fun attachToSwipeBack(view: View): View

    fun getSwipeBackLayout(): SwipeBackLayout

    fun setSwipeBackEnable(enable: Boolean)

    fun setEdgeLevel(edgeLevel: SwipeBackLayout.EdgeLevel)

    fun setEdgeLevel(widthPixel: Int)

    /**
     * Set the offset of the parallax slip.
     */
    fun setParallaxOffset(@FloatRange(from = 0.0, to = 1.0) offset: Float)
}
