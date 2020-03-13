package me.yokeyword.fragmentation

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import me.yokeyword.fragmentation.anim.FragmentAnimator
import me.yokeyword.fragmentation.helper.internal.AnimatorHelper
import me.yokeyword.fragmentation.helper.internal.ResultRecord
import me.yokeyword.fragmentation.helper.internal.TransactionRecord
import me.yokeyword.fragmentation.helper.internal.VisibleDelegate

@Suppress("unused", "UNUSED_PARAMETER")
class SupportFragmentDelegate(private val mSupportF: ISupportFragment) {
    private lateinit var mActivity: FragmentActivity
    internal var mFragmentAnimator: FragmentAnimator? = null
    internal var mAnimHelper: AnimatorHelper? = null
    internal var mLockAnim = false
    internal var mContainerId = 0
    internal var mTransactionRecord: TransactionRecord? = null
    internal var mNewBundle: Bundle? = null
    internal var mAnimByActivity = true
    internal var mEnterAnimListener: EnterAnimListener? = null
    private var mRootStatus = STATUS_UN_ROOT
    private var mIsSharedElement = false
    private var mCustomEnterAnim = Integer.MIN_VALUE
    private var mCustomExitAnim = Integer.MIN_VALUE
    private var mCustomPopExitAnim = Integer.MIN_VALUE
    private var mHandler: Handler? = null
    private var mFirstCreateView = true
    private var mReplaceMode = false
    private var mIsHidden = true
    private var mTransactionDelegate: TransactionDelegate? = null

    // SupportVisible
    private var mVisibleDelegate: VisibleDelegate? = null
    private var mSaveInstanceState: Bundle? = null
    private var mFragment: Fragment? = null
    private var mSupportA: ISupportActivity? = null
    private var mRootViewClickable = false

    private val mNotifyEnterAnimEndRunnable = Runnable {
        if (mFragment == null) {
            return@Runnable
        }
        mSupportF.onEnterAnimationEnd(mSaveInstanceState)

        if (mRootViewClickable) {
            return@Runnable
        }
        val view = mFragment?.view ?: return@Runnable
        val preFragment = SupportHelper.getPreFragment(mFragment) ?: return@Runnable

        val prePopExitDuration = preFragment.getSupportDelegate().getPopExitAnimDuration()
        val enterDuration = getEnterAnimDuration()

        getHandler().postDelayed({ view.isClickable = false }, prePopExitDuration - enterDuration)
    }

    init {
        if (mSupportF !is Fragment) {
            throw RuntimeException("Must extends Fragment")
        }
        this.mFragment = mSupportF
    }

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义Tag，添加SharedElement动画，操作非回退栈Fragment
     */
    fun extraTransaction(): ExtraTransaction {
        if (mTransactionDelegate == null) {
            throw RuntimeException(mFragment?.javaClass?.simpleName + " not attach!")
        }
        return ExtraTransaction.ExtraTransactionImpl((mSupportA as? FragmentActivity),
                mSupportF, mTransactionDelegate, false)
    }

    fun onAttach() {
        val activity = mFragment?.activity
        if (activity !is ISupportActivity) {
            throw RuntimeException(activity?.javaClass?.simpleName
                    ?: "fragment" + " must impl ISupportActivity!")
        }
        this.mSupportA = activity
        this.mActivity = activity
        mTransactionDelegate = mSupportA?.getSupportDelegate()?.getTransactionDelegate()
    }

    fun onCreate(savedInstanceState: Bundle?) {
        getVisibleDelegate().onCreate(savedInstanceState)

        mFragment?.arguments?.apply {
            mRootStatus = getInt(TransactionDelegate.FRAGMENTATION_ARG_ROOT_STATUS, STATUS_UN_ROOT)
            mIsSharedElement = getBoolean(TransactionDelegate.FRAGMENTATION_ARG_IS_SHARED_ELEMENT, false)
            mContainerId = getInt(TransactionDelegate.FRAGMENTATION_ARG_CONTAINER)
            mReplaceMode = getBoolean(TransactionDelegate.FRAGMENTATION_ARG_REPLACE, false)
            mCustomEnterAnim = getInt(TransactionDelegate.FRAGMENTATION_ARG_CUSTOM_ENTER_ANIM, Integer.MIN_VALUE)
            mCustomExitAnim = getInt(TransactionDelegate.FRAGMENTATION_ARG_CUSTOM_EXIT_ANIM, Integer.MIN_VALUE)
            mCustomPopExitAnim = getInt(TransactionDelegate.FRAGMENTATION_ARG_CUSTOM_POP_EXIT_ANIM, Integer.MIN_VALUE)
        }

        if (savedInstanceState == null) {
            getFragmentAnimator()
        } else {
            savedInstanceState.classLoader = javaClass.classLoader
            mSaveInstanceState = savedInstanceState
            mFragmentAnimator = savedInstanceState.getParcelable(TransactionDelegate.FRAGMENTATION_STATE_SAVE_ANIMATOR)
            mIsHidden = savedInstanceState.getBoolean(TransactionDelegate.FRAGMENTATION_STATE_SAVE_IS_HIDDEN)
            mContainerId = savedInstanceState.getInt(TransactionDelegate.FRAGMENTATION_ARG_CONTAINER)
        }

        mAnimHelper = AnimatorHelper(mActivity.applicationContext, mFragmentAnimator)

        val enter = getEnterAnim() ?: return

        getEnterAnim()?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                mSupportA?.getSupportDelegate()?.mFragmentClickable = false  // 开启防抖动

                getHandler().postDelayed({ mSupportA?.getSupportDelegate()?.mFragmentClickable = true }, enter.duration)
            }

            override fun onAnimationEnd(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (mSupportA?.getSupportDelegate()?.mPopMultipleNoAnim == true || mLockAnim) {
            return if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE && enter) {
                mAnimHelper?.getNoneAnimFixed()
            } else mAnimHelper?.getNoneAnim()
        }
        when (transit) {
            FragmentTransaction.TRANSIT_FRAGMENT_OPEN -> return if (enter) {
                val enterAnim: Animation?
                if (mRootStatus == STATUS_ROOT_ANIM_DISABLE) {
                    enterAnim = mAnimHelper?.getNoneAnim()
                } else {
                    enterAnim = mAnimHelper?.mEnterAnim
                    fixAnimationListener(enterAnim)
                }
                enterAnim
            } else {
                mAnimHelper?.mPopExitAnim
            }
            FragmentTransaction.TRANSIT_FRAGMENT_CLOSE -> return if (enter) mAnimHelper?.mPopEnterAnim else mAnimHelper?.mExitAnim
            else -> {
                if (mIsSharedElement && enter) {
                    compatSharedElements()
                }

                return if (!enter) {
                    mAnimHelper?.compatChildFragmentExitAnim(mFragment)
                } else null
            }
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        getVisibleDelegate().onSaveInstanceState(outState)
        outState.putParcelable(TransactionDelegate.FRAGMENTATION_STATE_SAVE_ANIMATOR, mFragmentAnimator)
        outState.putBoolean(TransactionDelegate.FRAGMENTATION_STATE_SAVE_IS_HIDDEN, mFragment!!.isHidden)
        outState.putInt(TransactionDelegate.FRAGMENTATION_ARG_CONTAINER, mContainerId)
    }

    fun onActivityCreated(savedInstanceState: Bundle?) {
        getVisibleDelegate().onActivityCreated()

        val view = mFragment?.view
        if (view != null) {
            mRootViewClickable = view.isClickable
            view.isClickable = true
            setBackground(view)
        }

        if (savedInstanceState != null
                || mRootStatus == STATUS_ROOT_ANIM_DISABLE
                || mFragment?.tag != null && mFragment!!.tag!!.startsWith("android:switcher:")
                || mReplaceMode && !mFirstCreateView) {
            notifyEnterAnimEnd()
        } else if (mCustomEnterAnim != Integer.MIN_VALUE) {
            fixAnimationListener(if (mCustomEnterAnim == 0)
                mAnimHelper?.getNoneAnim()
            else
                AnimationUtils.loadAnimation(mActivity, mCustomEnterAnim))
        }

        if (mFirstCreateView) {
            mFirstCreateView = false
        }
    }

    fun onResume() {
        getVisibleDelegate().onResume()
    }

    fun onPause() {
        getVisibleDelegate().onPause()
    }

    fun onDestroyView() {
        mSupportA?.getSupportDelegate()?.mFragmentClickable = true
        getVisibleDelegate().onDestroyView()
        getHandler().removeCallbacks(mNotifyEnterAnimEndRunnable)
    }

    fun onDestroy() {
        mTransactionDelegate?.handleResultRecord(mFragment)
    }

    fun onHiddenChanged(hidden: Boolean) {
        getVisibleDelegate().onHiddenChanged(hidden)
    }

    fun setUserVisibleHint(isVisibleToUser: Boolean) {
        getVisibleDelegate().setUserVisibleHint(isVisibleToUser)
    }

    /**
     * Causes the Runnable r to be added to the action queue.
     *
     *
     * The runnable will be run after all the previous action has been run.
     *
     *
     * 前面的事务全部执行后 执行该Action
     */
    fun post(runnable: Runnable) {
        mTransactionDelegate?.post(runnable)
    }

    /**
     * Called when the enter-animation end.
     * 入栈动画 结束时,回调
     */
    @Suppress("UNUSED_PARAMETER")
    fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
    }

    /**
     * Lazy initial，Called when fragment is first visible.
     *
     *
     * 同级下的 懒加载 ＋ ViewPager下的懒加载  的结合回调方法
     */
    @Suppress("UNUSED_PARAMETER")
    fun onLazyInitView(savedInstanceState: Bundle?) {
    }

    /**
     * Called when the fragment is visible.
     *
     *
     * 当Fragment对用户可见时回调
     *
     *
     * Is the combination of  [onHiddenChanged() + onResume()/onPause() + setUserVisibleHint()]
     */
    fun onSupportVisible() {}

    /**
     * Called when the fragment is invivible.
     *
     *
     * Is the combination of  [onHiddenChanged() + onResume()/onPause() + setUserVisibleHint()]
     */
    fun onSupportInvisible() {}

    /**
     * Return true if the fragment has been supportVisible.
     */
    fun isSupportVisible(): Boolean {
        return getVisibleDelegate().isSupportVisible()
    }

    /**
     * Set fragment animation with a higher priority than the ISupportActivity
     * 设定当前Fragmemt动画,优先级比在ISupportActivity里高
     */
    fun onCreateFragmentAnimator(): FragmentAnimator? {
        return mSupportA!!.getFragmentAnimator()
    }

    /**
     * 获取设置的全局动画
     *
     * @return FragmentAnimator
     */
    fun getFragmentAnimator(): FragmentAnimator? {
        if (mFragmentAnimator == null) {
            mFragmentAnimator = mSupportF.onCreateFragmentAnimator()
            if (mFragmentAnimator == null) {
                mFragmentAnimator = mSupportA?.getFragmentAnimator()
            }
        }
        return mFragmentAnimator
    }

    /**
     * Set the fragment animation.
     */
    fun setFragmentAnimator(fragmentAnimator: FragmentAnimator?) {
        this.mFragmentAnimator = fragmentAnimator
        mAnimHelper?.notifyChanged(fragmentAnimator)
        mAnimByActivity = false
    }

    /**
     * 类似 [Activity.setResult]
     *
     *
     * Similar to [Activity.setResult]
     *
     * @see .startForResult
     */
    fun setFragmentResult(resultCode: Int, bundle: Bundle?) {
        val args = mFragment?.arguments
        if (args == null || !args.containsKey(TransactionDelegate.FRAGMENTATION_ARG_RESULT_RECORD)) {
            return
        }

        val resultRecord = args.getParcelable<ResultRecord>(TransactionDelegate.FRAGMENTATION_ARG_RESULT_RECORD)
        if (resultRecord != null) {
            resultRecord.mResultCode = resultCode
            resultRecord.mResultBundle = bundle
        }
    }

    /**
     * 类似  [Activity.onActivityResult]
     *
     *
     * Similar to [Activity.onActivityResult]
     *
     * @see .startForResult
     */
    fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {}

    /**
     * 在start(TargetFragment,LaunchMode)时,启动模式为SingleTask/SingleTop, 回调TargetFragment的该方法
     * 类似 [Activity.onNewIntent]
     *
     *
     * Similar to [Activity.onNewIntent]
     *
     * @param args putNewBundle(Bundle newBundle)
     * @see .start
     */
    fun onNewBundle(args: Bundle) {}

    /**
     * 添加NewBundle,用于启动模式为SingleTask/SingleTop时
     *
     * @see .start
     */
    fun putNewBundle(newBundle: Bundle?) {
        this.mNewBundle = newBundle
    }

    /**********************************************************************************************/

    /**
     * Back Event
     *
     * @return false则继续向上传递, true则消费掉该事件
     */
    fun onBackPressedSupport(): Boolean {
        return false
    }

    /**
     * 隐藏软键盘
     */
    fun hideSoftInput() {
        val activity = mFragment?.activity ?: return
        val view = activity.window.decorView
        SupportHelper.hideSoftInput(view)
    }

    /**
     * 显示软键盘,调用该方法后,会在onPause时自动隐藏软键盘
     */
    fun showSoftInput(view: View) {
        SupportHelper.showSoftInput(view)
    }

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     */
    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment?) {
        loadRootFragment(containerId, toFragment, true, false)
    }

    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment?,
                         addToBackStack: Boolean, allowAnim: Boolean) {
        mTransactionDelegate?.loadRootTransaction(getChildFragmentManager(), containerId,
                toFragment, addToBackStack, allowAnim)
    }

    /**
     * 加载多个同级根Fragment,类似Wechat, QQ主页的场景
     */
    fun loadMultipleRootFragment(containerId: Int, showPosition: Int,
                                 toFragments: Array<out ISupportFragment?>) {
        mTransactionDelegate?.loadMultipleRootTransaction(getChildFragmentManager(),
                containerId, showPosition, toFragments)
    }

    /**
     * show一个Fragment,hide其他同栈所有Fragment
     * 使用该方法时，要确保同级栈内无多余的Fragment,(只有通过loadMultipleRootFragment()载入的Fragment)
     *
     *
     * 建议使用更明确的[.showHideFragment]
     */
    fun showHideFragment(showFragment: ISupportFragment?) {
        showHideFragment(showFragment, null)
    }

    /**
     * show一个Fragment,hide一个Fragment ; 主要用于类似微信主页那种 切换tab的情况
     */
    fun showHideFragment(showFragment: ISupportFragment?, hideFragment: ISupportFragment?) {
        mTransactionDelegate?.showHideFragment(getChildFragmentManager(), showFragment, hideFragment)
    }

    fun start(toFragment: ISupportFragment?) {
        start(toFragment, ISupportFragment.STANDARD)
    }

    /**
     * @param launchMode Similar to Activity's LaunchMode.
     */
    fun start(toFragment: ISupportFragment?, @ISupportFragment.LaunchMode launchMode: Int) {
        mTransactionDelegate?.dispatchStartTransaction(mFragment!!.fragmentManager, mSupportF,
                toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD)
    }

    /**
     * Launch an fragment for which you would like a result when it poped.
     */
    fun startForResult(toFragment: ISupportFragment?, requestCode: Int) {
        mTransactionDelegate?.dispatchStartTransaction(mFragment!!.fragmentManager, mSupportF,
                toFragment, requestCode, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT)
    }

    /**
     * Start the target Fragment and pop itself
     */
    fun startWithPop(toFragment: ISupportFragment?) {
        mTransactionDelegate?.startWithPop(mFragment!!.fragmentManager, mSupportF, toFragment)
    }

    fun startWithPopTo(toFragment: ISupportFragment?, targetFragmentClass: Class<*>?, includeTargetFragment: Boolean) {
        mTransactionDelegate?.startWithPopTo(mFragment!!.fragmentManager, mSupportF, toFragment,
                targetFragmentClass?.name, includeTargetFragment)
    }

    fun replaceFragment(toFragment: ISupportFragment?, addToBackStack: Boolean) {
        mTransactionDelegate?.dispatchStartTransaction(mFragment!!.fragmentManager, mSupportF,
                toFragment, 0, ISupportFragment.STANDARD, if (addToBackStack)
            TransactionDelegate.TYPE_REPLACE else TransactionDelegate.TYPE_REPLACE_DONT_BACK)
    }

    fun startChild(toFragment: ISupportFragment?) {
        startChild(toFragment, ISupportFragment.STANDARD)
    }

    fun startChild(toFragment: ISupportFragment?, @ISupportFragment.LaunchMode launchMode: Int) {
        mTransactionDelegate?.dispatchStartTransaction(getChildFragmentManager(), getTopFragment(),
                toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD)
    }

    fun startChildForResult(toFragment: ISupportFragment?, requestCode: Int) {
        mTransactionDelegate?.dispatchStartTransaction(getChildFragmentManager(), getTopFragment(),
                toFragment, requestCode, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT)
    }

    fun startChildWithPop(toFragment: ISupportFragment?) {
        mTransactionDelegate?.startWithPop(getChildFragmentManager(), getTopFragment(), toFragment)
    }

    fun replaceChildFragment(toFragment: ISupportFragment?, addToBackStack: Boolean) {
        mTransactionDelegate?.dispatchStartTransaction(getChildFragmentManager(), getTopFragment(),
                toFragment, 0, ISupportFragment.STANDARD, if (addToBackStack)
            TransactionDelegate.TYPE_REPLACE else TransactionDelegate.TYPE_REPLACE_DONT_BACK)
    }

    fun pop() {
        mTransactionDelegate?.pop(mFragment!!.fragmentManager)
    }

    /**
     * Pop the child fragment.
     */
    fun popChild() {
        mTransactionDelegate?.pop(getChildFragmentManager())
    }

    /**
     * Pop the last fragment transition from the manager's fragment
     * back stack.
     *
     *
     * 出栈到目标fragment
     *
     * @param targetFragmentClass   目标fragment
     * @param includeTargetFragment 是否包含该fragment
     */
    fun popTo(targetFragmentClass: Class<*>?, includeTargetFragment: Boolean) {
        popTo(targetFragmentClass, includeTargetFragment, null)
    }

    /**
     * If you want to begin another FragmentTransaction immediately after popTo(), use this method.
     * 如果你想在出栈后, 立刻进行FragmentTransaction操作，请使用该方法
     */
    fun popTo(targetFragmentClass: Class<*>?, includeTargetFragment: Boolean, afterPopTransactionRunnable: Runnable?) {
        popTo(targetFragmentClass, includeTargetFragment,
                afterPopTransactionRunnable, TransactionDelegate.DEFAULT_POPTO_ANIM)
    }

    fun popTo(targetFragmentClass: Class<*>?, includeTargetFragment: Boolean,
              afterPopTransactionRunnable: Runnable?, popAnim: Int) {
        mTransactionDelegate?.popTo(targetFragmentClass?.name, includeTargetFragment,
                afterPopTransactionRunnable, mFragment?.fragmentManager, popAnim)
    }

    fun popToChild(targetFragmentClass: Class<*>?, includeTargetFragment: Boolean) {
        popToChild(targetFragmentClass, includeTargetFragment, null)
    }

    fun popToChild(targetFragmentClass: Class<*>?, includeTargetFragment: Boolean,
                   afterPopTransactionRunnable: Runnable?) {
        popToChild(targetFragmentClass, includeTargetFragment,
                afterPopTransactionRunnable, TransactionDelegate.DEFAULT_POPTO_ANIM)
    }

    fun popToChild(targetFragmentClass: Class<*>?, includeTargetFragment: Boolean,
                   afterPopTransactionRunnable: Runnable?, popAnim: Int) {
        mTransactionDelegate?.popTo(targetFragmentClass?.name, includeTargetFragment,
                afterPopTransactionRunnable, getChildFragmentManager(), popAnim)
    }

    fun popQuiet() {
        mTransactionDelegate?.popQuiet(mFragment?.fragmentManager, mFragment)
    }

    private fun getChildFragmentManager(): FragmentManager? {
        return mFragment?.childFragmentManager
    }

    private fun getTopFragment(): ISupportFragment? {
        return SupportHelper.getTopFragment(getChildFragmentManager())
    }

    private fun fixAnimationListener(enterAnim: Animation?) {
        // AnimationListener is not reliable.
        getHandler().postDelayed(mNotifyEnterAnimEndRunnable, enterAnim?.duration ?: 0)
        mSupportA?.getSupportDelegate()?.mFragmentClickable = true

        if (mEnterAnimListener != null) {
            getHandler().post {
                mEnterAnimListener!!.onEnterAnimStart()
                mEnterAnimListener = null
            }
        }
    }

    private fun compatSharedElements() {
        notifyEnterAnimEnd()
    }

    fun setBackground(view: View) {
        if (mFragment?.tag != null && mFragment!!.tag!!.startsWith("android:switcher:") ||
                mRootStatus != STATUS_UN_ROOT || view.background != null) {
            return
        }

        val defaultBg = mSupportA?.getSupportDelegate()?.getDefaultFragmentBackground() ?: 0
        if (defaultBg == 0) {
            val background = getWindowBackground()
            view.setBackgroundResource(background)
        } else {
            view.setBackgroundResource(defaultBg)
        }
    }

    private fun getWindowBackground(): Int {
        val a = mActivity.theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
        val background = a.getResourceId(0, 0)
        a.recycle()
        return background
    }

    private fun notifyEnterAnimEnd() {
        getHandler().post(mNotifyEnterAnimEndRunnable)
        mSupportA?.getSupportDelegate()?.mFragmentClickable = true
    }

    private fun getHandler(): Handler {
        if (mHandler == null) {
            mHandler = Handler(Looper.getMainLooper())
        }
        return mHandler!!
    }

    fun getVisibleDelegate(): VisibleDelegate {
        if (mVisibleDelegate == null) {
            mVisibleDelegate = VisibleDelegate(mSupportF)
        }
        return mVisibleDelegate!!
    }

    fun getActivity(): FragmentActivity {
        return mActivity
    }

    private fun getEnterAnim(): Animation? {
        if (mCustomEnterAnim == Integer.MIN_VALUE) {
            if (mAnimHelper?.mEnterAnim != null) {
                return mAnimHelper!!.mEnterAnim
            }
        } else {
            try {
                return AnimationUtils.loadAnimation(mActivity, mCustomEnterAnim)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun getEnterAnimDuration(): Long {
        val enter = getEnterAnim()
        return enter?.duration ?: NOT_FOUND_ANIM_TIME
    }

    fun getExitAnimDuration(): Long {
        if (mCustomExitAnim == Integer.MIN_VALUE) {
            if (mAnimHelper?.mExitAnim != null) {
                return mAnimHelper!!.mExitAnim!!.duration
            }
        } else {
            try {
                return AnimationUtils.loadAnimation(mActivity, mCustomExitAnim).duration
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return NOT_FOUND_ANIM_TIME
    }

    private fun getPopExitAnimDuration(): Long {
        if (mCustomPopExitAnim == Integer.MIN_VALUE) {
            if (mAnimHelper?.mPopExitAnim != null) {
                return mAnimHelper!!.mPopExitAnim!!.duration
            }
        } else {
            try {
                return AnimationUtils.loadAnimation(mActivity, mCustomPopExitAnim).duration
            } catch (ignore: Exception) {
            }
        }
        return NOT_FOUND_ANIM_TIME
    }

    internal fun getExitAnim(): Animation? {
        if (mCustomExitAnim == Integer.MIN_VALUE) {
            if (mAnimHelper?.mExitAnim != null) {
                return mAnimHelper!!.mExitAnim
            }
        } else {
            try {
                return AnimationUtils.loadAnimation(mActivity, mCustomExitAnim)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    internal interface EnterAnimListener {
        fun onEnterAnimStart()
    }

    companion object {
        internal const val STATUS_UN_ROOT = 0
        internal const val STATUS_ROOT_ANIM_DISABLE = 1
        internal const val STATUS_ROOT_ANIM_ENABLE = 2
        private const val NOT_FOUND_ANIM_TIME = 300L
    }
}