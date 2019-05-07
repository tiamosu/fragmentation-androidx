package me.yokeyword.sample.demo_flow.ui.fragment_swipe_back

import androidx.appcompat.widget.Toolbar
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import me.yokeyword.sample.R

/**
 * Created by YoKeyword on 16/4/21.
 */
open class BaseSwipeBackFragment : SwipeBackFragment() {

    internal fun _initToolbar(toolbar: Toolbar) {
        toolbar.title = "SwipeBackActivity's Fragment"
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { v -> context.onBackPressed() }
    }
}
