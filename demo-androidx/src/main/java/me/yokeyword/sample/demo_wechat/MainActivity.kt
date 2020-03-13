package me.yokeyword.sample.demo_wechat

import android.os.Bundle
import me.yokeyword.fragmentation.SupportActivity
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator
import me.yokeyword.fragmentation.anim.FragmentAnimator
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_wechat.ui.fragment.LoginFragment
import me.yokeyword.sample.demo_wechat.ui.fragment.MainFragment
import me.yokeyword.sample.demo_wechat.ui.fragment.SplashFragment

/**
 * 仿微信交互方式Demo
 * Created by YoKeyword on 16/6/30.
 *
 * 修改后，加入SplashFragment、LoginFragment。这样的跳转关系和内存重启后的判断很关键，如果处理不当，容易出现崩溃
 */
class MainActivity : SupportActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wechat_activity_main)

        //这里需要如下判断，否则可能出现这个错误 https://xuexuan.blog.csdn.net/article/details/103733622
        if (findFragment(MainFragment::class.java) == null
                && findFragment(LoginFragment::class.java) == null
                && findFragment(SplashFragment::class.java) == null) {
            loadRootFragment(R.id.fl_container, SplashFragment.newInstance())
        }
    }

    override fun onBackPressedSupport() {
        // 对于 4个类别的主Fragment内的回退back逻辑,已经在其onBackPressedSupport里各自处理了
        super.onBackPressedSupport()
    }

    override fun onCreateFragmentAnimator(): FragmentAnimator? {
        // 设置横向(和安卓4.x动画相同)
        return DefaultHorizontalAnimator()
    }
}
