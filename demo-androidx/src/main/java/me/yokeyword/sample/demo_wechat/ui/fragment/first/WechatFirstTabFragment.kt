package me.yokeyword.sample.demo_wechat.ui.fragment.first

import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import me.yokeyword.eventbusactivityscope.EventBusActivityScope
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_wechat.adapter.ChatAdapter
import me.yokeyword.sample.demo_wechat.base.BaseMainFragment
import me.yokeyword.sample.demo_wechat.entity.Chat
import me.yokeyword.sample.demo_wechat.event.TabSelectedEvent
import me.yokeyword.sample.demo_wechat.listener.OnItemClickListener
import me.yokeyword.sample.demo_wechat.ui.fragment.MainFragment
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * Created by YoKeyword on 16/6/30.
 */
class WechatFirstTabFragment : BaseMainFragment(), SwipeRefreshLayout.OnRefreshListener {
    private var mToolbar: Toolbar? = null
    private var mRefreshLayout: SwipeRefreshLayout? = null
    private var mRecy: RecyclerView? = null
    private var mInAtTop = true
    private var mScrollTotal: Int = 0
    private var mAdapter: ChatAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.wechat_fragment_tab_first, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        mToolbar = view.findViewById(R.id.toolbar)
        mRefreshLayout = view.findViewById(R.id.refresh_layout)
        mRecy = view.findViewById(R.id.recy)

        EventBusActivityScope.getDefault(context).register(this)

        mToolbar!!.setTitle(R.string.home)
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        mRefreshLayout!!.setOnRefreshListener(this)

        mRecy!!.layoutManager = LinearLayoutManager(context)
        mRecy!!.setHasFixedSize(true)
        val space = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, resources.displayMetrics).toInt()
        mRecy!!.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(0, 0, 0, space)
            }
        })
        mAdapter = ChatAdapter(context)
        mRecy!!.adapter = mAdapter

        mRecy!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                mScrollTotal += dy
                mInAtTop = mScrollTotal <= 0
            }
        })
        mAdapter!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int, view: View, vh: RecyclerView.ViewHolder) {
                // 因为启动的MsgFragment是MainFragment的兄弟Fragment,所以需要MainFragment.start()
                // 也可以像使用getParentFragment()的方式,拿到父Fragment来操作 或者使用 EventBusActivityScope
                if (parentFragment != null) {
                    (parentFragment as MainFragment).startBrotherFragment(MsgFragment.newInstance(mAdapter!!.getMsg(position)))
                }
            }
        })

        val chatList = initDatas()
        mAdapter!!.setDatas(chatList)
    }

    private fun initDatas(): List<Chat> {
        val msgList = ArrayList<Chat>()
        val name = arrayOf("Jake", "Eric", "Kenny", "Helen", "Carr")
        val chats = arrayOf("message1", "message2", "message3", "message4", "message5")

        for (i in 0..14) {
            val index = (Math.random() * 5).toInt()
            val chat = Chat()
            chat.setName(name[index])
            chat.setMessage(chats[index])
            msgList.add(chat)
        }
        return msgList
    }

    override fun onRefresh() {
        mRefreshLayout!!.postDelayed({ mRefreshLayout!!.isRefreshing = false }, 2500)
    }

    /**
     * Reselected Tab
     */
    @Subscribe
    fun onTabSelectedEvent(event: TabSelectedEvent) {
        if (event.position != MainFragment.FIRST) {
            return
        }
        if (mInAtTop) {
            mRefreshLayout!!.isRefreshing = true
            onRefresh()
        } else {
            scrollToTop()
        }
    }

    private fun scrollToTop() {
        mRecy!!.smoothScrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBusActivityScope.getDefault(context).unregister(this)
    }

    companion object {

        fun newInstance(): WechatFirstTabFragment {
            val args = Bundle()
            val fragment = WechatFirstTabFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
