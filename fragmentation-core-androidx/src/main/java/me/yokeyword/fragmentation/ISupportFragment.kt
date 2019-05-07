package me.yokeyword.fragmentation

import android.os.Bundle
import androidx.annotation.IntDef
import me.yokeyword.fragmentation.anim.FragmentAnimator

/**
 * Created by YoKey on 17/6/23.
 */
interface ISupportFragment {

    fun getSupportDelegate(): SupportFragmentDelegate

    fun extraTransaction(): ExtraTransaction

    fun enqueueAction(runnable: Runnable)

    fun post(runnable: Runnable)

    fun onEnterAnimationEnd(savedInstanceState: Bundle?)

    fun onLazyInitView(savedInstanceState: Bundle?)

    fun onSupportVisible()

    fun onSupportInvisible()

    fun isSupportVisible(): Boolean

    fun onCreateFragmentAnimator(): FragmentAnimator?

    fun getFragmentAnimator(): FragmentAnimator?

    fun setFragmentAnimator(fragmentAnimator: FragmentAnimator)

    fun setFragmentResult(resultCode: Int, bundle: Bundle?)

    fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?)

    fun onNewBundle(args: Bundle)

    fun putNewBundle(newBundle: Bundle?)

    fun onBackPressedSupport(): Boolean

    @IntDef(STANDARD, SINGLETOP, SINGLETASK)
    @Retention(AnnotationRetention.SOURCE)
    annotation class LaunchMode

    companion object {
        // LaunchMode
        const val STANDARD = 0
        const val SINGLETOP = 1
        const val SINGLETASK = 2

        // ResultCode
        const val RESULT_CANCELED = 0
        const val RESULT_OK = -1
    }
}
