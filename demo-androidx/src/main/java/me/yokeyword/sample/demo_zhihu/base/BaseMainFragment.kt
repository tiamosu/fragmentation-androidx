package me.yokeyword.sample.demo_zhihu.base

import android.content.Context
import me.yokeyword.fragmentation.SupportFragment
import me.yokeyword.sample.demo_zhihu.ui.fragment.first.ZhihuFirstFragment

/**
 * 懒加载
 * Created by YoKeyword on 16/6/5.
 */
abstract class BaseMainFragment : SupportFragment() {
    protected var _mBackToFirstListener: OnBackToFirstListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBackToFirstListener) {
            _mBackToFirstListener = context
        } else {
            throw RuntimeException("$context must implement OnBackToFirstListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        _mBackToFirstListener = null
    }

    /**
     * 处理回退事件
     */
    override fun onBackPressedSupport(): Boolean {
        if (childFragmentManager.backStackEntryCount > 1) {
            popChild()
        } else {
            if (this is ZhihuFirstFragment) {   // 如果是 第一个Fragment 则退出app
                getContext().finish()
            } else {                                    // 如果不是,则回到第一个Fragment
                _mBackToFirstListener!!.onBackToFirstFragment()
            }
        }
        return true
    }

    interface OnBackToFirstListener {
        fun onBackToFirstFragment()
    }
}
