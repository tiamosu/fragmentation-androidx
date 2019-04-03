package me.yokeyword.sample.demo_zhihu.ui.fragment.third.child;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_zhihu.ui.fragment.third.child.child.ContentFragment;
import me.yokeyword.sample.demo_zhihu.ui.fragment.third.child.child.MenuListFragment;

/**
 * Created by YoKeyword on 16/2/4.
 */
@SuppressWarnings("FieldCanBeLocal")
public class ShopFragment extends SupportFragment {
    private Toolbar mToolbar;

    public static ShopFragment newInstance() {
        final Bundle args = new Bundle();
        final ShopFragment fragment = new ShopFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_shop, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.shop);

        if (findChildFragment(MenuListFragment.class) == null) {
            final ArrayList<String> listMenus = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.array_menu)));
            final MenuListFragment menuListFragment = MenuListFragment.newInstance(listMenus);
            loadRootFragment(R.id.fl_list_container, menuListFragment);
            // false:  不加入回退栈;  false: 不显示动画
            loadRootFragment(R.id.fl_content_container, ContentFragment.newInstance(listMenus.get(0)), false, false);
        }
    }

    @Override
    public boolean onBackPressedSupport() {
        // ContentFragment是ShopFragment的栈顶子Fragment,会先调用ContentFragment的onBackPressedSupport方法
        Toast.makeText(_mActivity, "onBackPressedSupport-->return false, " + getString(R.string.upper_process), Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * 替换加载 内容Fragment
     */
    public void switchContentFragment(ContentFragment fragment) {
        final SupportFragment contentFragment = findChildFragment(ContentFragment.class);
        if (contentFragment != null) {
            contentFragment.replaceFragment(fragment, false);
        }
    }
}
