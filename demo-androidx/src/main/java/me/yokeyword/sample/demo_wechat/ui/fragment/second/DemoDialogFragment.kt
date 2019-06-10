package me.yokeyword.sample.demo_wechat.ui.fragment.second

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import me.yokeyword.fragmentation.ISupportActivity
import me.yokeyword.sample.R

/**
 * 使用DialogFragment时，需要重写show()，入Fragmentation的事务队列
 *
 * Dialog是基于Window （Activity也是Window），普通Fragment的视图一般基于View，这样会导致Dialog永远会浮在最顶层
 *
 * 可以考虑自定义半透明View的Fragment，从视觉上模拟Dialog
 *
 * Created by YoKey on 19/6/7.
 */
class DemoDialogFragment : DialogFragment() {

    private var mActivity: FragmentActivity? = null

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        mActivity = activity as FragmentActivity?
    }

    /**
     * Enqueue the Fragmentation Queue.
     *
     *
     * 如果是SupportFragment打开，可以不用复写该方法， 放到post()中show亦可
     */
    override fun show(manager: FragmentManager?, tag: String?) {
        if (mActivity is ISupportActivity) {
            (mActivity as ISupportActivity).getSupportDelegate()
                    .post(Runnable { super@DemoDialogFragment.show(manager, tag) })
            return
        }
        super.show(manager, tag)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.wechat_fragment_dialog, container, false)
        view.findViewById<View>(R.id.btn).setOnClickListener { dismiss() }
        return view
    }

    companion object {

        fun newInstance(): DemoDialogFragment {
            return DemoDialogFragment()
        }
    }
}
