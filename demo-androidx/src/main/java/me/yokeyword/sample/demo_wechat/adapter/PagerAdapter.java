package me.yokeyword.sample.demo_wechat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_wechat.listener.OnItemClickListener;

/**
 * Created by YoKeyword on 16/6/30.
 */
public class PagerAdapter extends RecyclerView.Adapter<PagerAdapter.MyViewHolder> {
    private List<String> mItems = new ArrayList<>();
    private LayoutInflater mInflater;

    private OnItemClickListener mClickListener;

    public PagerAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public void setDatas(List<String> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.item_pager, parent, false);
        final MyViewHolder holder = new MyViewHolder(view);
        holder.itemView.setOnClickListener(v -> {
            final int position = holder.getAdapterPosition();
            if (mClickListener != null) {
                mClickListener.onItemClick(position, v, holder);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final String item = mItems.get(position);
        holder.tvTitle.setText(item);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;

        MyViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }
}
