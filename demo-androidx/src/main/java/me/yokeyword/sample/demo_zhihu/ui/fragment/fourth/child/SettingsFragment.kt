package me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.child

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import me.yokeyword.fragmentation.SupportFragment
import me.yokeyword.sample.R

/**
 * Created by YoKeyword on 16/6/6.
 */
class SettingsFragment : SupportFragment() {
    private var mToolbar: Toolbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.zhihu_fragment_fourth_settings, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        mToolbar = view.findViewById(R.id.toolbarSettings)

        mToolbar!!.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        mToolbar!!.setNavigationOnClickListener { context.onBackPressed() }
    }

    override fun onBackPressedSupport(): Boolean {
        pop()
        return true
    }

    companion object {

        fun newInstance(): SettingsFragment {
            val args = Bundle()
            val fragment = SettingsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
