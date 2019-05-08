package me.yokeyword.sample.demo_wechat.ui.fragment.second

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.yokeyword.fragmentation.SupportFragment
import me.yokeyword.sample.R

/**
 * Created by YoKeyword on 16/6/30.
 */
class OtherPagerFragment : SupportFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.wechat_fragment_tab_second_pager_other, container, false)
    }

    companion object {

        fun newInstance(): OtherPagerFragment {
            val args = Bundle()
            val fragment = OtherPagerFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
