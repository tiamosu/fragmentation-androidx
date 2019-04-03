package me.yokeyword.sample.demo_flow.ui.fragment.discover;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_flow.adapter.PagerAdapter;
import me.yokeyword.sample.demo_flow.base.MySupportFragment;
import me.yokeyword.sample.demo_flow.ui.fragment.CycleFragment;

@SuppressWarnings("FieldCanBeLocal")
public class PagerChildFragment extends MySupportFragment {
    private static final String ARG_FROM = "arg_from";

    private int mFrom;

    private RecyclerView mRecy;
    private PagerAdapter mAdapter;

    public static PagerChildFragment newInstance(int from) {
        final Bundle args = new Bundle();
        final PagerChildFragment fragment = new PagerChildFragment();
        args.putInt(ARG_FROM, from);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mFrom = args.getInt(ARG_FROM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_pager, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mRecy = view.findViewById(R.id.recy);

        mAdapter = new PagerAdapter(_mActivity);
        final LinearLayoutManager manager = new LinearLayoutManager(_mActivity);
        mRecy.setLayoutManager(manager);
        mRecy.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((position, view1) -> {
            if (getParentFragment() instanceof DiscoverFragment) {
                ((DiscoverFragment) getParentFragment()).start(CycleFragment.newInstance(1));
            }
        });

        mRecy.post(() -> {
            // Init Datas
            final List<String> items = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                String item;
                if (mFrom == 0) {
                    item = getString(R.string.recommend) + " " + i;
                } else if (mFrom == 1) {
                    item = getString(R.string.hot) + " " + i;
                } else {
                    item = getString(R.string.favorite) + " " + i;
                }
                items.add(item);
            }
            mAdapter.setDatas(items);
        });
    }
}
