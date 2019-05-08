package me.yokeyword.sample.demo_wechat.ui.fragment.second

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.material.tabs.TabLayout
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_wechat.adapter.WechatPagerFragmentAdapter
import me.yokeyword.sample.demo_wechat.base.BaseMainFragment

/**
 * Created by YoKeyword on 16/6/30.
 */
class WechatSecondTabFragment : BaseMainFragment() {
    private var mTab: TabLayout? = null
    private var mToolbar: Toolbar? = null
    private var mViewPager: ViewPager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.wechat_fragment_tab_second, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        mToolbar = view.findViewById(R.id.toolbar)
        mTab = view.findViewById(R.id.tab)
        mViewPager = view.findViewById(R.id.viewPager)

        mToolbar!!.setTitle(R.string.discover)

        mTab!!.addTab(mTab!!.newTab())
        mTab!!.addTab(mTab!!.newTab())
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        mViewPager!!.adapter = WechatPagerFragmentAdapter(childFragmentManager, getString(R.string.all), getString(R.string.more))
        mTab!!.setupWithViewPager(mViewPager)
    }

    companion object {

        fun newInstance(): WechatSecondTabFragment {
            val args = Bundle()
            val fragment = WechatSecondTabFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
