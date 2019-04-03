package me.yokeyword.sample.demo_zhihu.ui.fragment.fourth.child;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.sample.R;

/**
 * Created by YoKeyword on 16/6/6.
 */
public class AvatarFragment extends SupportFragment {

    public static AvatarFragment newInstance() {
        final Bundle args = new Bundle();
        final AvatarFragment fragment = new AvatarFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.zhihu_fragment_fourth_avatar, container, false);
    }
}
