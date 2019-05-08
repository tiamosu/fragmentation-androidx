package me.yokeyword.sample.demo_wechat.ui.fragment.third

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_wechat.adapter.HomeAdapter
import me.yokeyword.sample.demo_wechat.base.BaseMainFragment
import me.yokeyword.sample.demo_wechat.entity.Article
import me.yokeyword.sample.demo_wechat.listener.OnItemClickListener
import me.yokeyword.sample.demo_wechat.ui.fragment.MainFragment
import java.util.*

/**
 * Created by YoKeyword on 16/6/30.
 */
class WechatThirdTabFragment : BaseMainFragment() {
    private var mRecy: RecyclerView? = null
    private var mToolbar: Toolbar? = null
    private var mAdapter: HomeAdapter? = null
    private var mTitles: Array<String>? = null
    private var mContents: Array<String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.wechat_fragment_tab_third, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        mRecy = view.findViewById(R.id.recy)
        mToolbar = view.findViewById(R.id.toolbar)

        mTitles = resources.getStringArray(R.array.array_title)
        mContents = resources.getStringArray(R.array.array_content)

        mToolbar!!.setTitle(R.string.more)
    }


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        mAdapter = HomeAdapter(context)
        val manager = LinearLayoutManager(context)
        mRecy!!.layoutManager = manager
        mRecy!!.adapter = mAdapter

        mAdapter!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int, view: View, vh: RecyclerView.ViewHolder) {
                (parentFragment as? MainFragment)
                        ?.startBrotherFragment(DetailFragment.newInstance(mAdapter!!.getItem(position).getTitle()))
                // 或者使用EventBus
                // EventBusActivityScope.getDefault(mActivity).post(new StartBrotherEvent (DetailFragment.newInstance(mAdapter.getItem(position).getTitle())));
            }
        })

        // Init Datas
        val articleList = ArrayList<Article>()
        for (i in 0..2) {
            val article = Article(mTitles!![i], mContents!![i])
            articleList.add(article)
        }
        mAdapter!!.setDatas(articleList)
    }

    companion object {

        fun newInstance(): WechatThirdTabFragment {
            val args = Bundle()
            val fragment = WechatThirdTabFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
