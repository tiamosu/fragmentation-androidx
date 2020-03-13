package me.yokeyword.sample.demo_wechat.ui.fragment.second;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.sample.R;

/**
 * @author tiamosu
 * @date 2020/3/13.
 */
public class DontAddStackFragment extends SupportFragment {
    private Toolbar mToolbar;
    private TextView mTvName;
    private Button mBtnRemove;

    public static DontAddStackFragment newInstance() {
        return new DontAddStackFragment();
    }

    protected void initToolbarNav(Toolbar toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(v -> getContext().onBackPressed());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dont_add_stack, container, false);
        initView(view);
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void initView(View view) {
        mToolbar = view.findViewById(R.id.toolbar);
        mTvName = view.findViewById(R.id.tv_name);
        mBtnRemove = view.findViewById(R.id.btn_remove);

        String title = "DontAddStackFragment";

        mToolbar.setTitle(title);
        initToolbarNav(mToolbar);

        mTvName.setText(title + "\n" + getString(R.string.can_swipe));
        mBtnRemove.setOnClickListener(v ->
                extraTransaction()
                        .remove(DontAddStackFragment.this, true));
    }

    /**
     * 处理回退事件
     *
     * @return
     */
    @Override
    public boolean onBackPressedSupport() {
        extraTransaction()
                .remove(DontAddStackFragment.this, true);
        return true;
    }
}
