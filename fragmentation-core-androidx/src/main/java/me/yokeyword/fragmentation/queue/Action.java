package me.yokeyword.fragmentation.queue;

import androidx.fragment.app.FragmentManager;

/**
 * Created by YoKey on 17/12/28.
 */
@SuppressWarnings("WeakerAccess")
public abstract class Action {
    public static final long DEFAULT_POP_TIME = 300L;
    public static final int ACTION_NORMAL = 0;
    public static final int ACTION_POP = 1;
    public static final int ACTION_POP_MOCK = 2;
    public static final int ACTION_BACK = 3;
    public static final int ACTION_LOAD = 4;

    public FragmentManager mFragmentManager;
    public int mAction = ACTION_NORMAL;
    public long mDuration = 0;

    public Action() {
    }

    public Action(int action) {
        this.mAction = action;
    }

    public Action(int action, FragmentManager fragmentManager) {
        this(action);
        this.mFragmentManager = fragmentManager;
    }

    public abstract void run();
}
