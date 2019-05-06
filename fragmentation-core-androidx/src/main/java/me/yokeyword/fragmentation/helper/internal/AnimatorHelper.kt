package me.yokeyword.fragmentation.helper.internal

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import me.yokeyword.fragmentation.R
import me.yokeyword.fragmentation.anim.FragmentAnimator

/**
 * @Hide Created by YoKeyword on 16/7/26.
 */
class AnimatorHelper(private val mContext: Context, fragmentAnimator: FragmentAnimator) {
    var mEnterAnim: Animation? = null
    var mExitAnim: Animation? = null
    var mPopEnterAnim: Animation? = null
    var mPopExitAnim: Animation? = null
    private var mNoneAnim: Animation? = null
    private var mNoneAnimFixed: Animation? = null
    private var mFragmentAnimator: FragmentAnimator? = null

    init {
        notifyChanged(fragmentAnimator)
    }

    fun notifyChanged(fragmentAnimator: FragmentAnimator?) {
        this.mFragmentAnimator = fragmentAnimator
        initEnterAnim()
        initExitAnim()
        initPopEnterAnim()
        initPopExitAnim()
    }

    fun getNoneAnim(): Animation {
        if (mNoneAnim == null) {
            mNoneAnim = AnimationUtils.loadAnimation(mContext, R.anim.no_anim)
        }
        return mNoneAnim!!
    }

    fun getNoneAnimFixed(): Animation {
        if (mNoneAnimFixed == null) {
            mNoneAnimFixed = object : Animation() {}
        }
        return mNoneAnimFixed!!
    }

    fun compatChildFragmentExitAnim(fragment: Fragment): Animation? {
        if ((fragment.tag != null
                        && fragment.tag!!.startsWith("android:switcher:")
                        && fragment.userVisibleHint) || (fragment.parentFragment != null
                        && fragment.parentFragment!!.isRemoving
                        && !fragment.isHidden)) {
            val animation = object : Animation() {}
            animation.duration = mExitAnim?.duration ?: 0
            return animation
        }
        return null
    }

    private fun initEnterAnim(): Animation? {
        mEnterAnim = if (mFragmentAnimator!!.getEnter() == 0) {
            AnimationUtils.loadAnimation(mContext, R.anim.no_anim)
        } else {
            AnimationUtils.loadAnimation(mContext, mFragmentAnimator!!.getEnter())
        }
        return mEnterAnim
    }

    private fun initExitAnim(): Animation? {
        mExitAnim = if (mFragmentAnimator!!.getExit() == 0) {
            AnimationUtils.loadAnimation(mContext, R.anim.no_anim)
        } else {
            AnimationUtils.loadAnimation(mContext, mFragmentAnimator!!.getExit())
        }
        return mExitAnim
    }

    private fun initPopEnterAnim(): Animation? {
        mPopEnterAnim = if (mFragmentAnimator!!.getPopEnter() == 0) {
            AnimationUtils.loadAnimation(mContext, R.anim.no_anim)
        } else {
            AnimationUtils.loadAnimation(mContext, mFragmentAnimator!!.getPopEnter())
        }
        return mPopEnterAnim
    }

    private fun initPopExitAnim(): Animation? {
        mPopExitAnim = if (mFragmentAnimator!!.getPopExit() == 0) {
            AnimationUtils.loadAnimation(mContext, R.anim.no_anim)
        } else {
            AnimationUtils.loadAnimation(mContext, mFragmentAnimator!!.getPopExit())
        }
        return mPopExitAnim
    }
}
