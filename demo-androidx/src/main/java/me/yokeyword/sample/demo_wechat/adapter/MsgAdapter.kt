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
import me.yokeyword.sample.demo_wechat.entity.Msg
import me.yokeyword.sample.demo_wechat.listener.OnItemClickListener

/**
 * Created by YoKeyword on 16/6/30.
 */
class MsgAdapter(private val mContext: Context) : RecyclerView.Adapter<MsgAdapter.VH>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(mContext)
    private val mItems = ArrayList<Msg>()
    private var mClickListener: OnItemClickListener? = null

    fun addMsg(bean: Msg) {
        mItems.add(bean)
        notifyItemInserted(mItems.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = mInflater.inflate(R.layout.item_wechat_msg, parent, false)
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
        holder.tvMsg.text = item.getMessage()
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mClickListener = listener
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.img_avatar)
        val tvMsg: TextView = itemView.findViewById(R.id.tv_msg)
    }
}
