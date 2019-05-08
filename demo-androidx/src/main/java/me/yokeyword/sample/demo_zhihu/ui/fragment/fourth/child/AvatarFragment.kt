package me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.child

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.yokeyword.fragmentation.SupportFragment
import me.yokeyword.sample.R

/**
 * Created by YoKeyword on 16/6/6.
 */
class AvatarFragment : SupportFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.zhihu_fragment_fourth_avatar, container, false)
    }

    companion object {

        fun newInstance(): AvatarFragment {
            val args = Bundle()
            val fragment = AvatarFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
