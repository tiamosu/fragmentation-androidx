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
class SupportFragmentDelegate(private val supportF: ISupportFragment) {
    private lateinit var activity: FragmentActivity
    internal var fragmentAnimator: FragmentAnimator? = null
    internal var animHelper: AnimatorHelper? = null
    internal var lockAnim = false
    internal var containerId = 0
    internal var transactionRecord: TransactionRecord? = null
    internal var newBundle: Bundle? = null
    internal var animByActivity = true
    internal var enterAnimListener: EnterAnimListener? = null
    private var rootStatus = STATUS_UN_ROOT
    private var isSharedElement = false
    private var customEnterAnim = Integer.MIN_VALUE
    private var customExitAnim = Integer.MIN_VALUE
    private var customPopExitAnim = Integer.MIN_VALUE
    private var handler: Handler? = null
    private var firstCreateView = true
    private var replaceMode = false
    private var isHidden = true
    private var transactionDelegate: TransactionDelegate? = null

    // SupportVisible
    private var visibleDelegate: VisibleDelegate? = null
    private var saveInstanceState: Bundle? = null
    private var fragment: Fragment? = null
    private var supportA: ISupportActivity? = null
    private var rootViewClickable = false

    private val notifyEnterAnimEndRunnable by lazy {
        Runnable {
            if (fragment == null) {
                return@Runnable
            }
            supportF.onEnterAnimationEnd(saveInstanceState)

            if (rootViewClickable) {
                return@Runnable
            }
            val view = fragment?.view ?: return@Runnable
            val preFragment = SupportHelper.getPreFragment(fragment) ?: return@Runnable

            val prePopExitDuration = preFragment.getSupportDelegate().getPopExitAnimDuration()
            val enterDuration = getEnterAnimDuration()

            getHandler().postDelayed({ view.isClickable = false }, prePopExitDuration - enterDuration)
        }
    }

    init {
        if (supportF !is Fragment) {
            throw RuntimeException("Must extends Fragment")
        }
        this.fragment = supportF
    }

    fun onAttach() {
        val activity = fragment?.activity
        if (activity !is ISupportActivity) {
            throw RuntimeException(activity?.javaClass?.simpleName
                    ?: "fragment" + " must impl ISupportActivity!")
        }
        this.supportA = activity
        this.activity = activity
        transactionDelegate = supportA?.getSupportDelegate()?.getTransactionDelegate()
    }

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义Tag，添加SharedElement动画，操作非回退栈Fragment
     */
    fun extraTransaction(): ExtraTransaction {
        if (transactionDelegate == null) {
            throw RuntimeException(fragment?.javaClass?.simpleName + " not attach!")
        }
        return ExtraTransaction.ExtraTransactionImpl(activity, supportF, transactionDelegate, false)
    }

    fun onCreate(savedInstanceState: Bundle?) {
        getVisibleDelegate().onCreate(savedInstanceState)

        fragment?.arguments?.apply {
            rootStatus = getInt(TransactionDelegate.FRAGMENTATION_ARG_ROOT_STATUS, STATUS_UN_ROOT)
            isSharedElement = getBoolean(TransactionDelegate.FRAGMENTATION_ARG_IS_SHARED_ELEMENT, false)
            containerId = getInt(TransactionDelegate.FRAGMENTATION_ARG_CONTAINER)
            replaceMode = getBoolean(TransactionDelegate.FRAGMENTATION_ARG_REPLACE, false)
            customEnterAnim = getInt(TransactionDelegate.FRAGMENTATION_ARG_CUSTOM_ENTER_ANIM, Integer.MIN_VALUE)
            customExitAnim = getInt(TransactionDelegate.FRAGMENTATION_ARG_CUSTOM_EXIT_ANIM, Integer.MIN_VALUE)
            customPopExitAnim = getInt(TransactionDelegate.FRAGMENTATION_ARG_CUSTOM_POP_EXIT_ANIM, Integer.MIN_VALUE)
        }

        if (savedInstanceState == null) {
            getFragmentAnimator()
        } else {
            savedInstanceState.classLoader = javaClass.classLoader
            saveInstanceState = savedInstanceState
            fragmentAnimator = savedInstanceState.getParcelable(TransactionDelegate.FRAGMENTATION_STATE_SAVE_ANIMATOR)
            isHidden = savedInstanceState.getBoolean(TransactionDelegate.FRAGMENTATION_STATE_SAVE_IS_HIDDEN)
            containerId = savedInstanceState.getInt(TransactionDelegate.FRAGMENTATION_ARG_CONTAINER)
        }

        animHelper = AnimatorHelper(activity.applicationContext, fragmentAnimator)

        val enter = getEnterAnim() ?: return

        getEnterAnim()?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                supportA?.getSupportDelegate()?.fragmentClickable = false  // 开启防抖动

                getHandler().postDelayed({ supportA?.getSupportDelegate()?.fragmentClickable = true }, enter.duration)
            }

            override fun onAnimationEnd(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (supportA?.getSupportDelegate()?.popMultipleNoAnim == true || lockAnim) {
            return if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE && enter) {
                animHelper?.getNoneAnimFixed()
            } else animHelper?.getNoneAnim()
        }
        when (transit) {
            FragmentTransaction.TRANSIT_FRAGMENT_OPEN -> return if (enter) {
                val enterAnim: Animation?
                if (rootStatus == STATUS_ROOT_ANIM_DISABLE) {
                    enterAnim = animHelper?.getNoneAnim()
                } else {
                    enterAnim = animHelper?.enterAnim
                    fixAnimationListener(enterAnim)
                }
                enterAnim
            } else {
                animHelper?.popExitAnim
            }
            FragmentTransaction.TRANSIT_FRAGMENT_CLOSE -> return if (enter) animHelper?.popEnterAnim else animHelper?.exitAnim
            else -> {
                if (isSharedElement && enter) {
                    compatSharedElements()
                }

                return if (!enter) {
                    animHelper?.compatChildFragmentExitAnim(fragment)
                } else null
            }
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        getVisibleDelegate().onSaveInstanceState(outState)
        outState.putParcelable(TransactionDelegate.FRAGMENTATION_STATE_SAVE_ANIMATOR, fragmentAnimator)
        outState.putBoolean(TransactionDelegate.FRAGMENTATION_STATE_SAVE_IS_HIDDEN, fragment!!.isHidden)
        outState.putInt(TransactionDelegate.FRAGMENTATION_ARG_CONTAINER, containerId)
    }

    fun onActivityCreated(savedInstanceState: Bundle?) {
        getVisibleDelegate().onActivityCreated()

        val view = fragment?.view
        if (view != null) {
            rootViewClickable = view.isClickable
            view.isClickable = true
            setBackground(view)
        }

        if (savedInstanceState != null
                || rootStatus == STATUS_ROOT_ANIM_DISABLE
                || fragment?.tag != null && fragment!!.tag!!.startsWith("android:switcher:")
                || replaceMode && !firstCreateView) {
            notifyEnterAnimEnd()
        } else if (customEnterAnim != Integer.MIN_VALUE) {
            fixAnimationListener(if (customEnterAnim == 0)
                animHelper?.getNoneAnim()
            else
                AnimationUtils.loadAnimation(activity, customEnterAnim))
        }

        if (firstCreateView) {
            firstCreateView = false
        }
    }

    fun onResume() {
        getVisibleDelegate().onResume()
    }

    fun onPause() {
        getVisibleDelegate().onPause()
    }

    fun onDestroyView() {
        supportA?.getSupportDelegate()?.fragmentClickable = true
        getVisibleDelegate().onDestroyView()
        getHandler().removeCallbacks(notifyEnterAnimEndRunnable)
    }

    fun onDestroy() {
        transactionDelegate?.handleResultRecord(fragment)
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
        transactionDelegate?.post(runnable)
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
        return supportA!!.getFragmentAnimator()
    }

    /**
     * 获取设置的全局动画
     *
     * @return FragmentAnimator
     */
    fun getFragmentAnimator(): FragmentAnimator? {
        if (fragmentAnimator == null) {
            fragmentAnimator = supportF.onCreateFragmentAnimator()
            if (fragmentAnimator == null) {
                fragmentAnimator = supportA?.getFragmentAnimator()
            }
        }
        return fragmentAnimator
    }

    /**
     * Set the fragment animation.
     */
    fun setFragmentAnimator(fragmentAnimator: FragmentAnimator?) {
        this.fragmentAnimator = fragmentAnimator
        animHelper?.notifyChanged(fragmentAnimator)
        animByActivity = false
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
        val args = fragment?.arguments
        if (args == null || !args.containsKey(TransactionDelegate.FRAGMENTATION_ARG_RESULT_RECORD)) {
            return
        }

        val resultRecord = args.getParcelable<ResultRecord>(TransactionDelegate.FRAGMENTATION_ARG_RESULT_RECORD)
        if (resultRecord != null) {
            resultRecord.resultCode = resultCode
            resultRecord.resultBundle = bundle
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
        this.newBundle = newBundle
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
        val activity = fragment?.activity ?: return
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
        transactionDelegate?.loadRootTransaction(getChildFragmentManager(), containerId,
                toFragment, addToBackStack, allowAnim)
    }

    /**
     * 加载多个同级根Fragment,类似Wechat, QQ主页的场景
     */
    fun loadMultipleRootFragment(containerId: Int, showPosition: Int,
                                 toFragments: Array<out ISupportFragment?>) {
        transactionDelegate?.loadMultipleRootTransaction(getChildFragmentManager(),
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
        transactionDelegate?.showHideFragment(getChildFragmentManager(), showFragment, hideFragment)
    }

    fun start(toFragment: ISupportFragment?) {
        start(toFragment, ISupportFragment.STANDARD)
    }

    /**
     * @param launchMode Similar to Activity's LaunchMode.
     */
    fun start(toFragment: ISupportFragment?, @ISupportFragment.LaunchMode launchMode: Int) {
        transactionDelegate?.dispatchStartTransaction(fragment!!.fragmentManager, supportF,
                toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD)
    }

    /**
     * Launch an fragment for which you would like a result when it poped.
     */
    fun startForResult(toFragment: ISupportFragment?, requestCode: Int) {
        transactionDelegate?.dispatchStartTransaction(fragment!!.fragmentManager, supportF,
                toFragment, requestCode, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT)
    }

    /**
     * Start the target Fragment and pop itself
     */
    fun startWithPop(toFragment: ISupportFragment?) {
        transactionDelegate?.startWithPop(fragment!!.fragmentManager, supportF, toFragment)
    }

    fun startWithPopTo(toFragment: ISupportFragment?, targetFragmentClass: Class<*>?, includeTargetFragment: Boolean) {
        transactionDelegate?.startWithPopTo(fragment!!.fragmentManager, supportF, toFragment,
                targetFragmentClass?.name, includeTargetFragment)
    }

    fun replaceFragment(toFragment: ISupportFragment?, addToBackStack: Boolean) {
        transactionDelegate?.dispatchStartTransaction(fragment!!.fragmentManager, supportF,
                toFragment, 0, ISupportFragment.STANDARD, if (addToBackStack)
            TransactionDelegate.TYPE_REPLACE else TransactionDelegate.TYPE_REPLACE_DONT_BACK)
    }

    fun startChild(toFragment: ISupportFragment?) {
        startChild(toFragment, ISupportFragment.STANDARD)
    }

    fun startChild(toFragment: ISupportFragment?, @ISupportFragment.LaunchMode launchMode: Int) {
        transactionDelegate?.dispatchStartTransaction(getChildFragmentManager(), getTopFragment(),
                toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD)
    }

    fun startChildForResult(toFragment: ISupportFragment?, requestCode: Int) {
        transactionDelegate?.dispatchStartTransaction(getChildFragmentManager(), getTopFragment(),
                toFragment, requestCode, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT)
    }

    fun startChildWithPop(toFragment: ISupportFragment?) {
        transactionDelegate?.startWithPop(getChildFragmentManager(), getTopFragment(), toFragment)
    }

    fun replaceChildFragment(toFragment: ISupportFragment?, addToBackStack: Boolean) {
        transactionDelegate?.dispatchStartTransaction(getChildFragmentManager(), getTopFragment(),
                toFragment, 0, ISupportFragment.STANDARD, if (addToBackStack)
            TransactionDelegate.TYPE_REPLACE else TransactionDelegate.TYPE_REPLACE_DONT_BACK)
    }

    fun pop() {
        transactionDelegate?.pop(fragment!!.fragmentManager)
    }

    /**
     * Pop the child fragment.
     */
    fun popChild() {
        transactionDelegate?.pop(getChildFragmentManager())
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
        transactionDelegate?.popTo(targetFragmentClass?.name, includeTargetFragment,
                afterPopTransactionRunnable, fragment?.fragmentManager, popAnim)
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
        transactionDelegate?.popTo(targetFragmentClass?.name, includeTargetFragment,
                afterPopTransactionRunnable, getChildFragmentManager(), popAnim)
    }

    fun popQuiet() {
        transactionDelegate?.popQuiet(fragment?.fragmentManager, fragment)
    }

    private fun getChildFragmentManager(): FragmentManager? {
        return fragment?.childFragmentManager
    }

    private fun getTopFragment(): ISupportFragment? {
        return SupportHelper.getTopFragment(getChildFragmentManager())
    }

    private fun fixAnimationListener(enterAnim: Animation?) {
        // AnimationListener is not reliable.
        getHandler().postDelayed(notifyEnterAnimEndRunnable, enterAnim?.duration ?: 0)
        supportA?.getSupportDelegate()?.fragmentClickable = true

        if (enterAnimListener != null) {
            getHandler().post {
                enterAnimListener!!.onEnterAnimStart()
                enterAnimListener = null
            }
        }
    }

    private fun compatSharedElements() {
        notifyEnterAnimEnd()
    }

    fun setBackground(view: View) {
        if (fragment?.tag != null && fragment!!.tag!!.startsWith("android:switcher:") ||
                rootStatus != STATUS_UN_ROOT || view.background != null) {
            return
        }

        val defaultBg = supportA?.getSupportDelegate()?.getDefaultFragmentBackground() ?: 0
        if (defaultBg == 0) {
            val background = getWindowBackground()
            view.setBackgroundResource(background)
        } else {
            view.setBackgroundResource(defaultBg)
        }
    }

    private fun getWindowBackground(): Int {
        val a = activity.theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
        val background = a.getResourceId(0, 0)
        a.recycle()
        return background
    }

    private fun notifyEnterAnimEnd() {
        getHandler().post(notifyEnterAnimEndRunnable)
        supportA?.getSupportDelegate()?.fragmentClickable = true
    }

    private fun getHandler(): Handler {
        if (handler == null) {
            handler = Handler(Looper.getMainLooper())
        }
        return handler!!
    }

    fun getVisibleDelegate(): VisibleDelegate {
        if (visibleDelegate == null) {
            visibleDelegate = VisibleDelegate(supportF)
        }
        return visibleDelegate!!
    }

    fun getActivity(): FragmentActivity {
        return activity
    }

    private fun getEnterAnim(): Animation? {
        if (customEnterAnim == Integer.MIN_VALUE) {
            if (animHelper?.enterAnim != null) {
                return animHelper!!.enterAnim
            }
        } else {
            try {
                return AnimationUtils.loadAnimation(activity, customEnterAnim)
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
        if (customExitAnim == Integer.MIN_VALUE) {
            if (animHelper?.exitAnim != null) {
                return animHelper!!.exitAnim!!.duration
            }
        } else {
            try {
                return AnimationUtils.loadAnimation(activity, customExitAnim).duration
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return NOT_FOUND_ANIM_TIME
    }

    private fun getPopExitAnimDuration(): Long {
        if (customPopExitAnim == Integer.MIN_VALUE) {
            if (animHelper?.popExitAnim != null) {
                return animHelper!!.popExitAnim!!.duration
            }
        } else {
            try {
                return AnimationUtils.loadAnimation(activity, customPopExitAnim).duration
            } catch (ignore: Exception) {
            }
        }
        return NOT_FOUND_ANIM_TIME
    }

    internal fun getExitAnim(): Animation? {
        if (customExitAnim == Integer.MIN_VALUE) {
            if (animHelper?.exitAnim != null) {
                return animHelper!!.exitAnim
            }
        } else {
            try {
                return AnimationUtils.loadAnimation(activity, customExitAnim)
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