package me.yokeyword.sample.demo_flow.ui.fragment_swipe_back;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_flow.adapter.PagerAdapter;

@SuppressWarnings("FieldCanBeLocal")
public class RecyclerSwipeBackFragment extends BaseSwipeBackFragment {
    private Toolbar mToolbar;
    private RecyclerView mRecy;
    private PagerAdapter mAdapter;

    public static RecyclerSwipeBackFragment newInstance() {
        return new RecyclerSwipeBackFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_swipe_back_recy, container, false);
        initView(view);
        return attachToSwipeBack(view);
    }

    private void initView(View view) {
        mRecy = view.findViewById(R.id.recy);

        mToolbar = view.findViewById(R.id.toolbar);
        _initToolbar(mToolbar);

        mAdapter = new PagerAdapter(_mActivity);
        final LinearLayoutManager manager = new LinearLayoutManager(_mActivity);
        mRecy.setLayoutManager(manager);
        mRecy.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((position, view1) -> start(FirstSwipeBackFragment.newInstance()));

        // Init Datas
        final List<String> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String item;
            item = getString(R.string.favorite) + " " + i;
            items.add(item);
        }
        mAdapter.setDatas(items);
    }
}
