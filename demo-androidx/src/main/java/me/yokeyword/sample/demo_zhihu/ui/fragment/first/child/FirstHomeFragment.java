package me.yokeyword.sample.demo_zhihu.ui.fragment.first.child;

import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_zhihu.MainActivity;
import me.yokeyword.sample.demo_zhihu.adapter.FirstHomeAdapter;
import me.yokeyword.sample.demo_zhihu.entity.Article;
import me.yokeyword.sample.demo_zhihu.event.TabSelectedEvent;
import me.yokeyword.sample.demo_zhihu.helper.DetailTransition;

/**
 * Created by YoKeyword on 16/6/5.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class FirstHomeFragment extends SupportFragment implements SwipeRefreshLayout.OnRefreshListener {
    private Toolbar mToolbar;
    private RecyclerView mRecy;
    private SwipeRefreshLayout mRefreshLayout;
    private FloatingActionButton mFab;

    private FirstHomeAdapter mAdapter;

    private boolean mInAtTop = true;
    private int mScrollTotal;

    private String[] mTitles = new String[]{
            "Use imagery to express a distinctive voice and exemplify creative excellence.",
            "An image that tells a story is infinitely more interesting and informative.",
            "The most powerful iconic images consist of a few meaningful elements, with minimal distractions.",
            "Properly contextualized concepts convey your message and brand more effectively.",
            "Have an iconic point of focus in your imagery. Focus ranges from a single entity to an overarching composition."
    };

    private int[] mImgRes = new int[]{
            R.drawable.bg_first, R.drawable.bg_second, R.drawable.bg_third, R.drawable.bg_fourth, R.drawable.bg_fifth
    };


    public static FirstHomeFragment newInstance() {
        final Bundle args = new Bundle();
        final FirstHomeFragment fragment = new FirstHomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.zhihu_fragment_first_home, container, false);
        EventBusActivityScope.getDefault(getContext()).register(this);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = view.findViewById(R.id.toolbar);
        mRecy = view.findViewById(R.id.recy);
        mRefreshLayout = view.findViewById(R.id.refresh_layout);
        mFab = view.findViewById(R.id.fab);

        mToolbar.setTitle(R.string.home);

        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setOnRefreshListener(this);

        mAdapter = new FirstHomeAdapter(getContext());
        final LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mRecy.setLayoutManager(manager);
        mRecy.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((position, view1, vh) -> {
            final FirstDetailFragment fragment = FirstDetailFragment.newInstance(mAdapter.getItem(position));

            // 这里是使用SharedElement的用例
            // LOLLIPOP(5.0)系统的 SharedElement支持有 系统BUG， 这里判断大于 > LOLLIPOP
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                setExitTransition(new Fade());
                fragment.setEnterTransition(new Fade());
                fragment.setSharedElementReturnTransition(new DetailTransition());
                fragment.setSharedElementEnterTransition(new DetailTransition());

                // 25.1.0以下的support包,Material过渡动画只有在进栈时有,返回时没有;
                // 25.1.0+的support包，SharedElement正常
                extraTransaction()
                        .addSharedElement(((FirstHomeAdapter.VH) vh).img, getString(R.string.image_transition))
                        .addSharedElement(((FirstHomeAdapter.VH) vh).tvTitle, "tv")
                        .start(fragment);
            } else {
                start(fragment);
            }
        });

        // Init Datas
        final List<Article> articleList = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            final int index = i % 5;
            final Article article = new Article(mTitles[index], mImgRes[index]);
            articleList.add(article);
        }
        mAdapter.setDatas(articleList);

        mRecy.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mScrollTotal += dy;
                mInAtTop = mScrollTotal <= 0;
                if (dy > 5) {
                    mFab.hide();
                } else if (dy < -5) {
                    mFab.show();
                }
            }
        });

        mFab.setOnClickListener(v -> Toast.makeText(getContext(), "Action", Toast.LENGTH_SHORT).show());
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
        if (event.position != MainActivity.FIRST) {
            return;
        }
        if (mInAtTop) {
            mRefreshLayout.setRefreshing(true);
            onRefresh();
        } else {
            scrollToTop();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBusActivityScope.getDefault(getContext()).unregister(this);
    }
}
