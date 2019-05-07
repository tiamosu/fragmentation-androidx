package me.yokeyword.sample.demo_flow.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.util.ArrayList
import androidx.recyclerview.widget.RecyclerView
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_flow.entity.Article
import me.yokeyword.sample.demo_flow.listener.OnItemClickListener

/**
 * 主页HomeFragment  Adapter
 * Created by YoKeyword on 16/2/1.
 */
class HomeAdapter(context: Context) : RecyclerView.Adapter<HomeAdapter.MyViewHolder>() {
    private val mItems = ArrayList<Article>()
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: OnItemClickListener? = null

    fun setDatas(items: List<Article>) {
        mItems.clear()
        mItems.addAll(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = mInflater.inflate(R.layout.item_home, parent, false)
        val holder = MyViewHolder(view)
        holder.itemView.setOnClickListener { v ->
            val position = holder.adapterPosition
            if (mClickListener != null) {
                mClickListener!!.onItemClick(position, v)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = mItems[position]
        holder.tvTitle.text = item.getTitle()
        holder.tvContent.text = item.getContent()
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

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
    }
}
