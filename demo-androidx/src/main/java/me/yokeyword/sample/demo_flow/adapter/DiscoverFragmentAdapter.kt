package me.yokeyword.sample.demo_flow.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.yokeyword.sample.demo_flow.ui.fragment.discover.PagerChildFragment

/**
 * Created by YoKeyword on 16/2/5.
 */
class DiscoverFragmentAdapter(fm: FragmentManager, vararg titles: String) : FragmentPagerAdapter(fm) {
    private val mTitles: Array<String> = titles as Array<String>

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> PagerChildFragment.newInstance(0)
            1 -> PagerChildFragment.newInstance(1)
            else -> PagerChildFragment.newInstance(2)
        }
    }

    override fun getCount(): Int {
        return mTitles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mTitles[position]
    }
}
