package me.yokeyword.sample.demo_zhihu.ui.fragment.second.child.childpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.yokeyword.fragmentation.SupportFragment
import me.yokeyword.sample.R

/**
 * Created by YoKeyword on 16/6/5.
 */
class OtherPagerFragment : SupportFragment() {

    private var mTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mTitle = arguments!!.getString(ARG_TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.zhihu_fragment_second_pager_other, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        tvTitle.text = mTitle
    }

    companion object {
        private const val ARG_TYPE = "arg_type"

        fun newInstance(title: String): OtherPagerFragment {
            val args = Bundle()
            args.putString(ARG_TYPE, title)
            val fragment = OtherPagerFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
