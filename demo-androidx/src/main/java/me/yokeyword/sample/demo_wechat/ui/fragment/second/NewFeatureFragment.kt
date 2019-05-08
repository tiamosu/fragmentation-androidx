package me.yokeyword.sample.demo_wechat.ui.fragment.second

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_wechat.base.BaseBackFragment
import me.yokeyword.sample.demo_wechat.ui.fragment.CycleFragment

/**
 * 该类是展示 1.0 版本新特性：拓展事务 extraTransaction()
 *
 *
 * Created by YoKey on 16/11/25.
 */
class NewFeatureFragment : BaseBackFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.wechat_fragment_new_feature, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        initToolbarNav(toolbar)
        toolbar.title = "NewFeatures"

        // 自定义动画启动一个Fragment，并且不隐藏自己
        view.findViewById<View>(R.id.btn_start_dont_hide).setOnClickListener {
            extraTransaction()
                    .setCustomAnimations(R.anim.v_fragment_enter, 0, 0, R.anim.v_fragment_exit)
                    .startDontHideSelf(ViewFragment.newInstance())
        }

        // 自定义动画启动一个Fragment
        view.findViewById<View>(R.id.btn_start).setOnClickListener {
            extraTransaction()
                    //                        .setTag("CustomTag")
                    //                        . ...
                    .setCustomAnimations(R.anim.v_fragment_enter, R.anim.v_fragment_pop_exit,
                            R.anim.v_fragment_pop_enter, R.anim.v_fragment_exit)
                    .start(CycleFragment.newInstance(0))
        }

        return attachToSwipeBack(view)
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        // 懒加载
        // 同级Fragment场景、ViewPager场景均适用
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        // 当对用户可见时 回调
        // 不管是 父Fragment还是子Fragment 都有效！
    }

    override fun onSupportInvisible() {
        super.onSupportInvisible()
        // 当对用户不可见时 回调
        // 不管是 父Fragment还是子Fragment 都有效！
    }

    companion object {
        fun newInstance(): NewFeatureFragment {
            return NewFeatureFragment()
        }
    }
}
