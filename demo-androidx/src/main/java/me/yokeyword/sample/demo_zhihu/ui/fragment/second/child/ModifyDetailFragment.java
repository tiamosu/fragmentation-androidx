package me.yokeyword.sample.demo_zhihu.ui.fragment.second.child;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_zhihu.base.BaseBackFragment;
import me.yokeyword.sample.demo_zhihu.ui.fragment.CycleFragment;

/**
 * Created by YoKeyword on 16/2/7.
 */
@SuppressWarnings("FieldCanBeLocal")
public class ModifyDetailFragment extends BaseBackFragment {
    private static final String ARG_TITLE = "arg_title";

    private Toolbar mToolbar;
    private EditText mEtModiyTitle;
    private Button mBtnModify, mBtnNext;

    private String mTitle;

    public static ModifyDetailFragment newInstance(String title) {
        final Bundle args = new Bundle();
        final ModifyDetailFragment fragment = new ModifyDetailFragment();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mTitle = args.getString(ARG_TITLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_modify_detail, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = view.findViewById(R.id.toolbar);
        mEtModiyTitle = view.findViewById(R.id.et_modify_title);
        mBtnModify = view.findViewById(R.id.btn_modify);
        mBtnNext = view.findViewById(R.id.btn_next);

        mToolbar.setTitle(R.string.start_result_test);
        initToolbarNav(mToolbar);

        mEtModiyTitle.setText(mTitle);

        // 显示 软键盘
//        showSoftInput(mEtModiyTitle);

        mBtnModify.setOnClickListener(v -> {
            final Bundle bundle = new Bundle();
            bundle.putString(DetailFragment.KEY_RESULT_TITLE, mEtModiyTitle.getText().toString());
            setFragmentResult(RESULT_OK, bundle);

            Toast.makeText(getContext(), R.string.modify_success, Toast.LENGTH_SHORT).show();
        });
        mBtnNext.setOnClickListener(v -> start(CycleFragment.newInstance(1)));
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        hideSoftInput();
    }
}
