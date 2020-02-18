package me.yokeyword.sample.demo_wechat.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.yokeyword.sample.demo_wechat.ui.fragment.second.FirstPagerFragment
import me.yokeyword.sample.demo_wechat.ui.fragment.second.OtherPagerFragment

/**
 * Created by YoKeyword on 16/6/5.
 */
@Suppress("UNCHECKED_CAST")
class WechatPagerFragmentAdapter(fm: FragmentManager, vararg titles: String) : FragmentPagerAdapter(fm) {
    private val mTitles: Array<String> = titles as Array<String>

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            FirstPagerFragment.newInstance()
        } else {
            OtherPagerFragment.newInstance()
        }
    }

    override fun getCount(): Int {
        return mTitles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mTitles[position]
    }
}
