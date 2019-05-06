package me.yokeyword.fragmentation.anim;

import android.os.Parcel;
import android.os.Parcelable;

import me.yokeyword.fragmentation.R;

/**
 * Created by YoKeyword on 16/2/5.
 */
public class DefaultVerticalAnimator extends FragmentAnimator implements Parcelable {

    public DefaultVerticalAnimator() {
        mEnter = R.anim.v_fragment_enter;
        mExit = R.anim.v_fragment_exit;
        mPopEnter = R.anim.v_fragment_pop_enter;
        mPopExit = R.anim.v_fragment_pop_exit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    private DefaultVerticalAnimator(Parcel in) {
        super(in);
    }

    public static final Creator<DefaultVerticalAnimator> CREATOR = new Creator<DefaultVerticalAnimator>() {
        @Override
        public DefaultVerticalAnimator createFromParcel(Parcel source) {
            return new DefaultVerticalAnimator(source);
        }

        @Override
        public DefaultVerticalAnimator[] newArray(int size) {
            return new DefaultVerticalAnimator[size];
        }
    };
}
