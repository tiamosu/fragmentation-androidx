package me.yokeyword.sample.demo_zhihu.ui.fragment.fourth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_zhihu.base.BaseMainFragment
import me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.child.AvatarFragment
import me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.child.MeFragment

/**
 * Created by YoKeyword on 16/6/3.
 */
class ZhihuFourthFragment : BaseMainFragment() {
    private var mToolbar: Toolbar? = null
    private var mView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.zhihu_fragment_fourth, container, false)
        return mView
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        if (findChildFragment(AvatarFragment::class.java) == null) {
            loadFragment()
        }

        mToolbar = mView!!.findViewById(R.id.toolbar)
        mToolbar!!.setTitle(R.string.me)
    }

    private fun loadFragment() {
        loadRootFragment(R.id.fl_fourth_container_upper, AvatarFragment.newInstance())
        loadRootFragment(R.id.fl_fourth_container_lower, MeFragment.newInstance())
    }

    fun onBackToFirstFragment() {
        _mBackToFirstListener!!.onBackToFirstFragment()
    }

    companion object {

        fun newInstance(): ZhihuFourthFragment {
            val args = Bundle()
            val fragment = ZhihuFourthFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
