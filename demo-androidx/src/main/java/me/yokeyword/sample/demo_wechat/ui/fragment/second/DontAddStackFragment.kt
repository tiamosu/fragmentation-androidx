package me.yokeyword.sample.demo_wechat.ui.fragment.second

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import me.yokeyword.fragmentation.SupportFragment
import me.yokeyword.sample.R

/**
 * @author tiamosu
 * @date 2020/3/13.
 */
open class DontAddStackFragment : SupportFragment() {
    private var mToolbar: Toolbar? = null
    private var mTvName: TextView? = null
    private var mBtnRemove: Button? = null

    protected fun initToolbarNav(toolbar: Toolbar?) {
        toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar?.setNavigationOnClickListener { context.onBackPressed() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dont_add_stack, container, false)
        initView(view)
        return view
    }

    @SuppressLint("SetTextI18n")
    private fun initView(view: View) {
        mToolbar = view.findViewById(R.id.toolbar)
        mTvName = view.findViewById(R.id.tv_name)
        mBtnRemove = view.findViewById(R.id.btn_remove)

        val title = "DontAddStackFragment"
        mToolbar?.title = title
        initToolbarNav(mToolbar)

        mTvName?.text = title

        mBtnRemove?.setOnClickListener {
            extraTransaction()
                    .remove(this@DontAddStackFragment, true)
        }
    }

    /**
     * 处理回退事件
     *
     * @return
     */
    override fun onBackPressedSupport(): Boolean {
        extraTransaction()
                .remove(this@DontAddStackFragment, true)
        return true
    }

    companion object {
        fun newInstance(): DontAddStackFragment {
            return DontAddStackFragment()
        }
    }
}