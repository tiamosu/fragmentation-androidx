package me.yokeyword.sample.demo_flow.ui.fragment.shop

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import me.yokeyword.fragmentation.anim.DefaultNoAnimator
import me.yokeyword.fragmentation.anim.FragmentAnimator
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_flow.base.MySupportFragment
import me.yokeyword.sample.demo_flow.ui.fragment.CycleFragment

/**
 * Created by YoKeyword on 16/2/9.
 */
class ContentFragment : MySupportFragment() {
    private var mTvContent: TextView? = null
    private var mBtnNext: Button? = null
    private var mMenu: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        if (args != null) {
            mMenu = args.getString(ARG_MENU)
        }
    }

    override fun onCreateFragmentAnimator(): FragmentAnimator? {
        return DefaultNoAnimator()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_content, container, false)
        initView(view)
        return view
    }

    @SuppressLint("SetTextI18n")
    private fun initView(view: View) {
        mTvContent = view.findViewById(R.id.tv_content)
        mBtnNext = view.findViewById(R.id.btn_next)

        mTvContent!!.text = "Content:\n" + mMenu!!

        mBtnNext!!.setOnClickListener {
            // 和MsgFragment同级别的跳转 交给MsgFragment处理
            if (parentFragment is ShopFragment) {
                (parentFragment as ShopFragment).start(CycleFragment.newInstance(1))
            }
        }
    }

    companion object {
        private const val ARG_MENU = "arg_menu"

        fun newInstance(menu: String): ContentFragment {
            val args = Bundle()
            val fragment = ContentFragment()
            args.putString(ARG_MENU, menu)
            fragment.arguments = args
            return fragment
        }
    }
}
