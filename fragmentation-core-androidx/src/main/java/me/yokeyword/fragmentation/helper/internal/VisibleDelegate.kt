package me.yokeyword.fragmentation.helper.internal

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentationMagician
import me.yokeyword.fragmentation.ISupportFragment

/**
 * Created by YoKey on 17/4/4.
 */
class VisibleDelegate(private val mSupportF: ISupportFragment) {
    // SupportVisible相关
    private var isSupportVisible = false
    private var needDispatch = true
    private var invisibleWhenLeave = false
    private var isFirstVisible = true
    private var firstCreateViewCompatReplace = true
    private var abortInitVisible = false
    private var taskDispatchSupportVisible: Runnable? = null

    private var handler: Handler? = null
    private var saveInstanceState: Bundle? = null
    private val fragment = mSupportF as Fragment

    fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            saveInstanceState = savedInstanceState
            // setUserVisibleHint() may be called before onCreate()
            invisibleWhenLeave = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE)
            firstCreateViewCompatReplace = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE)
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE, invisibleWhenLeave)
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE, firstCreateViewCompatReplace)
    }

    fun onActivityCreated() {
        if (!firstCreateViewCompatReplace && fragment.tag != null && fragment.tag!!.startsWith("android:switcher:")) {
            return
        }
        if (firstCreateViewCompatReplace) {
            firstCreateViewCompatReplace = false
        }
        initVisible()
    }

    private fun initVisible() {
        if (!invisibleWhenLeave && isFragmentVisible(fragment)) {
            if (fragment.parentFragment == null || isFragmentVisible(fragment.parentFragment!!)) {
                needDispatch = false
                safeDispatchUserVisibleHint(true)
            }
        }
    }

    fun onResume() {
        if (!isFirstVisible) {
            if (!isSupportVisible && !invisibleWhenLeave && isFragmentVisible(fragment)) {
                needDispatch = false
                dispatchSupportVisible(true)
            }
        } else {
            if (abortInitVisible) {
                abortInitVisible = false
                initVisible()
            }
        }
    }

    fun onPause() {
        if (taskDispatchSupportVisible != null) {
            getHandler().removeCallbacks(taskDispatchSupportVisible!!)
            abortInitVisible = true
            return
        }

        if (isSupportVisible && isFragmentVisible(fragment)) {
            needDispatch = false
            invisibleWhenLeave = false
            dispatchSupportVisible(false)
        } else {
            invisibleWhenLeave = true
        }
    }

    fun onHiddenChanged(hidden: Boolean) {
        if (!hidden && !fragment.isResumed) {
            //if fragment is shown but not resumed, ignore...
            onFragmentShownWhenNotResumed()
            return
        }
        if (hidden) {
            safeDispatchUserVisibleHint(false)
        } else {
            enqueueDispatchVisible()
        }
    }

    private fun onFragmentShownWhenNotResumed() {
        invisibleWhenLeave = false
        dispatchChildOnFragmentShownWhenNotResumed()
    }

    private fun dispatchChildOnFragmentShownWhenNotResumed() {
        val fragmentManager: FragmentManager = fragment.childFragmentManager
        val childFragments = FragmentationMagician.getAddedFragments(fragmentManager) ?: return
        for (child in childFragments) {
            if (child is ISupportFragment && isFragmentVisible(child)) {
                child.getSupportDelegate().getVisibleDelegate().onFragmentShownWhenNotResumed()
            }
        }
    }

    fun onDestroyView() {
        isFirstVisible = true
    }

    fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (fragment.isResumed || !fragment.isAdded && isVisibleToUser) {
            if (!isSupportVisible && isVisibleToUser) {
                safeDispatchUserVisibleHint(true)
            } else if (isSupportVisible && !isVisibleToUser) {
                dispatchSupportVisible(false)
            }
        }
    }

    private fun safeDispatchUserVisibleHint(visible: Boolean) {
        if (isFirstVisible) {
            if (!visible) {
                return
            }
            enqueueDispatchVisible()
        } else {
            dispatchSupportVisible(visible)
        }
    }

    private fun enqueueDispatchVisible() {
        taskDispatchSupportVisible = Runnable {
            taskDispatchSupportVisible = null
            dispatchSupportVisible(true)
        }
        getHandler().post(taskDispatchSupportVisible!!)
    }

    private fun dispatchSupportVisible(visible: Boolean) {
        if (visible && isParentInvisible()) {
            needDispatch = true
            return
        }
        if (isSupportVisible == visible) {
            needDispatch = true
            return
        }

        isSupportVisible = visible

        if (visible) {
            if (checkAddState()) {
                return
            }
            mSupportF.onSupportVisible()

            if (isFirstVisible) {
                isFirstVisible = false
                mSupportF.onLazyInitView(saveInstanceState)
            }
            dispatchChild(true)
        } else {
            dispatchChild(false)
            mSupportF.onSupportInvisible()
        }
    }

    private fun dispatchChild(visible: Boolean) {
        if (!needDispatch) {
            needDispatch = true
        } else {
            if (checkAddState()) {
                return
            }
            val fragmentManager = fragment.childFragmentManager
            val childFragments = FragmentationMagician.getAddedFragments(fragmentManager) ?: return
            for (child in childFragments) {
                if (child is ISupportFragment && isFragmentVisible(child)) {
                    child.getSupportDelegate().getVisibleDelegate().dispatchSupportVisible(visible)
                }
            }
        }
    }

    private fun isParentInvisible(): Boolean {
        val fragment = fragment.parentFragment as? ISupportFragment
        return fragment != null && !fragment.isSupportVisible()
    }

    private fun checkAddState(): Boolean {
        if (!fragment.isAdded) {
            isSupportVisible = !isSupportVisible
            return true
        }
        return false
    }

    private fun isFragmentVisible(fragment: Fragment): Boolean {
        return !fragment.isHidden && fragment.userVisibleHint
    }

    fun isSupportVisible(): Boolean {
        return isSupportVisible
    }

    private fun getHandler(): Handler {
        if (handler == null) {
            handler = Handler(Looper.getMainLooper())
        }
        return handler!!
    }

    companion object {
        private const val FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE = "fragmentation_invisible_when_leave"
        private const val FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE = "fragmentation_compat_replace"
    }
}
