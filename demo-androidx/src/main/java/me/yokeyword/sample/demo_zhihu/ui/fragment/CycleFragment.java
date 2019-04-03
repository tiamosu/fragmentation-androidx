package me.yokeyword.sample.demo_zhihu.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_zhihu.base.BaseBackFragment;

/**
 * Created by YoKeyword on 16/2/7.
 */
@SuppressWarnings("FieldCanBeLocal")
public class CycleFragment extends BaseBackFragment {
    private static final String ARG_NUMBER = "arg_number";

    private Toolbar mToolbar;
    private TextView mTvName;
    private Button mBtnNext, mBtnNextWithFinish;

    private int mNumber;

    public static CycleFragment newInstance(int number) {
        final CycleFragment fragment = new CycleFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_NUMBER, number);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args != null) {
            mNumber = args.getInt(ARG_NUMBER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_cycle, container, false);
        initView(view);
        return view;
    }


    private void initView(View view) {
        mToolbar = view.findViewById(R.id.toolbar);
        mTvName = view.findViewById(R.id.tv_name);
        mBtnNext = view.findViewById(R.id.btn_next);
        mBtnNextWithFinish = view.findViewById(R.id.btn_next_with_finish);

        final String title = "CyclerFragment " + mNumber;
        mToolbar.setTitle(title);
        initToolbarNav(mToolbar);

        mTvName.setText(title);
        mBtnNext.setOnClickListener(v -> start(CycleFragment.newInstance(mNumber + 1)));
        mBtnNextWithFinish.setOnClickListener(v -> startWithPop(CycleFragment.newInstance(mNumber + 1)));
    }
}
