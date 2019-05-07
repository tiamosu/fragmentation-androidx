package me.yokeyword.sample.demo_flow

import android.os.Bundle

import me.yokeyword.fragmentation.SwipeBackLayout
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator
import me.yokeyword.fragmentation.anim.FragmentAnimator
import me.yokeyword.fragmentation_swipeback.SwipeBackActivity
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_flow.ui.fragment_swipe_back.FirstSwipeBackFragment

/**
 * Created by YoKeyword on 16/4/19.
 */
class SwipeBackSampleActivity : SwipeBackActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe_back)

        if (findFragment(FirstSwipeBackFragment::class.java) == null) {
            loadRootFragment(R.id.fl_container, FirstSwipeBackFragment.newInstance())
        }

        getSwipeBackLayout()!!.setEdgeOrientation(SwipeBackLayout.EDGE_ALL)
    }

    /**
     * 限制SwipeBack的条件,默认栈内Fragment数 <= 1时 , 优先滑动退出Activity , 而不是Fragment
     *
     * @return true: Activity优先滑动退出;  false: Fragment优先滑动退出
     */
    override fun swipeBackPriority(): Boolean {
        return super.swipeBackPriority()
    }

    override fun onCreateFragmentAnimator(): FragmentAnimator? {
        return DefaultHorizontalAnimator()
    }
}
