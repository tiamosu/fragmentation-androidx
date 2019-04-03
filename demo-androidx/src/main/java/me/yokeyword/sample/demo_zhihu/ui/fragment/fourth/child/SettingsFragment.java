package me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.child;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.sample.R;

/**
 * Created by YoKeyword on 16/6/6.
 */
@SuppressWarnings("FieldCanBeLocal")
public class SettingsFragment extends SupportFragment {
    private Toolbar mToolbar;

    public static SettingsFragment newInstance() {
        final Bundle args = new Bundle();
        final SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.zhihu_fragment_fourth_settings, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = view.findViewById(R.id.toolbarSettings);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(v -> _mActivity.onBackPressed());
    }

    @Override
    public boolean onBackPressedSupport() {
        pop();
        return true;
    }
}
