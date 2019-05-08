package me.yokeyword.sample.demo_wechat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList
import androidx.recyclerview.widget.RecyclerView
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_wechat.entity.Chat
import me.yokeyword.sample.demo_wechat.listener.OnItemClickListener

/**
 * Created by YoKeyword on 16/6/30.
 */
class ChatAdapter(context: Context) : RecyclerView.Adapter<ChatAdapter.VH>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val mItems = ArrayList<Chat>()
    private var mClickListener: OnItemClickListener? = null

    fun setDatas(beans: List<Chat>) {
        mItems.clear()
        mItems.addAll(beans)
        notifyDataSetChanged()
    }

    fun refreshMsg(bean: Chat) {
        val index = mItems.indexOf(bean)
        if (index < 0) {
            return
        }
        notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = mInflater.inflate(R.layout.item_wechat_chat, parent, false)
        val holder = VH(view)
        holder.itemView.setOnClickListener { v ->
            if (mClickListener != null) {
                mClickListener!!.onItemClick(holder.adapterPosition, v, holder)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = mItems[position]
        holder.tvName.text = item.getName()
        holder.tvMsg.text = item.getMessage()
        holder.tvTime.setText(R.string.time)
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mClickListener = listener
    }

    fun getMsg(position: Int): Chat {
        return mItems[position]
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.img_avatar)
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvMsg: TextView = itemView.findViewById(R.id.tv_msg)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
    }
}
