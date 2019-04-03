package me.yokeyword.sample.demo_zhihu.adapter;

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
import me.yokeyword.sample.demo_zhihu.entity.Article;
import me.yokeyword.sample.demo_zhihu.listener.OnItemClickListener;

/**
 * 主页HomeFragment  Adapter
 * Created by YoKeyword on 16/2/1.
 */
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {
    private List<Article> mItems = new ArrayList<>();
    private LayoutInflater mInflater;
    private OnItemClickListener mClickListener;

    public HomeAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public void setDatas(List<Article> items) {
        mItems.clear();
        mItems.addAll(items);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.item_home, parent, false);
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
        final Article item = mItems.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvContent.setText(item.getContent());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Article getItem(int position) {
        return mItems.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvContent;

        MyViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
        }
    }
}
