package me.yokeyword.sample.demo_wechat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_wechat.base.BaseMainFragment

/**
 * @author tiamosu
 * @date 2020/3/13.
 */
class LoginFragment : BaseMainFragment() {
    private var mBtnGoMain: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_new_login, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        mBtnGoMain = view.findViewById(R.id.btn_go_main)
        mBtnGoMain?.setOnClickListener { startWithPop(MainFragment.newInstance()) }
    }

    companion object {

        fun newInstance(): LoginFragment {
            val args = Bundle()
            val fragment = LoginFragment()
            fragment.arguments = args
            return fragment
        }
    }
}