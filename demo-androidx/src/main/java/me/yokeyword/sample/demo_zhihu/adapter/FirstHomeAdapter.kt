package me.yokeyword.sample.demo_zhihu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_zhihu.entity.Article
import me.yokeyword.sample.demo_zhihu.listener.OnItemClickListener
import java.util.*

/**
 * Created by YoKeyword on 16/6/5.
 */
class FirstHomeAdapter(context: Context) : RecyclerView.Adapter<FirstHomeAdapter.VH>() {
    private val mItems = ArrayList<Article>()
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = mInflater.inflate(R.layout.item_zhihu_home_first, parent, false)
        val holder = VH(view)
        holder.itemView.setOnClickListener { v ->
            val position = holder.adapterPosition
            if (mClickListener != null) {
                mClickListener!!.onItemClick(position, v, holder)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = mItems[position]

        // 把每个图片视图设置不同的Transition名称, 防止在一个视图内有多个相同的名称, 在变换的时候造成混乱
        // Fragment支持多个View进行变换, 使用适配器时, 需要加以区分
        ViewCompat.setTransitionName(holder.img, position.toString() + "_image")
        ViewCompat.setTransitionName(holder.tvTitle, position.toString() + "_tv")

        holder.img.setImageResource(item.getImgRes())
        holder.tvTitle.text = item.getTitle()
    }

    fun setDatas(items: List<Article>) {
        mItems.clear()
        mItems.addAll(items)
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun getItem(position: Int): Article {
        return mItems[position]
    }

    fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
        this.mClickListener = itemClickListener
    }

    inner class VH internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val img: ImageView = itemView.findViewById(R.id.img)
    }
}
