package me.yokeyword.sample.demo_flow.base

import android.content.Context
import androidx.appcompat.widget.Toolbar
import me.yokeyword.sample.R

/**
 * Created by YoKeyword on 16/2/3.
 */
open class BaseMainFragment : MySupportFragment() {

    private var mOpenDraweListener: OnFragmentOpenDrawerListener? = null

    @JvmOverloads
    protected fun initToolbarNav(toolbar: Toolbar, isHome: Boolean = false) {
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        toolbar.setNavigationOnClickListener { v ->
            mOpenDraweListener?.onOpenDrawer()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentOpenDrawerListener) {
            mOpenDraweListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mOpenDraweListener = null
    }

    interface OnFragmentOpenDrawerListener {
        fun onOpenDrawer()
    }
}
