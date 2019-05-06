package me.yokeyword.fragmentation.helper.internal;

import android.view.View;

import java.util.ArrayList;

/**
 * @hide Created by YoKey on 16/11/25.
 */
public final class TransactionRecord {
    public String mTag;
    public int mTargetFragmentEnter = Integer.MIN_VALUE;
    public int mCurrentFragmentPopExit = Integer.MIN_VALUE;
    public int mCurrentFragmentPopEnter = Integer.MIN_VALUE;
    public int mTargetFragmentExit = Integer.MIN_VALUE;
    public boolean mDontAddToBackStack = false;
    public ArrayList<SharedElement> mSharedElementList;

    public static class SharedElement {
        public View mSharedElement;
        public String mSharedName;

        public SharedElement(View sharedElement, String sharedName) {
            this.mSharedElement = sharedElement;
            this.mSharedName = sharedName;
        }
    }
}
