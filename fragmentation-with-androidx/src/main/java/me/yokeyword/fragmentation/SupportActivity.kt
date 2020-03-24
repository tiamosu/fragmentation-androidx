package me.yokeyword.fragmentation

import android.os.Bundle
import android.view.MotionEvent

import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import me.yokeyword.fragmentation.anim.FragmentAnimator

/**
 * Base class for activities that use the support-based
 * [ISupportActivity] and
 * [AppCompatActivity] APIs.
 *
 * Created by YoKey on 17/6/20.
 */
@Suppress("unused")
open class SupportActivity : AppCompatActivity(), ISupportActivity {
    private val delegate = SupportActivityDelegate(apply { })

    override fun getSupportDelegate(): SupportActivityDelegate {
        return delegate
    }

    open fun getContext(): SupportActivity {
        return this
    }

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义Tag，添加SharedElement动画，操作非回退栈Fragment
     */
    override fun extraTransaction(): ExtraTransaction {
        return delegate.extraTransaction()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate.onCreate()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delegate.onPostCreate()
    }

    override fun onDestroy() {
        delegate.onDestroy()
        super.onDestroy()
    }

    /**
     * Note： return mDelegate.dispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return delegate.dispatchTouchEvent() || super.dispatchTouchEvent(ev)
    }

    /**
     * 不建议复写该方法,请使用 [onBackPressedSupport] 代替
     */
    override fun onBackPressed() {
        delegate.onBackPressed()
    }

    /**
     * 该方法回调时机为,Activity回退栈内Fragment的数量 小于等于1 时,默认finish Activity
     * 请尽量复写该方法,避免复写onBackPress(),以保证SupportFragment内的onBackPressedSupport()回退事件正常执行
     */
    override fun onBackPressedSupport() {
        delegate.onBackPressedSupport()
    }

    /**
     * 获取设置的全局动画 copy
     *
     * @return FragmentAnimator
     */
    override fun getFragmentAnimator(): FragmentAnimator? {
        return delegate.getFragmentAnimator()
    }

    /**
     * Set all fragments animation.
     * 设置Fragment内的全局动画
     */
    override fun setFragmentAnimator(fragmentAnimator: FragmentAnimator?) {
        delegate.setFragmentAnimator(fragmentAnimator)
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
    override fun onCreateFragmentAnimator(): FragmentAnimator? {
        return delegate.onCreateFragmentAnimator()
    }

    override fun post(runnable: Runnable) {
        delegate.post(runnable)
    }

    /****************************************以下为可选方法(Optional methods)******************************************************/

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     *
     * @param containerId 容器id
     * @param toFragment  目标Fragment
     */
    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment?) {
        delegate.loadRootFragment(containerId, toFragment)
    }

    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment?,
                         addToBackStack: Boolean, allowAnimation: Boolean) {
        delegate.loadRootFragment(containerId, toFragment, addToBackStack, allowAnimation)
    }

    /**
     * 加载多个同级根Fragment,类似Wechat, QQ主页的场景
     */
    fun loadMultipleRootFragment(containerId: Int, showPosition: Int,
                                 toFragments: Array<out ISupportFragment?>) {
        delegate.loadMultipleRootFragment(containerId, showPosition, toFragments)
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
        delegate.showHideFragment(showFragment)
    }

    /**
     * show一个Fragment,hide一个Fragment ; 主要用于类似微信主页那种 切换tab的情况
     */
    fun showHideFragment(showFragment: ISupportFragment?, hideFragment: ISupportFragment?) {
        delegate.showHideFragment(showFragment, hideFragment)
    }

    /**
     * It is recommended to use [SupportFragment.start].
     */
    fun start(toFragment: ISupportFragment?) {
        delegate.start(toFragment)
    }

    /**
     * It is recommended to use [SupportFragment.start].
     *
     * @param launchMode Similar to Activity's LaunchMode.
     */
    fun start(toFragment: ISupportFragment?, @ISupportFragment.LaunchMode launchMode: Int) {
        delegate.start(toFragment, launchMode)
    }

    /**
     * It is recommended to use [SupportFragment.startForResult].
     * Launch an fragment for which you would like a result when it poped.
     */
    fun startForResult(toFragment: ISupportFragment?, requestCode: Int) {
        delegate.startForResult(toFragment, requestCode)
    }

    /**
     * It is recommended to use [SupportFragment.startWithPop].
     * Start the target Fragment and pop itself
     */
    fun startWithPop(toFragment: ISupportFragment?) {
        delegate.startWithPop(toFragment)
    }

    /**
     * It is recommended to use [SupportFragment.startWithPopTo].
     *
     * @see .popTo
     * @see .start
     */
    fun startWithPopTo(toFragment: ISupportFragment?, targetFragmentClass: Class<*>?,
                       includeTargetFragment: Boolean) {
        delegate.startWithPopTo(toFragment, targetFragmentClass, includeTargetFragment)
    }

    /**
     * It is recommended to use [SupportFragment.replaceFragment].
     */
    fun replaceFragment(toFragment: ISupportFragment?, addToBackStack: Boolean) {
        delegate.replaceFragment(toFragment, addToBackStack)
    }

    /**
     * Pop the fragment.
     */
    fun pop() {
        delegate.pop()
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
        delegate.popTo(targetFragmentClass, includeTargetFragment)
    }

    /**
     * If you want to begin another FragmentTransaction immediately after popTo(), use this method.
     * 如果你想在出栈后, 立刻进行FragmentTransaction操作，请使用该方法
     */
    fun popTo(targetFragmentClass: Class<*>?, includeTargetFragment: Boolean,
              afterPopTransactionRunnable: Runnable?) {
        delegate.popTo(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable)
    }

    fun popTo(targetFragmentClass: Class<*>?, includeTargetFragment: Boolean,
              afterPopTransactionRunnable: Runnable?, popAnim: Int) {
        delegate.popTo(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable, popAnim)
    }

    /**
     * 当Fragment根布局 没有 设定background属性时,
     * Fragmentation默认使用Theme的android:windowbackground作为Fragment的背景,
     * 可以通过该方法改变其内所有Fragment的默认背景。
     */
    fun setDefaultFragmentBackground(@DrawableRes backgroundRes: Int) {
        delegate.setDefaultFragmentBackground(backgroundRes)
    }

    /**
     * 得到位于栈顶Fragment
     */
    fun getTopFragment(): ISupportFragment? {
        return SupportHelper.getTopFragment(supportFragmentManager)
    }

    /**
     * 获取栈内的fragment对象
     */
    fun <T : ISupportFragment> findFragment(fragmentClass: Class<T>?): T? {
        return SupportHelper.findFragment(supportFragmentManager, fragmentClass)
    }
}
