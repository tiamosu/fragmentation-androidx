package me.yokeyword.fragmentation.anim;

import android.os.Parcel;
import android.os.Parcelable;

import me.yokeyword.fragmentation.R;

/**
 * Created by YoKeyword on 16/2/5.
 */
public class DefaultHorizontalAnimator extends FragmentAnimator implements Parcelable {

    public DefaultHorizontalAnimator() {
        mEnter = R.anim.h_fragment_enter;
        mExit = R.anim.h_fragment_exit;
        mPopEnter = R.anim.h_fragment_pop_enter;
        mPopExit = R.anim.h_fragment_pop_exit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    private DefaultHorizontalAnimator(Parcel in) {
        super(in);
    }

    public static final Creator<DefaultHorizontalAnimator> CREATOR = new Creator<DefaultHorizontalAnimator>() {
        @Override
        public DefaultHorizontalAnimator createFromParcel(Parcel source) {
            return new DefaultHorizontalAnimator(source);
        }

        @Override
        public DefaultHorizontalAnimator[] newArray(int size) {
            return new DefaultHorizontalAnimator[size];
        }
    };
}
