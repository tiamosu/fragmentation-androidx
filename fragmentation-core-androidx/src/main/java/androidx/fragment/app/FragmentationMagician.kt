package androidx.fragment.app

/**
 * http://stackoverflow.com/questions/23504790/android-multiple-fragment-transaction-ordering
 *
 * 这是一个历史性问题，在 androidx 之前，不同版本 fragment 的栈顺序会有不同，上面这个链接说明了如何解决该问题。
 * 现在使用了在 androidx，没有这些问题了。
 *
 * Created by YoKey on 16/1/22.
 */
object FragmentationMagician {

    fun isStateSaved(fragmentManager: FragmentManager?): Boolean {
        if (fragmentManager !is FragmentManagerImpl) {
            return false
        }
        try {
            return fragmentManager.mStateSaved
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Like [FragmentManager.popBackStack]} but allows the commit to be executed after an
     * activity's state is saved.  This is dangerous because the action can
     * be lost if the activity needs to later be restored from its state, so
     * this should only be used for cases where it is okay for the UI state
     * to change unexpectedly on the user.
     */
    fun popBackStackAllowingStateLoss(fragmentManager: FragmentManager?) {
        hookStateSaved(fragmentManager, Runnable { fragmentManager?.popBackStack() })
    }

    /**
     * Like [FragmentManager.popBackStackImmediate]} but allows the commit to be executed after an
     * activity's state is saved.
     */
    fun popBackStackImmediateAllowingStateLoss(fragmentManager: FragmentManager) {
        hookStateSaved(fragmentManager, Runnable { fragmentManager.popBackStackImmediate() })
    }

    /**
     * Like [FragmentManager.popBackStackImmediate]} but allows the commit to be executed after an
     * activity's state is saved.
     */
    fun popBackStackAllowingStateLoss(fragmentManager: FragmentManager?, name: String?, flags: Int) {
        hookStateSaved(fragmentManager, Runnable { fragmentManager?.popBackStack(name, flags) })
    }

    /**
     * Like [FragmentManager.executePendingTransactions] but allows the commit to be executed after an
     * activity's state is saved.
     */
    fun executePendingTransactionsAllowingStateLoss(fragmentManager: FragmentManager?) {
        hookStateSaved(fragmentManager, Runnable { fragmentManager?.executePendingTransactions() })
    }

    fun getAddedFragments(fragmentManager: FragmentManager?): List<Fragment?>? {
        return fragmentManager?.fragments
    }

    private fun hookStateSaved(fragmentManager: FragmentManager?, runnable: Runnable) {
        if (fragmentManager !is FragmentManagerImpl) {
            return
        }
        if (isStateSaved(fragmentManager)) {
            val tempStateSaved = fragmentManager.mStateSaved
            val tempStopped: Boolean = fragmentManager.mStopped
            fragmentManager.mStateSaved = false
            fragmentManager.mStopped = false

            runnable.run()

            fragmentManager.mStopped = tempStopped
            fragmentManager.mStateSaved = tempStateSaved
        } else {
            runnable.run()
        }
    }
}