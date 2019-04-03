package me.yokeyword.sample.demo_wechat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_wechat.entity.Chat;
import me.yokeyword.sample.demo_wechat.listener.OnItemClickListener;

/**
 * Created by YoKeyword on 16/6/30.
 */
@SuppressWarnings("unused")
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
    private LayoutInflater mInflater;
    private List<Chat> mItems = new ArrayList<>();

    private OnItemClickListener mClickListener;

    public ChatAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setDatas(List<Chat> beans) {
        mItems.clear();
        mItems.addAll(beans);
        notifyDataSetChanged();
    }

    public void refreshMsg(Chat bean) {
        int index = mItems.indexOf(bean);
        if (index < 0) {
            return;
        }
        notifyItemChanged(index);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.item_wechat_chat, parent, false);
        final VH holder = new VH(view);
        holder.itemView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onItemClick(holder.getAdapterPosition(), v, holder);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final Chat item = mItems.get(position);
        holder.tvName.setText(item.name);
        holder.tvMsg.setText(item.message);
        holder.tvTime.setText(R.string.time);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public Chat getMsg(int position) {
        return mItems.get(position);
    }

    class VH extends RecyclerView.ViewHolder {
        private ImageView imgAvatar;
        private TextView tvName, tvMsg, tvTime;

        VH(View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvMsg = itemView.findViewById(R.id.tv_msg);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
