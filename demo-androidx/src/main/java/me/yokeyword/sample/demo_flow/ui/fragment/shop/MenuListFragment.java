package me.yokeyword.sample.demo_flow.ui.fragment.shop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.yokeyword.fragmentation.anim.DefaultNoAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_flow.adapter.MenuAdapter;
import me.yokeyword.sample.demo_flow.base.MySupportFragment;

/**
 * Created by YoKeyword on 16/2/9.
 */
public class MenuListFragment extends MySupportFragment {
    private static final String ARG_MENUS = "arg_menus";
    private static final String SAVE_STATE_POSITION = "save_state_position";

    private RecyclerView mRecy;
    private MenuAdapter mAdapter;

    private ArrayList<String> mMenus;
    private int mCurrentPosition = -1;

    public static MenuListFragment newInstance(ArrayList<String> menus) {
        final Bundle args = new Bundle();
        final MenuListFragment fragment = new MenuListFragment();
        args.putStringArrayList(ARG_MENUS, menus);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mMenus = args.getStringArrayList(ARG_MENUS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_list_menu, container, false);
        initView(view);
        return view;
    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultNoAnimator();
    }

    private void initView(View view) {
        mRecy = view.findViewById(R.id.recy);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final LinearLayoutManager manager = new LinearLayoutManager(_mActivity);
        mRecy.setLayoutManager(manager);
        mAdapter = new MenuAdapter(_mActivity);
        mRecy.setAdapter(mAdapter);
        mAdapter.setDatas(mMenus);

        mAdapter.setOnItemClickListener((position, view) -> showContent(position));

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(SAVE_STATE_POSITION);
            mAdapter.setItemChecked(mCurrentPosition);
        } else {
            mCurrentPosition = 0;
            mAdapter.setItemChecked(0);
        }
    }

    private void showContent(int position) {
        if (position == mCurrentPosition) {
            return;
        }

        mCurrentPosition = position;
        mAdapter.setItemChecked(position);

        final ContentFragment fragment = ContentFragment.newInstance(mMenus.get(position));
        if (getParentFragment() != null) {
            ((ShopFragment) getParentFragment()).switchContentFragment(fragment);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_STATE_POSITION, mCurrentPosition);
    }
}
