package me.yokeyword.sample.demo_flow.base

import androidx.appcompat.widget.Toolbar
import me.yokeyword.sample.R

/**
 * Created by YoKeyword on 16/2/7.
 */
open class BaseBackFragment : MySupportFragment() {

    protected fun initToolbarNav(toolbar: Toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { pop() }
    }
}
