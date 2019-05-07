package me.yokeyword.sample.demo_flow.ui.fragment.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.yokeyword.fragmentation.anim.DefaultNoAnimator
import me.yokeyword.fragmentation.anim.FragmentAnimator
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_flow.adapter.MenuAdapter
import me.yokeyword.sample.demo_flow.base.MySupportFragment
import me.yokeyword.sample.demo_flow.listener.OnItemClickListener
import java.util.*

/**
 * Created by YoKeyword on 16/2/9.
 */
class MenuListFragment : MySupportFragment() {
    private var mRecy: RecyclerView? = null
    private var mAdapter: MenuAdapter? = null
    private var mMenus: ArrayList<String>? = null
    private var mCurrentPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        if (args != null) {
            mMenus = args.getStringArrayList(ARG_MENUS)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_menu, container, false)
        initView(view)
        return view
    }

    override fun onCreateFragmentAnimator(): FragmentAnimator? {
        return DefaultNoAnimator()
    }

    private fun initView(view: View) {
        mRecy = view.findViewById(R.id.recy)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val manager = LinearLayoutManager(mActivity)
        mRecy!!.layoutManager = manager
        mAdapter = MenuAdapter(mActivity)
        mRecy!!.adapter = mAdapter
        mAdapter!!.setDatas(mMenus!!)

        mAdapter!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int, view: View) {
                showContent(position)
            }
        })

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(SAVE_STATE_POSITION)
            mAdapter!!.setItemChecked(mCurrentPosition)
        } else {
            mCurrentPosition = 0
            mAdapter!!.setItemChecked(0)
        }
    }

    private fun showContent(position: Int) {
        if (position == mCurrentPosition) {
            return
        }

        mCurrentPosition = position
        mAdapter!!.setItemChecked(position)

        val fragment = ContentFragment.newInstance(mMenus!![position])
        if (parentFragment != null) {
            (parentFragment as ShopFragment).switchContentFragment(fragment)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVE_STATE_POSITION, mCurrentPosition)
    }

    companion object {
        private const val ARG_MENUS = "arg_menus"
        private const val SAVE_STATE_POSITION = "save_state_position"

        fun newInstance(menus: ArrayList<String>): MenuListFragment {
            val args = Bundle()
            val fragment = MenuListFragment()
            args.putStringArrayList(ARG_MENUS, menus)
            fragment.arguments = args
            return fragment
        }
    }
}
