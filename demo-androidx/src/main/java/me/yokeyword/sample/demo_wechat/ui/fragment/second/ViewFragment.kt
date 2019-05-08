package me.yokeyword.sample.demo_wechat.ui.fragment.second

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.yokeyword.fragmentation.SupportFragment
import me.yokeyword.sample.R

/**
 * Created by YoKey on 17/8/1.
 */

class ViewFragment : SupportFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.wechat_fragment_view, container, false)
    }

    companion object {

        fun newInstance(): ViewFragment {
            val args = Bundle()
            val fragment = ViewFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
