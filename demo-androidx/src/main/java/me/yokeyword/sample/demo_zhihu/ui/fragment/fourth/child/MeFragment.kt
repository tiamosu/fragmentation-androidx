package me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.child

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.yokeyword.fragmentation.SupportFragment
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.ZhihuFourthFragment

/**
 * Created by YoKeyword on 16/6/6.
 */
class MeFragment : SupportFragment() {
    private var mTvBtnSettings: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.zhihu_fragment_fourth_me, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        mTvBtnSettings = view.findViewById(R.id.tv_btn_settings)
        mTvBtnSettings!!.setOnClickListener { v -> start(SettingsFragment.newInstance()) }
    }

    override fun onBackPressedSupport(): Boolean {
        // 这里实际项目中推荐使用 EventBus接耦
        if (parentFragment != null) {
            (parentFragment as ZhihuFourthFragment).onBackToFirstFragment()
        }
        return true
    }

    companion object {

        fun newInstance(): MeFragment {
            val args = Bundle()
            val fragment = MeFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
