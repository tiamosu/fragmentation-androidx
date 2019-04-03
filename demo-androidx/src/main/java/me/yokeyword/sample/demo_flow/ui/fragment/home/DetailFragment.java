package me.yokeyword.sample.demo_flow.ui.fragment.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_flow.base.BaseBackFragment;

/**
 * Created by YoKeyword on 16/2/3.
 */
public class DetailFragment extends BaseBackFragment {
    static final String KEY_RESULT_TITLE = "title";
    private static final int REQ_MODIFY_FRAGMENT = 100;
    private static final String ARG_TITLE = "arg_title";
    private Toolbar mToolbar;
    private TextView mTvContent;
    private FloatingActionButton mFab;
    private String mTitle;

    public static DetailFragment newInstance(String title) {
        final DetailFragment fragment = new DetailFragment();
        final Bundle bundle = new Bundle();
        bundle.putString(ARG_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        if (bundle != null) {
            mTitle = bundle.getString(ARG_TITLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_detail, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = view.findViewById(R.id.toolbar);
        mFab = view.findViewById(R.id.fab);
        mTvContent = view.findViewById(R.id.tv_content);

        mToolbar.setTitle(mTitle);

        initToolbarNav(mToolbar);
    }

    /**
     * 这里演示:
     * 比较复杂的Fragment页面会在第一次start时,导致动画卡顿
     * Fragmentation提供了onEnterAnimationEnd()方法,该方法会在 入栈动画 结束时回调
     * 所以在onCreateView进行一些简单的View初始化(比如 toolbar设置标题,返回按钮; 显示加载数据的进度条等),
     * 然后在onEnterAnimationEnd()方法里进行 复杂的耗时的初始化 (比如FragmentPagerAdapter的初始化 加载数据等)
     */
    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        initDelayView();
    }

    private void initDelayView() {
        mTvContent.setText(R.string.large_text);

        mFab.setOnClickListener(v -> startForResult(ModifyDetailFragment.newInstance(mTitle), REQ_MODIFY_FRAGMENT));
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        super.onFragmentResult(requestCode, resultCode, data);
        if (requestCode == REQ_MODIFY_FRAGMENT && resultCode == RESULT_OK && data != null) {
            mTitle = data.getString(KEY_RESULT_TITLE);
            mToolbar.setTitle(mTitle);
            // 保存被改变的 title
            if (getArguments() != null) {
                getArguments().putString(ARG_TITLE, mTitle);
            }
            Toast.makeText(_mActivity, R.string.modify_title, Toast.LENGTH_SHORT).show();
        }
    }
}
