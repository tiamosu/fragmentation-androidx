package me.yokeyword.fragmentation

import android.os.Build
import android.view.View
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import me.yokeyword.fragmentation.ISupportFragment.LaunchMode
import me.yokeyword.fragmentation.helper.internal.TransactionRecord
import java.util.*

/**
 * Created by YoKey on 16/11/24.
 */
abstract class ExtraTransaction {

    /**
     * @param tag Optional tag name for the fragment, to later retrieve the
     * , pop(String)
     * or FragmentManager.findFragmentByTag(String).
     */
    abstract fun setTag(tag: String): ExtraTransaction

    /**
     * Set specific animation resources to run for the fragments that are
     * entering and exiting in this transaction. These animations will not be
     * played when popping the back stack.
     */
    abstract fun setCustomAnimations(@AnimatorRes @AnimRes targetFragmentEnter: Int,
                                     @AnimatorRes @AnimRes currentFragmentPopExit: Int): ExtraTransaction

    /**
     * Set specific animation resources to run for the fragments that are
     * entering and exiting in this transaction. The `currentFragmentPopEnter`
     * and `targetFragmentExit` animations will be played for targetFragmentEnter/currentFragmentPopExit
     * operations specifically when popping the back stack.
     */
    abstract fun setCustomAnimations(@AnimatorRes @AnimRes targetFragmentEnter: Int,
                                     @AnimatorRes @AnimRes currentFragmentPopExit: Int,
                                     @AnimatorRes @AnimRes currentFragmentPopEnter: Int,
                                     @AnimatorRes @AnimRes targetFragmentExit: Int): ExtraTransaction

    /**
     * Used with custom Transitions to map a View from a removed or hidden
     * Fragment to a View from a shown or added Fragment.
     * <var>sharedElement</var> must have a unique transitionName in the View hierarchy.
     *
     * @param sharedElement A View in a disappearing Fragment to match with a View in an
     * appearing Fragment.
     * @param sharedName    The transitionName for a View in an appearing Fragment to match to the shared
     * element.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    abstract fun addSharedElement(sharedElement: View, sharedName: String): ExtraTransaction

    abstract fun loadRootFragment(containerId: Int, toFragment: ISupportFragment)

    abstract fun loadRootFragment(containerId: Int, toFragment: ISupportFragment,
                                  addToBackStack: Boolean, allowAnim: Boolean)

    abstract fun start(toFragment: ISupportFragment)

    abstract fun startDontHideSelf(toFragment: ISupportFragment)

    abstract fun startDontHideSelf(toFragment: ISupportFragment, @LaunchMode launchMode: Int)

    abstract fun start(toFragment: ISupportFragment, @LaunchMode launchMode: Int)

    abstract fun startForResult(toFragment: ISupportFragment, requestCode: Int)

    abstract fun startForResultDontHideSelf(toFragment: ISupportFragment, requestCode: Int)

    abstract fun startWithPop(toFragment: ISupportFragment)

    abstract fun startWithPopTo(toFragment: ISupportFragment, targetFragmentTag: String,
                                includeTargetFragment: Boolean)

    abstract fun replace(toFragment: ISupportFragment)

    /**
     * 使用setTag()自定义Tag时，使用下面popTo()／popToChild()出栈
     *
     * @param targetFragmentTag     通过setTag()设置的tag
     * @param includeTargetFragment 是否包含目标(Tag为targetFragmentTag)Fragment
     */
    abstract fun popTo(targetFragmentTag: String, includeTargetFragment: Boolean)

    abstract fun popTo(targetFragmentTag: String, includeTargetFragment: Boolean,
                       afterPopTransactionRunnable: Runnable?, popAnim: Int)

    abstract fun popToChild(targetFragmentTag: String, includeTargetFragment: Boolean)

    abstract fun popToChild(targetFragmentTag: String, includeTargetFragment: Boolean,
                            afterPopTransactionRunnable: Runnable?, popAnim: Int)

    /**
     * Don't add this extraTransaction to the back stack.
     * If you use this function to don't add to BackStack ,
     * then you must call {@link [DontAddToBackStackTransaction] # [remove] } when leaving the fragment.
     */
    abstract fun dontAddToBackStack(): DontAddToBackStackTransaction

    /**
     * 使用 dontAddToBackStack() 加载 Fragment 时， 使用 remove() 移除 Fragment
     */
    abstract fun remove(fragment: ISupportFragment, showPreFragment: Boolean)

    interface DontAddToBackStackTransaction {
        /**
         * add() +  hide(preFragment)
         */
        fun start(toFragment: ISupportFragment)

        /**
         * only add()
         */
        fun add(toFragment: ISupportFragment)

        /**
         * replace()
         */
        fun replace(toFragment: ISupportFragment)
    }

    internal class ExtraTransactionImpl<T : ISupportFragment>(
            private val activity: FragmentActivity?, private val supportF: T,
            private val transactionDelegate: TransactionDelegate?, private val fromActivity: Boolean)
        : ExtraTransaction(), DontAddToBackStackTransaction {

        private val fragment: Fragment?
        private val transactionRecord: TransactionRecord

        private val fragmentManager: FragmentManager?
            get() = if (fragment == null) {
                activity?.supportFragmentManager
            } else fragment.fragmentManager

        init {
            this.fragment = supportF as Fragment
            transactionRecord = TransactionRecord()
        }

        override fun setTag(tag: String): ExtraTransaction {
            transactionRecord.tag = tag
            return this
        }

        override fun setCustomAnimations(@AnimRes targetFragmentEnter: Int, @AnimRes currentFragmentPopExit: Int): ExtraTransaction {
            transactionRecord.targetFragmentEnter = targetFragmentEnter
            transactionRecord.currentFragmentPopExit = currentFragmentPopExit
            transactionRecord.currentFragmentPopEnter = 0
            transactionRecord.targetFragmentExit = 0
            return this
        }

        override fun setCustomAnimations(@AnimRes targetFragmentEnter: Int,
                                         @AnimRes currentFragmentPopExit: Int,
                                         @AnimRes currentFragmentPopEnter: Int,
                                         @AnimRes targetFragmentExit: Int): ExtraTransaction {
            transactionRecord.targetFragmentEnter = targetFragmentEnter
            transactionRecord.currentFragmentPopExit = currentFragmentPopExit
            transactionRecord.currentFragmentPopEnter = currentFragmentPopEnter
            transactionRecord.targetFragmentExit = targetFragmentExit
            return this
        }

        override fun addSharedElement(sharedElement: View, sharedName: String): ExtraTransaction {
            if (transactionRecord.sharedElementList == null) {
                transactionRecord.sharedElementList = ArrayList()
            }
            transactionRecord.sharedElementList!!.add(TransactionRecord.SharedElement(sharedElement, sharedName))
            return this
        }

        override fun loadRootFragment(containerId: Int, toFragment: ISupportFragment) {
            loadRootFragment(containerId, toFragment, true, false)
        }

        override fun loadRootFragment(containerId: Int, toFragment: ISupportFragment,
                                      addToBackStack: Boolean, allowAnim: Boolean) {
            toFragment.getSupportDelegate().transactionRecord = transactionRecord
            transactionDelegate?.loadRootTransaction(fragmentManager, containerId, toFragment,
                    addToBackStack, allowAnim)
        }

        override fun dontAddToBackStack(): DontAddToBackStackTransaction {
            transactionRecord.dontAddToBackStack = true
            return this
        }

        override fun remove(fragment: ISupportFragment, showPreFragment: Boolean) {
            transactionDelegate?.remove(fragmentManager, fragment as Fragment, showPreFragment)
        }

        override fun popTo(targetFragmentTag: String, includeTargetFragment: Boolean) {
            popTo(targetFragmentTag, includeTargetFragment, null,
                    TransactionDelegate.DEFAULT_POPTO_ANIM)
        }

        override fun popTo(targetFragmentTag: String, includeTargetFragment: Boolean,
                           afterPopTransactionRunnable: Runnable?, popAnim: Int) {
            transactionDelegate?.popTo(targetFragmentTag, includeTargetFragment,
                    afterPopTransactionRunnable, fragmentManager, popAnim)
        }

        override fun popToChild(targetFragmentTag: String, includeTargetFragment: Boolean) {
            popToChild(targetFragmentTag, includeTargetFragment, null,
                    TransactionDelegate.DEFAULT_POPTO_ANIM)
        }

        override fun popToChild(targetFragmentTag: String, includeTargetFragment: Boolean,
                                afterPopTransactionRunnable: Runnable?, popAnim: Int) {
            if (fromActivity) {
                popTo(targetFragmentTag, includeTargetFragment, afterPopTransactionRunnable, popAnim)
            } else {
                transactionDelegate?.popTo(targetFragmentTag, includeTargetFragment,
                        afterPopTransactionRunnable, fragment!!.childFragmentManager, popAnim)
            }
        }

        override fun add(toFragment: ISupportFragment) {
            toFragment.getSupportDelegate().transactionRecord = transactionRecord
            transactionDelegate?.dispatchStartTransaction(fragmentManager, supportF,
                    toFragment, 0, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_WITHOUT_HIDE)
        }

        override fun start(toFragment: ISupportFragment) {
            start(toFragment, ISupportFragment.STANDARD)
        }

        override fun startDontHideSelf(toFragment: ISupportFragment) {
            toFragment.getSupportDelegate().transactionRecord = transactionRecord
            transactionDelegate?.dispatchStartTransaction(fragmentManager, supportF,
                    toFragment, 0, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_WITHOUT_HIDE)
        }

        override fun startDontHideSelf(toFragment: ISupportFragment, @LaunchMode launchMode: Int) {
            toFragment.getSupportDelegate().transactionRecord = transactionRecord
            transactionDelegate?.dispatchStartTransaction(fragmentManager, supportF,
                    toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD_WITHOUT_HIDE)
        }

        override fun start(toFragment: ISupportFragment, @LaunchMode launchMode: Int) {
            toFragment.getSupportDelegate().transactionRecord = transactionRecord
            transactionDelegate?.dispatchStartTransaction(fragmentManager, supportF, toFragment,
                    0, launchMode, TransactionDelegate.TYPE_ADD)
        }

        override fun startForResult(toFragment: ISupportFragment, requestCode: Int) {
            toFragment.getSupportDelegate().transactionRecord = transactionRecord
            transactionDelegate?.dispatchStartTransaction(fragmentManager, supportF, toFragment,
                    requestCode, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT)
        }

        override fun startForResultDontHideSelf(toFragment: ISupportFragment, requestCode: Int) {
            toFragment.getSupportDelegate().transactionRecord = transactionRecord
            transactionDelegate?.dispatchStartTransaction(fragmentManager, supportF, toFragment,
                    requestCode, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT_WITHOUT_HIDE)
        }

        override fun startWithPop(toFragment: ISupportFragment) {
            toFragment.getSupportDelegate().transactionRecord = transactionRecord
            transactionDelegate?.startWithPop(fragmentManager, supportF, toFragment)
        }

        override fun startWithPopTo(toFragment: ISupportFragment, targetFragmentTag: String, includeTargetFragment: Boolean) {
            toFragment.getSupportDelegate().transactionRecord = transactionRecord
            transactionDelegate?.startWithPopTo(fragmentManager, supportF, toFragment,
                    targetFragmentTag, includeTargetFragment)
        }

        override fun replace(toFragment: ISupportFragment) {
            toFragment.getSupportDelegate().transactionRecord = transactionRecord
            transactionDelegate?.dispatchStartTransaction(fragmentManager, supportF,
                    toFragment, 0, ISupportFragment.STANDARD, TransactionDelegate.TYPE_REPLACE)
        }
    }
}
