package me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.child;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.ZhihuFourthFragment;

/**
 * Created by YoKeyword on 16/6/6.
 */
@SuppressWarnings("FieldCanBeLocal")
public class MeFragment extends SupportFragment {
    private TextView mTvBtnSettings;

    public static MeFragment newInstance() {
        final  Bundle args = new Bundle();
        final  MeFragment fragment = new MeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final  View view = inflater.inflate(R.layout.zhihu_fragment_fourth_me, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mTvBtnSettings = view.findViewById(R.id.tv_btn_settings);
        mTvBtnSettings.setOnClickListener(v -> start(SettingsFragment.newInstance()));
    }

    @Override
    public boolean onBackPressedSupport() {
        // 这里实际项目中推荐使用 EventBus接耦
        if (getParentFragment() != null) {
            ((ZhihuFourthFragment) getParentFragment()).onBackToFirstFragment();
        }
        return true;
    }
}
