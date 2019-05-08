package me.yokeyword.sample.demo_zhihu.ui.fragment.first

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_zhihu.base.BaseMainFragment
import me.yokeyword.sample.demo_zhihu.ui.fragment.first.child.FirstHomeFragment

/**
 * Created by YoKeyword on 16/6/3.
 */
class ZhihuFirstFragment : BaseMainFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.zhihu_fragment_first, container, false)
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        if (findChildFragment(FirstHomeFragment::class.java) == null) {
            loadRootFragment(R.id.fl_first_container, FirstHomeFragment.newInstance())
        }
    }

    companion object {

        fun newInstance(): ZhihuFirstFragment {
            val args = Bundle()
            val fragment = ZhihuFirstFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
