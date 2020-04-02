package me.yokeyword.fragmentation

import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentationMagician
import me.yokeyword.fragmentation.ISupportFragment.LaunchMode
import me.yokeyword.fragmentation.anim.DefaultVerticalAnimator
import me.yokeyword.fragmentation.anim.FragmentAnimator
import me.yokeyword.fragmentation.debug.DebugStackDelegate
import me.yokeyword.fragmentation.queue.Action

@Suppress("unused")
class SupportActivityDelegate(private val supportA: ISupportActivity) {
    internal var popMultipleNoAnim = false
    internal var fragmentClickable = true
    internal val transactionDelegate: TransactionDelegate by lazy { TransactionDelegate(supportA) }

    private val activity: FragmentActivity
    private var fragmentAnimator: FragmentAnimator? = null
    private var debugStackDelegate: DebugStackDelegate? = null
    private var defaultFragmentBackground = 0

    init {
        if (supportA !is FragmentActivity) {
            throw RuntimeException("must extends FragmentActivity/AppCompatActivity")
        }
        this.activity = supportA
        this.debugStackDelegate = DebugStackDelegate(activity)
    }

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义 tag，添加 SharedElement 动画，操作非回退栈 Fragment
     */
    fun extraTransaction(): ExtraTransaction {
        return ExtraTransaction.ExtraTransactionImpl(activity,
                getTopFragment(), transactionDelegate, true)
    }

    fun onCreate() {
        fragmentAnimator = supportA.onCreateFragmentAnimator()
        debugStackDelegate?.onCreate(Fragmentation.getDefault().getMode())
    }

    fun onPostCreate() {
        debugStackDelegate?.onPostCreate(Fragmentation.getDefault().getMode())
    }

    /**
     * 获取设置的全局动画 copy
     */
    fun getFragmentAnimator(): FragmentAnimator? {
        return fragmentAnimator?.copy()
    }

    /**
     * Set all fragments animation.
     * 设置 Fragment 内的全局动画
     */
    fun setFragmentAnimator(fragmentAnimator: FragmentAnimator?) {
        this.fragmentAnimator = fragmentAnimator

        val fragmentList = FragmentationMagician
                .getAddedFragments(getSupportFragmentManager()) ?: return
        for (fragment in fragmentList) {
            if (fragment is ISupportFragment) {
                val delegate = fragment.getSupportDelegate()
                if (delegate.animByActivity) {
                    delegate.fragmentAnimator = fragmentAnimator?.copy()
                    delegate.animHelper?.notifyChanged(delegate.fragmentAnimator)
                }
            }
        }
    }

    /**
     * Set all fragments animation.
     * 构建 Fragment 转场动画
     *
     * 如果是在 Activity 内实现，则构建的是 Activity 内所有 Fragment 的转场动画,
     * 如果是在 Fragment 内实现，则构建的是该 Fragment 的转场动画，此时优先级 > Activity 的 onCreateFragmentAnimator()
     *
     * @return FragmentAnimator对象
     */
    fun onCreateFragmentAnimator(): FragmentAnimator? {
        return DefaultVerticalAnimator()
    }

    fun getDefaultFragmentBackground(): Int {
        return defaultFragmentBackground
    }

    /**
     * 当 Fragment 根布局 没有设定 background 属性时,
     * Fragmentation 默认使用 Theme 的 android:windowbackground 作为 Fragment 的背景,
     * 可以通过该方法改变 Fragment 背景。
     */
    fun setDefaultFragmentBackground(@DrawableRes backgroundRes: Int) {
        defaultFragmentBackground = backgroundRes
    }

    /**
     * 显示栈视图 dialog，调试时使用
     */
    fun showFragmentStackHierarchyView() {
        debugStackDelegate?.showFragmentStackHierarchyView()
    }

    /**
     * 显示栈视图日志，调试时使用
     */
    fun logFragmentStackHierarchy(tag: String) {
        debugStackDelegate?.logFragmentRecords(tag)
    }

    /**
     * Causes the Runnable r to be added to the action queue.
     *
     * The runnable will be run after all the previous action has been run.
     *
     * 前面的事务全部执行后 执行该Action
     */
    fun post(runnable: Runnable) {
        transactionDelegate.post(runnable)
    }

    /**
     * 不建议复写该方法,请使用 [onBackPressedSupport] 代替
     */
    fun onBackPressed() {
        transactionDelegate.actionQueue.enqueue(object : Action(ACTION_BACK) {
            override fun run() {
                if (!fragmentClickable) {
                    fragmentClickable = true
                }

                // 获取 activeFragment 即从栈顶开始 状态为 show 的那个 Fragment
                val activeFragment = SupportHelper.getAddedFragment(getSupportFragmentManager())
                if (transactionDelegate.dispatchBackPressedEvent(activeFragment)) return
                supportA.onBackPressedSupport()
            }
        })
    }

    /**
     * 该方法回调时机为，Activity 回退栈内 Fragment 的数量小于等于1时，默认 finish Activity
     * 请尽量复写该方法，避免复写 onBackPress()，以保证 SupportFragment 内的 onBackPressedSupport() 回退事件正常执行
     */
    fun onBackPressedSupport() {
        if (getSupportFragmentManager().backStackEntryCount > 1) {
            pop()
        } else {
            ActivityCompat.finishAfterTransition(activity)
        }
    }

    fun onDestroy() {
        debugStackDelegate?.onDestroy()
    }

    fun dispatchTouchEvent(): Boolean {
        // 防抖动(防止点击速度过快)
        return !fragmentClickable
    }

    /**********************************************************************************************/

    /**
     * 加载根 Fragment, 即 Activity 内的第一个 Fragment 或 Fragment 内的第一个子 Fragment
     */
    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment?) {
        loadRootFragment(containerId, toFragment, true, false)
    }

    fun loadRootFragment(containerId: Int,
                         toFragment: ISupportFragment?,
                         addToBackStack: Boolean,
                         allowAnimation: Boolean) {
        transactionDelegate.loadRootTransaction(getSupportFragmentManager(),
                containerId, toFragment, addToBackStack, allowAnimation)
    }

    /**
     * 加载多个同级根 Fragment，类似 Wechat, QQ 主页的场景
     */
    fun loadMultipleRootFragment(containerId: Int,
                                 showPosition: Int,
                                 toFragments: Array<out ISupportFragment?>?) {
        transactionDelegate.loadMultipleRootTransaction(getSupportFragmentManager(),
                containerId, showPosition, toFragments)
    }

    /**
     * show 一个 Fragment，hide 其他同栈所有 Fragment
     * 使用该方法时，要确保同级栈内无多余的 Fragment，(只有通过 loadMultipleRootFragment() 载入的 Fragment)
     *
     * 建议使用更明确的 [showHideFragment]
     *
     * @param showFragment 需要show的Fragment
     */
    fun showHideFragment(showFragment: ISupportFragment?) {
        showHideFragment(showFragment, null)
    }

    /**
     * show 一个 Fragment，hide 一个 Fragment；主要用于类似微信主页那种 切换 tab 的情况
     *
     * @param showFragment 需要 show 的 Fragment
     * @param hideFragment 需要 hide 的 Fragment
     */
    fun showHideFragment(showFragment: ISupportFragment?, hideFragment: ISupportFragment?) {
        transactionDelegate.showHideFragment(getSupportFragmentManager(), showFragment, hideFragment)
    }

    fun start(toFragment: ISupportFragment?) {
        start(toFragment, ISupportFragment.STANDARD)
    }

    /**
     * @param launchMode Similar to Activity's LaunchMode.
     */
    fun start(toFragment: ISupportFragment?, @LaunchMode launchMode: Int) {
        transactionDelegate.dispatchStartTransaction(getSupportFragmentManager(),
                getTopFragment(), toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD)
    }

    /**
     * Launch an fragment for which you would like a result when it poped.
     */
    fun startForResult(toFragment: ISupportFragment?, requestCode: Int) {
        transactionDelegate.dispatchStartTransaction(getSupportFragmentManager(), getTopFragment(),
                toFragment, requestCode, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT)
    }

    /**
     * Start the target Fragment and pop itself
     */
    fun startWithPop(toFragment: ISupportFragment?) {
        transactionDelegate.startWithPop(getSupportFragmentManager(), getTopFragment(), toFragment)
    }

    fun startWithPopTo(toFragment: ISupportFragment?,
                       targetFragmentClass: Class<*>?,
                       includeTargetFragment: Boolean) {
        transactionDelegate.startWithPopTo(getSupportFragmentManager(), getTopFragment(),
                toFragment, targetFragmentClass?.name, includeTargetFragment)
    }

    fun replaceFragment(toFragment: ISupportFragment?, addToBackStack: Boolean) {
        transactionDelegate.dispatchStartTransaction(getSupportFragmentManager(), getTopFragment(),
                toFragment, 0, ISupportFragment.STANDARD, if (addToBackStack)
            TransactionDelegate.TYPE_REPLACE else TransactionDelegate.TYPE_REPLACE_DONT_BACK)
    }

    /**
     * Pop the child fragment.
     */
    fun pop() {
        transactionDelegate.pop(getSupportFragmentManager())
    }

    /**
     * Pop the last fragment transition from the manager's fragment
     * back stack.
     *
     * 出栈到目标 fragment
     *
     * @param targetFragmentClass   目标 fragment
     * @param includeTargetFragment 是否包含该 fragment
     */
    fun popTo(targetFragmentClass: Class<*>?, includeTargetFragment: Boolean) {
        popTo(targetFragmentClass, includeTargetFragment, null)
    }

    /**
     * If you want to begin another FragmentTransaction immediately after popTo(), use this method.
     * 如果你想在出栈后, 立刻进行FragmentTransaction操作，请使用该方法
     */
    fun popTo(targetFragmentClass: Class<*>?,
              includeTargetFragment: Boolean,
              afterPopTransactionRunnable: Runnable?) {
        popTo(targetFragmentClass, includeTargetFragment,
                afterPopTransactionRunnable, TransactionDelegate.DEFAULT_POPTO_ANIM)
    }

    fun popTo(targetFragmentClass: Class<*>?,
              includeTargetFragment: Boolean,
              afterPopTransactionRunnable: Runnable?,
              popAnim: Int) {
        transactionDelegate.popTo(targetFragmentClass?.name, includeTargetFragment,
                afterPopTransactionRunnable, getSupportFragmentManager(), popAnim)
    }

    private fun getSupportFragmentManager(): FragmentManager {
        return activity.supportFragmentManager
    }

    private fun getTopFragment(): ISupportFragment? {
        return SupportHelper.getTopFragment(getSupportFragmentManager())
    }
}
