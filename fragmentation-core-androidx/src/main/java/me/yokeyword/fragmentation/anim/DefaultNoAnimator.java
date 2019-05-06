package me.yokeyword.fragmentation.anim;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by YoKeyword on 16/2/15.
 */
public class DefaultNoAnimator extends FragmentAnimator implements Parcelable {

    public DefaultNoAnimator() {
        mEnter = 0;
        mExit = 0;
        mPopEnter = 0;
        mPopExit = 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    private DefaultNoAnimator(Parcel in) {
        super(in);
    }

    public static final Creator<DefaultNoAnimator> CREATOR = new Creator<DefaultNoAnimator>() {
        @Override
        public DefaultNoAnimator createFromParcel(Parcel source) {
            return new DefaultNoAnimator(source);
        }

        @Override
        public DefaultNoAnimator[] newArray(int size) {
            return new DefaultNoAnimator[size];
        }
    };
}
