package me.yokeyword.sample.demo_wechat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import me.yokeyword.fragmentation.SupportFragment
import me.yokeyword.sample.R

/**
 * @author tiamosu
 * @date 2020/3/13.
 */
class SplashFragment : SupportFragment() {
    private var mBtnGoLogin: Button? = null
    private var mBtnGoMain: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        mBtnGoLogin = view.findViewById<View>(R.id.btn_go_login) as Button
        mBtnGoMain = view.findViewById<View>(R.id.btn_go_main) as Button
        mBtnGoLogin?.setOnClickListener { startWithPop(LoginFragment.newInstance()) }
        mBtnGoMain?.setOnClickListener { startWithPop(MainFragment.newInstance()) }
    }

    companion object {
        fun newInstance(): SplashFragment {
            val args = Bundle()
            val fragment = SplashFragment()
            fragment.arguments = args
            return fragment
        }
    }
}