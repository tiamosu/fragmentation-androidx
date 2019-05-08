package me.yokeyword.sample.demo_zhihu.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.yokeyword.sample.demo_zhihu.ui.fragment.second.child.childpager.FirstPagerFragment
import me.yokeyword.sample.demo_zhihu.ui.fragment.second.child.childpager.OtherPagerFragment

/**
 * Created by YoKeyword on 16/6/5.
 */
class ZhihuPagerFragmentAdapter(fm: FragmentManager, vararg titles: String) : FragmentPagerAdapter(fm) {
    private val mTitles: Array<String> = titles as Array<String>

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            FirstPagerFragment.newInstance()
        } else {
            OtherPagerFragment.newInstance(mTitles[position])
        }
    }

    override fun getCount(): Int {
        return mTitles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mTitles[position]
    }
}
