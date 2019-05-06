package me.yokeyword.fragmentation_swipeback.core

import me.yokeyword.fragmentation.SwipeBackLayout

/**
 * Created by YoKey on 17/6/29.
 */
interface ISwipeBackActivity {

    fun getSwipeBackLayout(): SwipeBackLayout

    fun setSwipeBackEnable(enable: Boolean)

    fun setEdgeLevel(edgeLevel: SwipeBackLayout.EdgeLevel)

    fun setEdgeLevel(widthPixel: Int)

    /**
     * 限制SwipeBack的条件,默认栈内Fragment数 <= 1时 , 优先滑动退出Activity , 而不是Fragment
     *
     * @return true: Activity可以滑动退出, 并且总是优先;  false: Fragment优先滑动退出
     */
    fun swipeBackPriority(): Boolean
}
