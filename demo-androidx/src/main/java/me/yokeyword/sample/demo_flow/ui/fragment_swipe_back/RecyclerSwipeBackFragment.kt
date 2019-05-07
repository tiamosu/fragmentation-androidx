package me.yokeyword.sample.demo_flow.ui.fragment_swipe_back

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_flow.adapter.PagerAdapter
import me.yokeyword.sample.demo_flow.listener.OnItemClickListener
import java.util.*

class RecyclerSwipeBackFragment : BaseSwipeBackFragment() {
    private var mToolbar: Toolbar? = null
    private var mRecy: RecyclerView? = null
    private var mAdapter: PagerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_swipe_back_recy, container, false)
        initView(view)
        return attachToSwipeBack(view)
    }

    private fun initView(view: View) {
        mRecy = view.findViewById(R.id.recy)

        mToolbar = view.findViewById(R.id.toolbar)
        _initToolbar(mToolbar!!)

        mAdapter = PagerAdapter(context)
        val manager = LinearLayoutManager(context)
        mRecy!!.layoutManager = manager
        mRecy!!.adapter = mAdapter

        mAdapter!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int, view: View) {
                start(FirstSwipeBackFragment.newInstance())
            }
        })

        // Init Datas
        val items = ArrayList<String>()
        for (i in 0..19) {
            val item: String = getString(R.string.favorite) + " " + i
            items.add(item)
        }
        mAdapter!!.setDatas(items)
    }

    companion object {

        fun newInstance(): RecyclerSwipeBackFragment {
            return RecyclerSwipeBackFragment()
        }
    }
}
