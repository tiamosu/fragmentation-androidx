package me.yokeyword.fragmentation.helper.internal;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import me.yokeyword.fragmentation.R;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

/**
 * @Hide Created by YoKeyword on 16/7/26.
 */
@SuppressWarnings("UnusedReturnValue")
public final class AnimatorHelper {
    public Animation mEnterAnim, mExitAnim, mPopEnterAnim, mPopExitAnim;
    private Animation mNoneAnim, mNoneAnimFixed;
    private Context mContext;
    private FragmentAnimator mFragmentAnimator;

    public AnimatorHelper(Context context, FragmentAnimator fragmentAnimator) {
        this.mContext = context;
        notifyChanged(fragmentAnimator);
    }

    public void notifyChanged(FragmentAnimator fragmentAnimator) {
        this.mFragmentAnimator = fragmentAnimator;
        initEnterAnim();
        initExitAnim();
        initPopEnterAnim();
        initPopExitAnim();
    }

    public Animation getNoneAnim() {
        if (mNoneAnim == null) {
            mNoneAnim = AnimationUtils.loadAnimation(mContext, R.anim.no_anim);
        }
        return mNoneAnim;
    }

    public Animation getNoneAnimFixed() {
        if (mNoneAnimFixed == null) {
            mNoneAnimFixed = new Animation() {
            };
        }
        return mNoneAnimFixed;
    }

    @Nullable
    public Animation compatChildFragmentExitAnim(Fragment fragment) {
        if ((fragment.getTag() != null
                && fragment.getTag().startsWith("android:switcher:")
                && fragment.getUserVisibleHint())
                || (fragment.getParentFragment() != null
                && fragment.getParentFragment().isRemoving()
                && !fragment.isHidden())) {
            final Animation animation = new Animation() {
            };
            animation.setDuration(mExitAnim.getDuration());
            return animation;
        }
        return null;
    }

    private Animation initEnterAnim() {
        if (mFragmentAnimator.getEnter() == 0) {
            mEnterAnim = AnimationUtils.loadAnimation(mContext, R.anim.no_anim);
        } else {
            mEnterAnim = AnimationUtils.loadAnimation(mContext, mFragmentAnimator.getEnter());
        }
        return mEnterAnim;
    }

    private Animation initExitAnim() {
        if (mFragmentAnimator.getExit() == 0) {
            mExitAnim = AnimationUtils.loadAnimation(mContext, R.anim.no_anim);
        } else {
            mExitAnim = AnimationUtils.loadAnimation(mContext, mFragmentAnimator.getExit());
        }
        return mExitAnim;
    }

    private Animation initPopEnterAnim() {
        if (mFragmentAnimator.getPopEnter() == 0) {
            mPopEnterAnim = AnimationUtils.loadAnimation(mContext, R.anim.no_anim);
        } else {
            mPopEnterAnim = AnimationUtils.loadAnimation(mContext, mFragmentAnimator.getPopEnter());
        }
        return mPopEnterAnim;
    }

    private Animation initPopExitAnim() {
        if (mFragmentAnimator.getPopExit() == 0) {
            mPopExitAnim = AnimationUtils.loadAnimation(mContext, R.anim.no_anim);
        } else {
            mPopExitAnim = AnimationUtils.loadAnimation(mContext, mFragmentAnimator.getPopExit());
        }
        return mPopExitAnim;
    }
}
