package me.yokeyword.sample.demo_flow.ui.fragment_swipe_back

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import me.yokeyword.sample.R

/**
 * Created by YoKeyword on 16/4/19.
 */
class FirstSwipeBackFragment : BaseSwipeBackFragment() {
    private var mToolbar: Toolbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_swipe_back_first, container, false)

        mToolbar = view.findViewById(R.id.toolbar)
        mToolbar!!.title = "SwipeBackActivity's Fragment"
        _initToolbar(mToolbar!!)

        view.findViewById<View>(R.id.btn).setOnClickListener { start(RecyclerSwipeBackFragment.newInstance()) }

        return attachToSwipeBack(view)
    }

    companion object {

        fun newInstance(): FirstSwipeBackFragment {
            val args = Bundle()
            val fragment = FirstSwipeBackFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
