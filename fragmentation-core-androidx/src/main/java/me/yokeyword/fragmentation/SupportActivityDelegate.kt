package me.yokeyword.fragmentation

import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentationMagician
import me.yokeyword.fragmentation.anim.DefaultVerticalAnimator
import me.yokeyword.fragmentation.anim.FragmentAnimator
import me.yokeyword.fragmentation.debug.DebugStackDelegate
import me.yokeyword.fragmentation.queue.Action

@Suppress("unused")
class SupportActivityDelegate(private val supportA: ISupportActivity) {
    internal var popMultipleNoAnim = false
    internal var fragmentClickable = true
    private val activity: FragmentActivity
    private var transactionDelegate: TransactionDelegate? = null
    private var fragmentAnimator: FragmentAnimator? = null
    private var defaultFragmentBackground = 0
    private var debugStackDelegate: DebugStackDelegate? = null

    init {
        if (supportA !is FragmentActivity) {
            throw RuntimeException("must extends FragmentActivity/AppCompatActivity")
        }
        this.activity = supportA
        this.debugStackDelegate = DebugStackDelegate(activity)
    }

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义Tag，添加SharedElement动画，操作非回退栈Fragment
     */
    fun extraTransaction(): ExtraTransaction {
        return ExtraTransaction.ExtraTransactionImpl(activity, getTopFragment()!!,
                getTransactionDelegate(), true)
    }

    fun onCreate() {
        transactionDelegate = getTransactionDelegate()
        fragmentAnimator = supportA.onCreateFragmentAnimator()
        debugStackDelegate?.onCreate(Fragmentation.getDefault().getMode())
    }

    fun getTransactionDelegate(): TransactionDelegate {
        if (transactionDelegate == null) {
            transactionDelegate = TransactionDelegate(supportA)
        }
        return transactionDelegate!!
    }

    fun onPostCreate() {
        debugStackDelegate?.onPostCreate(Fragmentation.getDefault().getMode())
    }

    /**
     * 获取设置的全局动画 copy
     *
     * @return FragmentAnimator
     */
    fun getFragmentAnimator(): FragmentAnimator? {
        return fragmentAnimator?.copy()
    }

    /**
     * Set all fragments animation.
     * 设置Fragment内的全局动画
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
     * 构建Fragment转场动画
     *
     *
     * 如果是在Activity内实现,则构建的是Activity内所有Fragment的转场动画,
     * 如果是在Fragment内实现,则构建的是该Fragment的转场动画,此时优先级 > Activity的onCreateFragmentAnimator()
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
     * 当Fragment根布局 没有 设定background属性时,
     * Fragmentation默认使用Theme的android:windowbackground作为Fragment的背景,
     * 可以通过该方法改变Fragment背景。
     */
    fun setDefaultFragmentBackground(@DrawableRes backgroundRes: Int) {
        defaultFragmentBackground = backgroundRes
    }

    /**
     * 显示栈视图dialog,调试时使用
     */
    fun showFragmentStackHierarchyView() {
        debugStackDelegate?.showFragmentStackHierarchyView()
    }

    /**
     * 显示栈视图日志,调试时使用
     */
    fun logFragmentStackHierarchy(tag: String) {
        debugStackDelegate?.logFragmentRecords(tag)
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
     * 不建议复写该方法,请使用 [.onBackPressedSupport] 代替
     */
    fun onBackPressed() {
        transactionDelegate?.actionQueue?.enqueue(object : Action(ACTION_BACK) {
            override fun run() {
                if (!fragmentClickable) {
                    fragmentClickable = true
                }

                // 获取activeFragment:即从栈顶开始 状态为show的那个Fragment
                val activeFragment = SupportHelper.getAddedFragment(getSupportFragmentManager())
                if (transactionDelegate?.dispatchBackPressedEvent(activeFragment) == true) {
                    return
                }
                supportA.onBackPressedSupport()
            }
        })
    }

    /**
     * 该方法回调时机为,Activity回退栈内Fragment的数量 小于等于1 时,默认finish Activity
     * 请尽量复写该方法,避免复写onBackPress(),以保证SupportFragment内的onBackPressedSupport()回退事件正常执行
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
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     */
    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment?) {
        loadRootFragment(containerId, toFragment, true, false)
    }

    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment?, addToBackStack: Boolean, allowAnimation: Boolean) {
        transactionDelegate?.loadRootTransaction(getSupportFragmentManager(), containerId, toFragment, addToBackStack, allowAnimation)
    }

    /**
     * 加载多个同级根Fragment,类似Wechat, QQ主页的场景
     */
    fun loadMultipleRootFragment(containerId: Int, showPosition: Int, toFragments: Array<out ISupportFragment?>) {
        transactionDelegate?.loadMultipleRootTransaction(getSupportFragmentManager(), containerId, showPosition, toFragments)
    }

    /**
     * show一个Fragment,hide其他同栈所有Fragment
     * 使用该方法时，要确保同级栈内无多余的Fragment,(只有通过loadMultipleRootFragment()载入的Fragment)
     *
     *
     * 建议使用更明确的[.showHideFragment]
     *
     * @param showFragment 需要show的Fragment
     */
    fun showHideFragment(showFragment: ISupportFragment?) {
        showHideFragment(showFragment, null)
    }

    /**
     * show一个Fragment,hide一个Fragment ; 主要用于类似微信主页那种 切换tab的情况
     *
     * @param showFragment 需要show的Fragment
     * @param hideFragment 需要hide的Fragment
     */
    fun showHideFragment(showFragment: ISupportFragment?, hideFragment: ISupportFragment?) {
        transactionDelegate?.showHideFragment(getSupportFragmentManager(), showFragment, hideFragment)
    }

    fun start(toFragment: ISupportFragment?) {
        start(toFragment, ISupportFragment.STANDARD)
    }

    /**
     * @param launchMode Similar to Activity's LaunchMode.
     */
    fun start(toFragment: ISupportFragment?, @ISupportFragment.LaunchMode launchMode: Int) {
        transactionDelegate?.dispatchStartTransaction(getSupportFragmentManager(),
                getTopFragment(), toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD)
    }

    /**
     * Launch an fragment for which you would like a result when it poped.
     */
    fun startForResult(toFragment: ISupportFragment?, requestCode: Int) {
        transactionDelegate?.dispatchStartTransaction(getSupportFragmentManager(), getTopFragment(),
                toFragment, requestCode, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT)
    }

    /**
     * Start the target Fragment and pop itself
     */
    fun startWithPop(toFragment: ISupportFragment?) {
        transactionDelegate?.startWithPop(getSupportFragmentManager(), getTopFragment(), toFragment)
    }

    fun startWithPopTo(toFragment: ISupportFragment?, targetFragmentClass: Class<*>?, includeTargetFragment: Boolean) {
        transactionDelegate?.startWithPopTo(getSupportFragmentManager(), getTopFragment(),
                toFragment, targetFragmentClass?.name, includeTargetFragment)
    }

    fun replaceFragment(toFragment: ISupportFragment?, addToBackStack: Boolean) {
        transactionDelegate?.dispatchStartTransaction(getSupportFragmentManager(), getTopFragment(),
                toFragment, 0, ISupportFragment.STANDARD, if (addToBackStack)
            TransactionDelegate.TYPE_REPLACE else TransactionDelegate.TYPE_REPLACE_DONT_BACK)
    }

    /**
     * Pop the child fragment.
     */
    fun pop() {
        transactionDelegate?.pop(getSupportFragmentManager())
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
        popTo(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable, TransactionDelegate.DEFAULT_POPTO_ANIM)
    }

    fun popTo(targetFragmentClass: Class<*>?, includeTargetFragment: Boolean,
              afterPopTransactionRunnable: Runnable?, popAnim: Int) {
        transactionDelegate!!.popTo(targetFragmentClass?.name, includeTargetFragment,
                afterPopTransactionRunnable, getSupportFragmentManager(), popAnim)
    }

    private fun getSupportFragmentManager(): FragmentManager {
        return activity.supportFragmentManager
    }

    private fun getTopFragment(): ISupportFragment? {
        return SupportHelper.getTopFragment(getSupportFragmentManager())
    }
}
