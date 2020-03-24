package me.yokeyword.fragmentation_swipeback

import android.os.Bundle
import android.view.View

import androidx.annotation.FloatRange
import me.yokeyword.fragmentation.SupportFragment
import me.yokeyword.fragmentation.SwipeBackLayout
import me.yokeyword.fragmentation_swipeback.core.ISwipeBackFragment
import me.yokeyword.fragmentation_swipeback.core.SwipeBackFragmentDelegate

/**
 * You can also refer to [SwipeBackFragment] to implement YourSwipeBackFragment
 * (extends Fragment and impl [me.yokeyword.fragmentation.ISupportFragment])
 *
 * Created by YoKey on 16/4/19.
 */
open class SwipeBackFragment : SupportFragment(), ISwipeBackFragment {
    private val delegate = SwipeBackFragmentDelegate(apply { })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        delegate.onViewCreated(view, savedInstanceState)
    }

    override fun attachToSwipeBack(view: View): View? {
        return delegate.attachToSwipeBack(view)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        delegate.onHiddenChanged(hidden)
    }

    override fun getSwipeBackLayout(): SwipeBackLayout? {
        return delegate.getSwipeBackLayout()
    }

    /**
     * 是否可滑动
     *
     * @param enable
     */
    override fun setSwipeBackEnable(enable: Boolean) {
        delegate.setSwipeBackEnable(enable)
    }

    override fun setEdgeLevel(edgeLevel: SwipeBackLayout.EdgeLevel) {
        delegate.setEdgeLevel(edgeLevel)
    }

    override fun setEdgeLevel(widthPixel: Int) {
        delegate.setEdgeLevel(widthPixel)
    }

    /**
     * Set the offset of the parallax slip.
     */
    override fun setParallaxOffset(@FloatRange(from = 0.0, to = 1.0) offset: Float) {
        delegate.setParallaxOffset(offset)
    }

    override fun onDestroyView() {
        delegate.onDestroyView()
        super.onDestroyView()
    }
}