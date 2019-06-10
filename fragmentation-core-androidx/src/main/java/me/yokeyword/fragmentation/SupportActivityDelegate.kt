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
class SupportActivityDelegate(private val mSupport: ISupportActivity) {
    internal var mPopMultipleNoAnim = false
    internal var mFragmentClickable = true
    private val mActivity: FragmentActivity
    private var mTransactionDelegate: TransactionDelegate? = null
    private var mFragmentAnimator: FragmentAnimator? = null
    private var mDefaultFragmentBackground = 0
    private var mDebugStackDelegate: DebugStackDelegate? = null

    init {
        if (mSupport !is FragmentActivity) {
            throw RuntimeException("Must extends FragmentActivity/AppCompatActivity")
        }
        this.mActivity = mSupport
        this.mDebugStackDelegate = DebugStackDelegate(mActivity)
    }

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义Tag，添加SharedElement动画，操作非回退栈Fragment
     */
    fun extraTransaction(): ExtraTransaction {
        return ExtraTransaction.ExtraTransactionImpl(mSupport as FragmentActivity, getTopFragment()!!,
                getTransactionDelegate(), true)
    }

    fun onCreate() {
        mTransactionDelegate = getTransactionDelegate()
        mFragmentAnimator = mSupport.onCreateFragmentAnimator()
        mDebugStackDelegate!!.onCreate(Fragmentation.getDefault().getMode())
    }

    fun getTransactionDelegate(): TransactionDelegate {
        if (mTransactionDelegate == null) {
            mTransactionDelegate = TransactionDelegate(mSupport)
        }
        return mTransactionDelegate!!
    }

    fun onPostCreate() {
        mDebugStackDelegate!!.onPostCreate(Fragmentation.getDefault().getMode())
    }

    /**
     * 获取设置的全局动画 copy
     *
     * @return FragmentAnimator
     */
    fun getFragmentAnimator(): FragmentAnimator? {
        return mFragmentAnimator?.copy()
    }

    /**
     * Set all fragments animation.
     * 设置Fragment内的全局动画
     */
    fun setFragmentAnimator(fragmentAnimator: FragmentAnimator?) {
        this.mFragmentAnimator = fragmentAnimator

        val fragmentList = FragmentationMagician.getActiveFragments(getSupportFragmentManager())
                ?: return
        for (fragment in fragmentList) {
            if (fragment is ISupportFragment) {
                val delegate = fragment.getSupportDelegate()
                if (delegate.mAnimByActivity) {
                    delegate.mFragmentAnimator = fragmentAnimator?.copy()
                    delegate.mAnimHelper?.notifyChanged(delegate.mFragmentAnimator)
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
        return mDefaultFragmentBackground
    }

    /**
     * 当Fragment根布局 没有 设定background属性时,
     * Fragmentation默认使用Theme的android:windowbackground作为Fragment的背景,
     * 可以通过该方法改变Fragment背景。
     */
    fun setDefaultFragmentBackground(@DrawableRes backgroundRes: Int) {
        mDefaultFragmentBackground = backgroundRes
    }

    /**
     * 显示栈视图dialog,调试时使用
     */
    fun showFragmentStackHierarchyView() {
        mDebugStackDelegate!!.showFragmentStackHierarchyView()
    }

    /**
     * 显示栈视图日志,调试时使用
     */
    fun logFragmentStackHierarchy(TAG: String) {
        mDebugStackDelegate!!.logFragmentRecords(TAG)
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
        mTransactionDelegate!!.post(runnable)
    }

    /**
     * 不建议复写该方法,请使用 [.onBackPressedSupport] 代替
     */
    fun onBackPressed() {
        mTransactionDelegate!!.mActionQueue.enqueue(object : Action(ACTION_BACK) {
            override fun run() {
                if (!mFragmentClickable) {
                    mFragmentClickable = true
                }

                // 获取activeFragment:即从栈顶开始 状态为show的那个Fragment
                val activeFragment = SupportHelper.getActiveFragment(getSupportFragmentManager())
                if (mTransactionDelegate!!.dispatchBackPressedEvent(activeFragment)) {
                    return
                }
                mSupport.onBackPressedSupport()
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
            ActivityCompat.finishAfterTransition(mActivity)
        }
    }

    fun onDestroy() {
        mDebugStackDelegate!!.onDestroy()
    }

    fun dispatchTouchEvent(): Boolean {
        // 防抖动(防止点击速度过快)
        return !mFragmentClickable
    }

    /**********************************************************************************************/

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     */
    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment?) {
        loadRootFragment(containerId, toFragment, true, false)
    }

    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment?, addToBackStack: Boolean, allowAnimation: Boolean) {
        mTransactionDelegate!!.loadRootTransaction(getSupportFragmentManager(), containerId, toFragment, addToBackStack, allowAnimation)
    }

    /**
     * 加载多个同级根Fragment,类似Wechat, QQ主页的场景
     */
    fun loadMultipleRootFragment(containerId: Int, showPosition: Int, toFragments: Array<out ISupportFragment?>) {
        mTransactionDelegate!!.loadMultipleRootTransaction(getSupportFragmentManager(), containerId, showPosition, toFragments)
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
        mTransactionDelegate!!.showHideFragment(getSupportFragmentManager(), showFragment, hideFragment)
    }

    fun start(toFragment: ISupportFragment?) {
        start(toFragment, ISupportFragment.STANDARD)
    }

    /**
     * @param launchMode Similar to Activity's LaunchMode.
     */
    fun start(toFragment: ISupportFragment?, @ISupportFragment.LaunchMode launchMode: Int) {
        mTransactionDelegate!!.dispatchStartTransaction(getSupportFragmentManager(),
                getTopFragment(), toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD)
    }

    /**
     * Launch an fragment for which you would like a result when it poped.
     */
    fun startForResult(toFragment: ISupportFragment?, requestCode: Int) {
        mTransactionDelegate!!.dispatchStartTransaction(getSupportFragmentManager(),
                getTopFragment(), toFragment, requestCode, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT)
    }

    /**
     * Start the target Fragment and pop itself
     */
    fun startWithPop(toFragment: ISupportFragment?) {
        mTransactionDelegate!!.startWithPop(getSupportFragmentManager(), getTopFragment(), toFragment)
    }

    fun startWithPopTo(toFragment: ISupportFragment?, targetFragmentClass: Class<*>, includeTargetFragment: Boolean) {
        mTransactionDelegate!!.startWithPopTo(getSupportFragmentManager(), getTopFragment(),
                toFragment, targetFragmentClass.name, includeTargetFragment)
    }

    fun replaceFragment(toFragment: ISupportFragment?, addToBackStack: Boolean) {
        mTransactionDelegate!!.dispatchStartTransaction(getSupportFragmentManager(), getTopFragment(),
                toFragment, 0, ISupportFragment.STANDARD,
                if (addToBackStack) TransactionDelegate.TYPE_REPLACE else TransactionDelegate.TYPE_REPLACE_DONT_BACK)
    }

    /**
     * Pop the child fragment.
     */
    fun pop() {
        mTransactionDelegate!!.pop(getSupportFragmentManager())
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
    fun popTo(targetFragmentClass: Class<*>, includeTargetFragment: Boolean) {
        popTo(targetFragmentClass, includeTargetFragment, null)
    }

    /**
     * If you want to begin another FragmentTransaction immediately after popTo(), use this method.
     * 如果你想在出栈后, 立刻进行FragmentTransaction操作，请使用该方法
     */
    fun popTo(targetFragmentClass: Class<*>, includeTargetFragment: Boolean, afterPopTransactionRunnable: Runnable?) {
        popTo(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable, TransactionDelegate.DEFAULT_POPTO_ANIM)
    }

    fun popTo(targetFragmentClass: Class<*>, includeTargetFragment: Boolean,
              afterPopTransactionRunnable: Runnable?, popAnim: Int) {
        mTransactionDelegate!!.popTo(targetFragmentClass.name, includeTargetFragment,
                afterPopTransactionRunnable, getSupportFragmentManager(), popAnim)
    }

    private fun getSupportFragmentManager(): FragmentManager {
        return mActivity.supportFragmentManager
    }

    private fun getTopFragment(): ISupportFragment? {
        return SupportHelper.getTopFragment(getSupportFragmentManager())
    }
}
