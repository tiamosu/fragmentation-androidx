package me.yokeyword.sample.demo_flow.ui.fragment.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.material.tabs.TabLayout
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_flow.adapter.DiscoverFragmentAdapter
import me.yokeyword.sample.demo_flow.base.BaseMainFragment

/**
 * Created by YoKeyword on 16/2/3.
 */
class DiscoverFragment : BaseMainFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_discover, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        val mToolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val mTabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        val mViewPager = view.findViewById<ViewPager>(R.id.viewPager)

        mToolbar.setTitle(R.string.discover)
        initToolbarNav(mToolbar)

        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.recommend))
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.hot))
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.favorite))
        mViewPager.adapter = DiscoverFragmentAdapter(childFragmentManager, getString(R.string.recommend), getString(R.string.hot), getString(R.string.favorite))
        mTabLayout.setupWithViewPager(mViewPager)
    }

    companion object {

        fun newInstance(): DiscoverFragment {
            return DiscoverFragment()
        }
    }
}
