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
    private var mIsSupportVisible: Boolean = false
    private var mNeedDispatch = true
    private var mInvisibleWhenLeave: Boolean = false
    private var mIsFirstVisible = true
    private var mFirstCreateViewCompatReplace = true
    private var mAbortInitVisible = false
    private var mTaskDispatchSupportVisible: Runnable? = null

    private var mHandler: Handler? = null
    private var mSaveInstanceState: Bundle? = null
    private val mFragment: Fragment = mSupportF as Fragment

    fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            mSaveInstanceState = savedInstanceState
            // setUserVisibleHint() may be called before onCreate()
            mInvisibleWhenLeave = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE)
            mFirstCreateViewCompatReplace = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE)
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE, mInvisibleWhenLeave)
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE, mFirstCreateViewCompatReplace)
    }

    fun onActivityCreated() {
        if (!mFirstCreateViewCompatReplace && mFragment.tag != null && mFragment.tag!!.startsWith("android:switcher:")) {
            return
        }
        if (mFirstCreateViewCompatReplace) {
            mFirstCreateViewCompatReplace = false
        }
        initVisible()
    }

    private fun initVisible() {
        if (!mInvisibleWhenLeave && !mFragment.isHidden && mFragment.userVisibleHint) {
            if (mFragment.parentFragment == null || isFragmentVisible(mFragment.parentFragment!!)) {
                mNeedDispatch = false
                safeDispatchUserVisibleHint(true)
            }
        }
    }

    fun onResume() {
        if (!mIsFirstVisible) {
            if (!mIsSupportVisible && !mInvisibleWhenLeave && isFragmentVisible(mFragment)) {
                mNeedDispatch = false
                dispatchSupportVisible(true)
            }
        } else {
            if (mAbortInitVisible) {
                mAbortInitVisible = false
                initVisible()
            }
        }
    }

    fun onPause() {
        if (mTaskDispatchSupportVisible != null) {
            getHandler().removeCallbacks(mTaskDispatchSupportVisible)
            mAbortInitVisible = true
            return
        }

        if (mIsSupportVisible && isFragmentVisible(mFragment)) {
            mNeedDispatch = false
            mInvisibleWhenLeave = false
            dispatchSupportVisible(false)
        } else {
            mInvisibleWhenLeave = true
        }
    }

    fun onHiddenChanged(hidden: Boolean) {
        if (!hidden && !mFragment.isResumed) {
            //if fragment is shown but not resumed, ignore...
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
        mInvisibleWhenLeave = false
        dispatchChildOnFragmentShownWhenNotResumed()
    }

    private fun dispatchChildOnFragmentShownWhenNotResumed() {
        val fragmentManager: FragmentManager = mFragment.childFragmentManager
        val childFragments = FragmentationMagician.getActiveFragments(fragmentManager) ?: return
        for (child in childFragments) {
            if (child is ISupportFragment && !child.isHidden && child.userVisibleHint) {
                child.getSupportDelegate().getVisibleDelegate().onFragmentShownWhenNotResumed()
            }
        }
    }

    fun onDestroyView() {
        mIsFirstVisible = true
    }

    fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (mFragment.isResumed || !mFragment.isAdded && isVisibleToUser) {
            if (!mIsSupportVisible && isVisibleToUser) {
                safeDispatchUserVisibleHint(true)
            } else if (mIsSupportVisible && !isVisibleToUser) {
                dispatchSupportVisible(false)
            }
        }
    }

    private fun safeDispatchUserVisibleHint(visible: Boolean) {
        if (mIsFirstVisible) {
            if (!visible) {
                return
            }
            enqueueDispatchVisible()
        } else {
            dispatchSupportVisible(visible)
        }
    }

    private fun enqueueDispatchVisible() {
        mTaskDispatchSupportVisible = Runnable {
            mTaskDispatchSupportVisible = null
            dispatchSupportVisible(true)
        }
        getHandler().post(mTaskDispatchSupportVisible)
    }

    private fun dispatchSupportVisible(visible: Boolean) {
        if (visible && isParentInvisible()) {
            mNeedDispatch = true
            return
        }
        if (mIsSupportVisible == visible) {
            mNeedDispatch = true
            return
        }

        mIsSupportVisible = visible

        if (visible) {
            if (checkAddState()) {
                return
            }
            mSupportF.onSupportVisible()

            if (mIsFirstVisible) {
                mIsFirstVisible = false
                mSupportF.onLazyInitView(mSaveInstanceState)
            }
            dispatchChild(true)
        } else {
            dispatchChild(false)
            mSupportF.onSupportInvisible()
        }
    }

    private fun dispatchChild(visible: Boolean) {
        if (!mNeedDispatch) {
            mNeedDispatch = true
        } else {
            if (checkAddState()) {
                return
            }
            val fragmentManager = mFragment.childFragmentManager
            val childFragments = FragmentationMagician.getActiveFragments(fragmentManager) ?: return
            for (child in childFragments) {
                if (child is ISupportFragment && !child.isHidden && child.userVisibleHint) {
                    child.getSupportDelegate().getVisibleDelegate().dispatchSupportVisible(visible)
                }
            }
        }
    }

    private fun isParentInvisible(): Boolean {
        val fragment = mFragment.parentFragment as? ISupportFragment
        return fragment != null && !fragment.isSupportVisible()
    }

    private fun checkAddState(): Boolean {
        if (!mFragment.isAdded) {
            mIsSupportVisible = !mIsSupportVisible
            return true
        }
        return false
    }

    private fun isFragmentVisible(fragment: Fragment): Boolean {
        return !fragment.isHidden && fragment.userVisibleHint
    }

    fun isSupportVisible(): Boolean {
        return mIsSupportVisible
    }

    private fun getHandler(): Handler {
        if (mHandler == null) {
            mHandler = Handler(Looper.getMainLooper())
        }
        return mHandler!!
    }

    companion object {
        private const val FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE = "fragmentation_invisible_when_leave"
        private const val FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE = "fragmentation_compat_replace"
    }
}
