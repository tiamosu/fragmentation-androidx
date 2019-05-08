package me.yokeyword.fragmentation

import android.view.MotionEvent

import me.yokeyword.fragmentation.anim.FragmentAnimator

/**
 * Created by YoKey on 17/6/13.
 */
interface ISupportActivity {

    fun getSupportDelegate(): SupportActivityDelegate

    fun extraTransaction(): ExtraTransaction

    fun getFragmentAnimator(): FragmentAnimator?

    fun setFragmentAnimator(fragmentAnimator: FragmentAnimator?)

    fun onCreateFragmentAnimator(): FragmentAnimator?

    fun post(runnable: Runnable)

    fun onBackPressed()

    fun onBackPressedSupport()

    fun dispatchTouchEvent(ev: MotionEvent): Boolean
}
