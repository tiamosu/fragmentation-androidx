package me.yokeyword.fragmentation

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import me.yokeyword.fragmentation.ISupportFragment.LaunchMode
import me.yokeyword.fragmentation.anim.FragmentAnimator

/**
 * Base class for activities that use the support-based
 * [ISupportFragment] and [Fragment] APIs.
 *
 * Created by YoKey on 17/6/22.
 */
@Suppress("unused")
open class SupportFragment : Fragment(), ISupportFragment {
    private val delegate by lazy { SupportFragmentDelegate(apply { }) }
    private lateinit var activity: SupportActivity

    override fun getSupportDelegate(): SupportFragmentDelegate {
        return delegate
    }

    override fun getContext(): FragmentActivity {
        return activity
    }

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义 tag，添加 SharedElement 动画，操作非回退栈 Fragment
     */
    override fun extraTransaction(): ExtraTransaction {
        return delegate.extraTransaction()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        delegate.onAttach()
        activity = delegate.getActivity() as SupportActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate.onCreate(savedInstanceState)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return delegate.onCreateAnimation(transit, enter, nextAnim)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        delegate.onActivityCreated(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        delegate.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        delegate.onResume()
    }

    override fun onPause() {
        super.onPause()
        delegate.onPause()
    }

    override fun onDestroyView() {
        delegate.onDestroyView()
        super.onDestroyView()
    }

    override fun onDestroy() {
        delegate.onDestroy()
        super.onDestroy()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        delegate.onHiddenChanged(hidden)
    }

    @Suppress("DEPRECATION")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        delegate.setUserVisibleHint(isVisibleToUser)
    }

    /**
     * Causes the Runnable r to be added to the action queue.
     * The runnable will be run after all the previous action has been run.
     *
     * 前面的事务全部执行后，执行该 Action
     */
    override fun post(runnable: Runnable) {
        delegate.post(runnable)
    }

    /**
     * Called when the enter-animation end.
     * 入栈动画结束时，回调
     */
    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        delegate.onEnterAnimationEnd(savedInstanceState)
    }

    /**
     * Lazy initial，Called when fragment is first called.
     *
     * 同级下的 懒加载 ＋ ViewPager 下的懒加载  的结合回调方法
     */
    override fun onLazyInitView(savedInstanceState: Bundle?) {
        delegate.onLazyInitView(savedInstanceState)
    }

    /**
     * Called when the fragment is visible.
     * 当 Fragment 对用户可见时回调
     *
     * Is the combination of  [onHiddenChanged() + onResume()/onPause() + setUserVisibleHint()]
     */
    override fun onSupportVisible() {
        delegate.onSupportVisible()
    }

    /**
     * Called when the fragment is invivible.
     *
     * Is the combination of  [onHiddenChanged() + onResume()/onPause() + setUserVisibleHint()]
     */
    override fun onSupportInvisible() {
        delegate.onSupportInvisible()
    }

    /**
     * Return true if the fragment has been supportVisible.
     */
    override fun isSupportVisible(): Boolean {
        return delegate.isSupportVisible()
    }

    /**
     * Set fragment animation with a higher priority than the ISupportActivity
     * 设定当前 Fragmemt 动画,优先级比在 SupportActivity 里高
     */
    override fun onCreateFragmentAnimator(): FragmentAnimator? {
        return delegate.onCreateFragmentAnimator()
    }

    /**
     * 获取设置的全局动画 copy
     */
    override fun getFragmentAnimator(): FragmentAnimator? {
        return delegate.getFragmentAnimator()
    }

    /**
     * 设置 Fragment 内的全局动画
     */
    override fun setFragmentAnimator(fragmentAnimator: FragmentAnimator?) {
        delegate.setFragmentAnimator(fragmentAnimator)
    }

    /**
     * 按返回键触发,前提是 SupportActivity 的 onBackPressed() 方法能被调用
     *
     * @return false 则继续向上传递，true 则消费掉该事件
     */
    override fun onBackPressedSupport(): Boolean {
        return delegate.onBackPressedSupport()
    }

    /**
     * 类似 [Activity.setResult]
     * Similar to [Activity.setResult]
     *
     * @see [startForResult]
     */
    override fun setFragmentResult(resultCode: Int, bundle: Bundle?) {
        delegate.setFragmentResult(resultCode, bundle)
    }

    /**
     * 类似  [Activity.onActivityResult]
     * Similar to [Activity.onActivityResult]
     *
     * @see [startForResult]
     */
    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        delegate.onFragmentResult(requestCode, resultCode, data)
    }

    /**
     * 在 start(TargetFragment,LaunchMode) 时，启动模式为 SingleTask/SingleTop, 回调 TargetFragment 的该方法
     * 类似 [Activity.onNewIntent]
     *
     * Similar to [Activity.onNewIntent]
     *
     * @param args putNewBundle(Bundle newBundle)
     * @see [start]
     */
    override fun onNewBundle(args: Bundle) {
        delegate.onNewBundle(args)
    }

    /**
     * 添加 NewBundle，用于启动模式为 SingleTask/SingleTop 时
     *
     * @see [start]
     */
    override fun putNewBundle(newBundle: Bundle?) {
        delegate.putNewBundle(newBundle)
    }


    /****************************************以下为可选方法(Optional methods)******************************************************/

    /**
     * 隐藏软键盘
     */
    protected fun hideSoftInput() {
        delegate.hideSoftInput()
    }

    /**
     * 显示软键盘，调用该方法后，会在 onPause 时自动隐藏软键盘
     */
    protected fun showSoftInput(view: View) {
        delegate.showSoftInput(view)
    }

    /**
     * 加载根 Fragment，即 Activity 内的第一个 Fragment 或 Fragment 内的第一个子 Fragment
     *
     * @param containerId 容器id
     * @param toFragment  目标Fragment
     */
    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment?) {
        delegate.loadRootFragment(containerId, toFragment)
    }

    fun loadRootFragment(containerId: Int,
                         toFragment: ISupportFragment?,
                         addToBackStack: Boolean,
                         allowAnim: Boolean) {
        delegate.loadRootFragment(containerId, toFragment, addToBackStack, allowAnim)
    }

    /**
     * 加载多个同级根 Fragment，类似 Wechat, QQ 主页的场景
     */
    fun loadMultipleRootFragment(containerId: Int,
                                 showPosition: Int,
                                 toFragments: Array<out ISupportFragment?>?) {
        delegate.loadMultipleRootFragment(containerId, showPosition, toFragments)
    }

    /**
     * show 一个 Fragment，hide 其他同栈所有 Fragment
     * 使用该方法时，要确保同级栈内无多余的 Fragment，(只有通过 loadMultipleRootFragment() 载入的 Fragment)
     *
     * 建议使用更明确的 [showHideFragment]
     *
     * @param showFragment 需要 show 的 Fragment
     */
    fun showHideFragment(showFragment: ISupportFragment?) {
        delegate.showHideFragment(showFragment)
    }

    /**
     * show 一个 Fragment，hide 一个 Fragment； 主要用于类似微信主页那种 切换 tab 的情况
     */
    fun showHideFragment(showFragment: ISupportFragment?, hideFragment: ISupportFragment?) {
        delegate.showHideFragment(showFragment, hideFragment)
    }

    fun start(toFragment: ISupportFragment?) {
        delegate.start(toFragment)
    }

    /**
     * @param launchMode Similar to Activity's LaunchMode.
     */
    fun start(toFragment: ISupportFragment?, @LaunchMode launchMode: Int) {
        delegate.start(toFragment, launchMode)
    }

    /**
     * Launch an fragment for which you would like a result when it poped.
     */
    fun startForResult(toFragment: ISupportFragment?, requestCode: Int) {
        delegate.startForResult(toFragment, requestCode)
    }

    /**
     * Start the target Fragment and pop itself
     */
    fun startWithPop(toFragment: ISupportFragment?) {
        delegate.startWithPop(toFragment)
    }

    /**
     * @see [popTo]
     * @see [start]
     */
    fun startWithPopTo(toFragment: ISupportFragment?,
                       targetFragmentClass: Class<*>?,
                       includeTargetFragment: Boolean) {
        delegate.startWithPopTo(toFragment, targetFragmentClass, includeTargetFragment)
    }

    fun replaceFragment(toFragment: ISupportFragment?, addToBackStack: Boolean) {
        delegate.replaceFragment(toFragment, addToBackStack)
    }

    fun pop() {
        delegate.pop()
    }

    /**
     * Pop the child fragment.
     */
    fun popChild() {
        delegate.popChild()
    }

    /**
     * Pop the last fragment transition from the manager's fragment
     * back stack.
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
     * 如果你想在出栈后, 立刻进行 FragmentTransaction 操作，请使用该方法
     */
    fun popTo(targetFragmentClass: Class<*>?,
              includeTargetFragment: Boolean,
              afterPopTransactionRunnable: Runnable?) {
        delegate.popTo(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable)
    }

    fun popTo(targetFragmentClass: Class<*>?,
              includeTargetFragment: Boolean,
              afterPopTransactionRunnable: Runnable?,
              popAnim: Int) {
        delegate.popTo(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable, popAnim)
    }

    fun popToChild(targetFragmentClass: Class<*>?, includeTargetFragment: Boolean) {
        delegate.popToChild(targetFragmentClass, includeTargetFragment)
    }

    fun popToChild(targetFragmentClass: Class<*>?,
                   includeTargetFragment: Boolean,
                   afterPopTransactionRunnable: Runnable?) {
        delegate.popToChild(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable)
    }

    fun popToChild(targetFragmentClass: Class<*>?,
                   includeTargetFragment: Boolean,
                   afterPopTransactionRunnable: Runnable?,
                   popAnim: Int) {
        delegate.popToChild(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable, popAnim)
    }

    /**
     * 得到位于栈顶 Fragment
     */
    fun getTopFragment(): ISupportFragment? {
        return SupportHelper.getTopFragment(fragmentManager)
    }

    fun getTopChildFragment(): ISupportFragment? {
        return SupportHelper.getTopFragment(childFragmentManager)
    }

    /**
     * @return 位于当前 Fragment 的前一个 Fragment
     */
    fun getPreFragment(): ISupportFragment? {
        return SupportHelper.getPreFragment(this)
    }

    /**
     * 获取栈内的 Fragment 对象
     */
    fun <T : ISupportFragment> findFragment(fragmentClass: Class<T>?): T? {
        return SupportHelper.findFragment(fragmentManager, fragmentClass)
    }

    /**
     * 获取栈内的 Fragment 对象
     */
    fun <T : ISupportFragment> findChildFragment(fragmentClass: Class<T>?): T? {
        return SupportHelper.findFragment(childFragmentManager, fragmentClass)
    }
}
