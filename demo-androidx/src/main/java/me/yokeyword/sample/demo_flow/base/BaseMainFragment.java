package me.yokeyword.sample.demo_flow.base;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import me.yokeyword.sample.R;

/**
 * Created by YoKeyword on 16/2/3.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BaseMainFragment extends MySupportFragment {

    protected OnFragmentOpenDrawerListener mOpenDraweListener;

    protected void initToolbarNav(Toolbar toolbar) {
        initToolbarNav(toolbar, false);
    }

    protected void initToolbarNav(Toolbar toolbar, boolean isHome) {
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            if (mOpenDraweListener != null) {
                mOpenDraweListener.onOpenDrawer();
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentOpenDrawerListener) {
            mOpenDraweListener = (OnFragmentOpenDrawerListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOpenDraweListener = null;
    }

    public interface OnFragmentOpenDrawerListener {
        void onOpenDrawer();
    }
}
