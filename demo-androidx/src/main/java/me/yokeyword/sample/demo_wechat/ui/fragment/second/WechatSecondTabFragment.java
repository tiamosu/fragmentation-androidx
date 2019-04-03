package me.yokeyword.sample.demo_wechat.ui.fragment.second;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_wechat.adapter.WechatPagerFragmentAdapter;
import me.yokeyword.sample.demo_wechat.base.BaseMainFragment;

/**
 * Created by YoKeyword on 16/6/30.
 */
@SuppressWarnings("FieldCanBeLocal")
public class WechatSecondTabFragment extends BaseMainFragment {
    private TabLayout mTab;
    private Toolbar mToolbar;
    private ViewPager mViewPager;

    public static WechatSecondTabFragment newInstance() {
        final Bundle args = new Bundle();
        final WechatSecondTabFragment fragment = new WechatSecondTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.wechat_fragment_tab_second, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = view.findViewById(R.id.toolbar);
        mTab = view.findViewById(R.id.tab);
        mViewPager = view.findViewById(R.id.viewPager);

        mToolbar.setTitle(R.string.discover);

        mTab.addTab(mTab.newTab());
        mTab.addTab(mTab.newTab());
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        mViewPager.setAdapter(new WechatPagerFragmentAdapter(getChildFragmentManager()
                , getString(R.string.all), getString(R.string.more)));
        mTab.setupWithViewPager(mViewPager);
    }
}
