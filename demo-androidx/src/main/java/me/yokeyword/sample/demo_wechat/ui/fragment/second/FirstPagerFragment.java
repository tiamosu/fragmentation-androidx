package me.yokeyword.sample.demo_wechat.ui.fragment.second;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_wechat.adapter.PagerAdapter;
import me.yokeyword.sample.demo_wechat.event.TabSelectedEvent;
import me.yokeyword.sample.demo_wechat.ui.fragment.MainFragment;

/**
 * Created by YoKeyword on 16/6/30.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class FirstPagerFragment extends SupportFragment implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecy;
    private PagerAdapter mAdapter;

    private boolean mInAtTop = true;
    private int mScrollTotal;

    public static FirstPagerFragment newInstance() {
        final Bundle args = new Bundle();
        final FirstPagerFragment fragment = new FirstPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.wechat_fragment_tab_second_pager_first, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        EventBusActivityScope.getDefault(getContext()).register(this);

        mRecy = view.findViewById(R.id.recy);
        mRefreshLayout = view.findViewById(R.id.refresh_layout);

        mRefreshLayout.setOnRefreshListener(this);

        mAdapter = new PagerAdapter(getContext());
        mRecy.setHasFixedSize(true);
        final LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mRecy.setLayoutManager(manager);
        mRecy.setAdapter(mAdapter);

        mRecy.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mScrollTotal += dy;
                mInAtTop = mScrollTotal <= 0;
            }
        });

        mAdapter.setOnItemClickListener((position, view1, holder) -> {
            // 通知MainFragment跳转至NewFeatureFragment
            if (getParentFragment() != null) {
                if (getParentFragment().getParentFragment() != null) {
                    ((MainFragment) getParentFragment().getParentFragment()).startBrotherFragment(NewFeatureFragment.newInstance());
                }
            }
        });

        // Init Datas
        final List<String> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final String item = "New features";
            items.add(item);
        }
        mAdapter.setDatas(items);
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.postDelayed(() -> mRefreshLayout.setRefreshing(false), 2500);
    }

    /**
     * Reselected Tab
     */
    @Subscribe
    public void onTabSelectedEvent(TabSelectedEvent event) {
        if (event.position != MainFragment.SECOND) {
            return;
        }
        if (mInAtTop) {
            mRefreshLayout.setRefreshing(true);
            onRefresh();
        } else {
            scrollToTop();
        }
    }

    private void scrollToTop() {
        mRecy.smoothScrollToPosition(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBusActivityScope.getDefault(getContext()).unregister(this);
    }
}
