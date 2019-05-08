package me.yokeyword.sample.demo_zhihu.ui.fragment.first.child

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.widget.Toolbar
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_zhihu.base.BaseBackFragment
import me.yokeyword.sample.demo_zhihu.entity.Article
import me.yokeyword.sample.demo_zhihu.ui.fragment.CycleFragment

/**
 * Created by YoKeyword on 16/6/5.
 */
class FirstDetailFragment : BaseBackFragment() {
    private var mArticle: Article? = null
    private var mToolbar: Toolbar? = null
    private var mImgDetail: ImageView? = null
    private var mTvTitle: TextView? = null
    private var mFab: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mArticle = arguments!!.getParcelable(ARG_ITEM)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.zhihu_fragment_first_detail, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        mToolbar = view.findViewById(R.id.toolbar)
        mImgDetail = view.findViewById(R.id.img_detail)
        mTvTitle = view.findViewById(R.id.tv_content)
        mFab = view.findViewById(R.id.fab)

        mToolbar!!.title = ""
        initToolbarNav(mToolbar!!)
        mImgDetail!!.setImageResource(mArticle!!.getImgRes())
        mTvTitle!!.text = mArticle!!.getTitle()

        mFab!!.setOnClickListener { start(CycleFragment.newInstance(1)) }
    }

    companion object {
        private const val ARG_ITEM = "arg_item"

        fun newInstance(article: Article): FirstDetailFragment {
            val args = Bundle()
            args.putParcelable(ARG_ITEM, article)
            val fragment = FirstDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
