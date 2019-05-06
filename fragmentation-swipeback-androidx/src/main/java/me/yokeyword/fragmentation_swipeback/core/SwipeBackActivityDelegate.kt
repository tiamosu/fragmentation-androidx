package me.yokeyword.fragmentation_swipeback.core

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup

import androidx.fragment.app.FragmentActivity
import me.yokeyword.fragmentation.ISupportActivity
import me.yokeyword.fragmentation.SwipeBackLayout

/**
 * Created by YoKey on 17/6/29.
 */
class SwipeBackActivityDelegate(swipeBackActivity: ISwipeBackActivity) {
    private var mActivity: FragmentActivity
    private var mSwipeBackLayout: SwipeBackLayout? = null

    init {
        if (swipeBackActivity !is FragmentActivity || swipeBackActivity !is ISupportActivity) {
            throw RuntimeException("Must extends FragmentActivity/AppCompatActivity and implements ISupportActivity")
        }
        mActivity = swipeBackActivity
    }

    fun onCreate(savedInstanceState: Bundle?) {
        onActivityCreate()
    }

    fun onPostCreate(savedInstanceState: Bundle?) {
        mSwipeBackLayout?.attachToActivity(mActivity)
    }

    fun getSwipeBackLayout(): SwipeBackLayout? {
        return mSwipeBackLayout
    }

    fun setSwipeBackEnable(enable: Boolean) {
        mSwipeBackLayout?.setEnableGesture(enable)
    }

    fun setEdgeLevel(edgeLevel: SwipeBackLayout.EdgeLevel) {
        mSwipeBackLayout?.setEdgeLevel(edgeLevel)
    }

    fun setEdgeLevel(widthPixel: Int) {
        mSwipeBackLayout?.setEdgeLevel(widthPixel)
    }

    /**
     * 限制SwipeBack的条件,默认栈内Fragment数 <= 1时 , 优先滑动退出Activity , 而不是Fragment
     *
     * @return true: Activity可以滑动退出, 并且总是优先;  false: Fragment优先滑动退出
     */
    fun swipeBackPriority(): Boolean {
        return mActivity.supportFragmentManager.backStackEntryCount <= 1
    }

    private fun onActivityCreate() {
        mActivity.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mActivity.window.decorView.setBackgroundColor(Color.TRANSPARENT)
        mSwipeBackLayout = SwipeBackLayout(mActivity)
        val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mSwipeBackLayout!!.layoutParams = params
    }
}
