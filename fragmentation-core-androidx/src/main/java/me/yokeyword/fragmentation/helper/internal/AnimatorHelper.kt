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
class AnimatorHelper(private val context: Context, fragmentAnimator: FragmentAnimator?) {
    var enterAnim: Animation? = null
    var exitAnim: Animation? = null
    var popEnterAnim: Animation? = null
    var popExitAnim: Animation? = null

    val noneAnim: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.no_anim) }
    val noneAnimFixed: Animation by lazy { object : Animation() {} }

    private var fragmentAnimator: FragmentAnimator? = null

    init {
        notifyChanged(fragmentAnimator)
    }

    fun notifyChanged(fragmentAnimator: FragmentAnimator?) {
        this.fragmentAnimator = fragmentAnimator
        initEnterAnim()
        initExitAnim()
        initPopEnterAnim()
        initPopExitAnim()
    }

    @Suppress("DEPRECATION")
    fun compatChildFragmentExitAnim(fragment: Fragment?): Animation? {
        fragment ?: return null

        if ((fragment.tag?.startsWith("android:switcher:") == true && fragment.userVisibleHint)
                || (fragment.parentFragment?.isRemoving == true && !fragment.isHidden)) {
            val animation = object : Animation() {}
            animation.duration = exitAnim?.duration ?: 0
            return animation
        }
        return null
    }

    private fun initEnterAnim(): Animation? {
        enterAnim = if (fragmentAnimator?.getEnter() == 0) {
            AnimationUtils.loadAnimation(context, R.anim.no_anim)
        } else {
            AnimationUtils.loadAnimation(context, fragmentAnimator?.getEnter() ?: 0)
        }
        return enterAnim
    }

    private fun initExitAnim(): Animation? {
        exitAnim = if (fragmentAnimator?.getExit() == 0) {
            AnimationUtils.loadAnimation(context, R.anim.no_anim)
        } else {
            AnimationUtils.loadAnimation(context, fragmentAnimator?.getExit() ?: 0)
        }
        return exitAnim
    }

    private fun initPopEnterAnim(): Animation? {
        popEnterAnim = if (fragmentAnimator?.getPopEnter() == 0) {
            AnimationUtils.loadAnimation(context, R.anim.no_anim)
        } else {
            AnimationUtils.loadAnimation(context, fragmentAnimator?.getPopEnter() ?: 0)
        }
        return popEnterAnim
    }

    private fun initPopExitAnim(): Animation? {
        popExitAnim = if (fragmentAnimator?.getPopExit() == 0) {
            AnimationUtils.loadAnimation(context, R.anim.no_anim)
        } else {
            AnimationUtils.loadAnimation(context, fragmentAnimator?.getPopExit() ?: 0)
        }
        return popExitAnim
    }
}
