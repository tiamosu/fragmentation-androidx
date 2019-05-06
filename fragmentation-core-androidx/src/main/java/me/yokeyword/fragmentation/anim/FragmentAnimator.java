package me.yokeyword.fragmentation.anim;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.AnimRes;

/**
 * Fragment动画实体类
 * Created by YoKeyword on 16/2/4.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FragmentAnimator implements Parcelable {
    @AnimRes
    protected int mEnter;
    @AnimRes
    protected int mExit;
    @AnimRes
    protected int mPopEnter;
    @AnimRes
    protected int mPopExit;

    public FragmentAnimator() {
    }

    public FragmentAnimator(int enter, int exit) {
        this.mEnter = enter;
        this.mExit = exit;
    }

    public FragmentAnimator(int enter, int exit, int popEnter, int popExit) {
        this.mEnter = enter;
        this.mExit = exit;
        this.mPopEnter = popEnter;
        this.mPopExit = popExit;
    }

    public FragmentAnimator copy() {
        return new FragmentAnimator(getEnter(), getExit(), getPopEnter(), getPopExit());
    }

    public int getEnter() {
        return mEnter;
    }

    public FragmentAnimator setEnter(int enter) {
        this.mEnter = enter;
        return this;
    }

    public int getExit() {
        return mExit;
    }

    public FragmentAnimator setExit(int exit) {
        this.mExit = exit;
        return this;
    }

    public int getPopEnter() {
        return mPopEnter;
    }

    public FragmentAnimator setPopEnter(int popEnter) {
        this.mPopEnter = popEnter;
        return this;
    }

    public int getPopExit() {
        return mPopExit;
    }

    public FragmentAnimator setPopExit(int popExit) {
        this.mPopExit = popExit;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEnter);
        dest.writeInt(this.mExit);
        dest.writeInt(this.mPopEnter);
        dest.writeInt(this.mPopExit);
    }

    protected FragmentAnimator(Parcel in) {
        this.mEnter = in.readInt();
        this.mExit = in.readInt();
        this.mPopEnter = in.readInt();
        this.mPopExit = in.readInt();
    }

    public static final Creator<FragmentAnimator> CREATOR = new Creator<FragmentAnimator>() {
        @Override
        public FragmentAnimator createFromParcel(Parcel source) {
            return new FragmentAnimator(source);
        }

        @Override
        public FragmentAnimator[] newArray(int size) {
            return new FragmentAnimator[size];
        }
    };
}
