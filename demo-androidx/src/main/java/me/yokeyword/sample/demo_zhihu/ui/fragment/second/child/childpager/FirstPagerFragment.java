package me.yokeyword.sample.demo_zhihu.ui.fragment.second.child.childpager;

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
import me.yokeyword.sample.demo_zhihu.MainActivity;
import me.yokeyword.sample.demo_zhihu.adapter.HomeAdapter;
import me.yokeyword.sample.demo_zhihu.entity.Article;
import me.yokeyword.sample.demo_zhihu.event.TabSelectedEvent;
import me.yokeyword.sample.demo_zhihu.ui.fragment.second.child.DetailFragment;

/**
 * Created by YoKeyword on 16/6/3.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class FirstPagerFragment extends SupportFragment implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView mRecy;
    private SwipeRefreshLayout mRefreshLayout;
    private HomeAdapter mAdapter;
    private boolean mAtTop = true;
    private int mScrollTotal;
    private String[] mTitles;
    private String[] mContents;

    public static FirstPagerFragment newInstance() {
        final Bundle args = new Bundle();
        final FirstPagerFragment fragment = new FirstPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.zhihu_fragment_second_pager_first, container, false);
        EventBusActivityScope.getDefault(mActivity).register(this);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mRecy = view.findViewById(R.id.recy);
        mRefreshLayout = view.findViewById(R.id.refresh_layout);

        mTitles = getResources().getStringArray(R.array.array_title);
        mContents = getResources().getStringArray(R.array.array_content);

        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setOnRefreshListener(this);

        mAdapter = new HomeAdapter(mActivity);
        final LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        mRecy.setLayoutManager(manager);
        mRecy.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((position, view1, vh) -> {
            // 这里的DetailFragment在flow包里
            // 这里是父Fragment启动,要注意 栈层级
            if (getParentFragment() != null) {
                ((SupportFragment) getParentFragment()).start(DetailFragment.newInstance(mAdapter.getItem(position).getTitle()));
            }
        });

        // Init Datas
        final List<Article> articleList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            final int index = (int) (Math.random() * 3);
            final Article article = new Article(mTitles[index], mContents[index]);
            articleList.add(article);
        }
        mAdapter.setDatas(articleList);

        mRecy.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mScrollTotal += dy;
                mAtTop = mScrollTotal <= 0;
            }
        });
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.postDelayed(() -> mRefreshLayout.setRefreshing(false), 2000);
    }

    private void scrollToTop() {
        mRecy.smoothScrollToPosition(0);
    }

    /**
     * 选择tab事件
     */
    @Subscribe
    public void onTabSelectedEvent(TabSelectedEvent event) {
        if (event.position != MainActivity.SECOND) {
            return;
        }
        if (mAtTop) {
            mRefreshLayout.setRefreshing(true);
            onRefresh();
        } else {
            scrollToTop();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBusActivityScope.getDefault(mActivity).unregister(this);
    }

}
