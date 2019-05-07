package me.yokeyword.sample.demo_flow.ui.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.yokeyword.fragmentation.ISupportActivity
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator
import me.yokeyword.fragmentation.anim.DefaultNoAnimator
import me.yokeyword.fragmentation.anim.DefaultVerticalAnimator
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_flow.adapter.HomeAdapter
import me.yokeyword.sample.demo_flow.base.BaseMainFragment
import me.yokeyword.sample.demo_flow.entity.Article
import me.yokeyword.sample.demo_flow.listener.OnItemClickListener
import java.util.*

class HomeFragment : BaseMainFragment(), Toolbar.OnMenuItemClickListener {
    private var mTitles: Array<String>? = null
    private var mContents: Array<String>? = null

    private var mToolbar: Toolbar? = null
    private var mRecy: RecyclerView? = null
    private var mAdapter: HomeAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initView(view)
        //        动态改动 当前Fragment的动画
        //        setFragmentAnimator(fragmentAnimator);
        return view
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_anim) {
            val popupMenu = PopupMenu(mActivity, mToolbar!!, GravityCompat.END)
            popupMenu.inflate(R.menu.home_pop)
            popupMenu.setOnMenuItemClickListener { item1 ->
                when (item1.itemId) {
                    R.id.action_anim_veritical -> {
                        (mActivity as ISupportActivity).setFragmentAnimator(DefaultVerticalAnimator())
                        Toast.makeText(mActivity, R.string.anim_v, Toast.LENGTH_SHORT).show()
                    }
                    R.id.action_anim_horizontal -> {
                        (mActivity as ISupportActivity).setFragmentAnimator(DefaultHorizontalAnimator())
                        Toast.makeText(mActivity, R.string.anim_h, Toast.LENGTH_SHORT).show()
                    }
                    R.id.action_anim_none -> {
                        (mActivity as ISupportActivity).setFragmentAnimator(DefaultNoAnimator())
                        Toast.makeText(mActivity, R.string.anim_none, Toast.LENGTH_SHORT).show()
                    }
                }
                popupMenu.dismiss()
                true
            }
            popupMenu.show()
        }
        return true
    }

    private fun initView(view: View) {
        mToolbar = view.findViewById(R.id.toolbar)
        mRecy = view.findViewById(R.id.recy)

        mTitles = resources.getStringArray(R.array.array_title)
        mContents = resources.getStringArray(R.array.array_content)

        mToolbar!!.setTitle(R.string.home)
        initToolbarNav(mToolbar!!, true)
        mToolbar!!.inflateMenu(R.menu.home)
        mToolbar!!.setOnMenuItemClickListener(this)

        mAdapter = HomeAdapter(mActivity)
        val manager = LinearLayoutManager(mActivity)
        mRecy!!.layoutManager = manager
        mRecy!!.adapter = mAdapter

        mAdapter!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int, view: View) {
                start(DetailFragment.newInstance(mAdapter!!.getItem(position).getTitle()))
            }
        })

        // Init Datas
        val articleList = ArrayList<Article>()
        for (i in 0..14) {
            val index = (Math.random() * 3).toInt()
            val article = Article(mTitles!![index], mContents!![index])
            articleList.add(article)
        }
        mAdapter!!.setDatas(articleList)
    }

    /**
     * 类似于 Activity的 onNewIntent()
     */
    override fun onNewBundle(args: Bundle) {
        super.onNewBundle(args)

        Toast.makeText(mActivity, args.getString("from"), Toast.LENGTH_SHORT).show()
    }

    companion object {

        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}
