package me.yokeyword.sample.demo_zhihu

import android.os.Bundle
import androidx.core.app.ActivityCompat
import me.yokeyword.eventbusactivityscope.EventBusActivityScope
import me.yokeyword.fragmentation.SupportActivity
import me.yokeyword.fragmentation.SupportFragment
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_zhihu.base.BaseMainFragment
import me.yokeyword.sample.demo_zhihu.event.TabSelectedEvent
import me.yokeyword.sample.demo_zhihu.ui.fragment.first.ZhihuFirstFragment
import me.yokeyword.sample.demo_zhihu.ui.fragment.first.child.FirstHomeFragment
import me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.ZhihuFourthFragment
import me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.child.MeFragment
import me.yokeyword.sample.demo_zhihu.ui.fragment.second.ZhihuSecondFragment
import me.yokeyword.sample.demo_zhihu.ui.fragment.second.child.ViewPagerFragment
import me.yokeyword.sample.demo_zhihu.ui.fragment.third.ZhihuThirdFragment
import me.yokeyword.sample.demo_zhihu.ui.fragment.third.child.ShopFragment
import me.yokeyword.sample.demo_zhihu.ui.view.BottomBar
import me.yokeyword.sample.demo_zhihu.ui.view.BottomBarTab

/**
 * 类知乎 复杂嵌套Demo tip: 多使用右上角的"查看栈视图"
 * Created by YoKeyword on 16/6/2.
 */
class MainActivity : SupportActivity(), BaseMainFragment.OnBackToFirstListener {
    private val mFragments = arrayOfNulls<SupportFragment>(4)
    private var mBottomBar: BottomBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.zhihu_activity_main)

        val firstFragment = findFragment(ZhihuFirstFragment::class.java)
        if (firstFragment == null) {
            mFragments[FIRST] = ZhihuFirstFragment.newInstance()
            mFragments[SECOND] = ZhihuSecondFragment.newInstance()
            mFragments[THIRD] = ZhihuThirdFragment.newInstance()
            mFragments[FOURTH] = ZhihuFourthFragment.newInstance()

            loadMultipleRootFragment(R.id.fl_container, FIRST, mFragments)
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题

            // 这里我们需要拿到mFragments的引用
            mFragments[FIRST] = firstFragment
            mFragments[SECOND] = findFragment(ZhihuSecondFragment::class.java)
            mFragments[THIRD] = findFragment(ZhihuThirdFragment::class.java)
            mFragments[FOURTH] = findFragment(ZhihuFourthFragment::class.java)
        }

        initView()
    }

    private fun initView() {
        mBottomBar = findViewById(R.id.bottomBar)

        mBottomBar!!.addItem(BottomBarTab(this, icon = R.drawable.ic_home_white_24dp))
                .addItem(BottomBarTab(this, icon = R.drawable.ic_discover_white_24dp))
                .addItem(BottomBarTab(this, icon = R.drawable.ic_message_white_24dp))
                .addItem(BottomBarTab(this, icon = R.drawable.ic_account_circle_white_24dp))

        mBottomBar!!.setOnTabSelectedListener(object : BottomBar.OnTabSelectedListener {
            override fun onTabSelected(position: Int, prePosition: Int) {
                showHideFragment(mFragments[position]!!, mFragments[prePosition]!!)
            }

            override fun onTabUnselected(position: Int) {
            }

            override fun onTabReselected(position: Int) {
                val currentFragment = mFragments[position]
                val count = currentFragment?.childFragmentManager?.backStackEntryCount ?: 0

                // 如果不在该类别Fragment的主页,则回到主页;
                if (count > 1) {
                    when (currentFragment) {
                        is ZhihuFirstFragment -> currentFragment.popToChild(FirstHomeFragment::class.java, false)
                        is ZhihuSecondFragment -> currentFragment.popToChild(ViewPagerFragment::class.java, false)
                        is ZhihuThirdFragment -> currentFragment.popToChild(ShopFragment::class.java, false)
                        is ZhihuFourthFragment -> currentFragment.popToChild(MeFragment::class.java, false)
                    }
                    return
                }

                // 这里推荐使用EventBus来实现 -> 解耦
                if (count == 1) {
                    // 在FirstPagerFragment中接收, 因为是嵌套的孙子Fragment 所以用EventBus比较方便
                    // 主要为了交互: 重选tab 如果列表不在顶部则移动到顶部,如果已经在顶部,则刷新
                    EventBusActivityScope.getDefault(this@MainActivity).post(TabSelectedEvent(position))
                }
            }
        })
    }

    override fun onBackPressedSupport() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            pop()
        } else {
            ActivityCompat.finishAfterTransition(this)
        }
    }

    override fun onBackToFirstFragment() {
        mBottomBar!!.setCurrentItem(0)
    }

    companion object {
        const val FIRST = 0
        const val SECOND = 1
        const val THIRD = 2
        const val FOURTH = 3
    }

    /*
     * 这里暂没实现,忽略
     */
    //    @Subscribe
    //    public void onHiddenBottombarEvent(boolean hidden) {
    //        if (hidden) {
    //            mBottomBar.hide();
    //        } else {
    //            mBottomBar.show();
    //        }
    //    }
}
