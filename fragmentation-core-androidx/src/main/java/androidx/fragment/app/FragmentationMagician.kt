package androidx.fragment.app

import android.util.SparseArray
import java.util.*

/**
 * http://stackoverflow.com/questions/23504790/android-multiple-fragment-transaction-ordering
 *
 *
 * Created by YoKey on 16/1/22.
 */
object FragmentationMagician {
    private var sSupportLessThan25dot4 = false
    private var sSupportGreaterThan27dot1dot0 = false

    init {
        val fields = FragmentManagerImpl::class.java.declaredFields
        for (field in fields) {
            if ("mStopped" == field.name) { //  > v27.1.0
                sSupportGreaterThan27dot1dot0 = true
                break
            } else if ("mAvailIndices" == field.name) { // < 25.4.0
                sSupportLessThan25dot4 = true
                break
            }
        }
    }

    fun isSupportLessThan25dot4(): Boolean {
        return sSupportLessThan25dot4
    }

    fun isExecutingActions(fragmentManager: FragmentManager): Boolean {
        if (fragmentManager !is FragmentManagerImpl) {
            return false
        }
        try {
            return fragmentManager.mExecutingActions
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * To fix the FragmentManagerImpl.mAvailIndices incorrect ordering when pop() multiple Fragments
     * on pre-support-v4-25.4.0
     */
    @Suppress("UNCHECKED_CAST")
    fun reorderIndices(fragmentManager: FragmentManager?) {
        if (!sSupportLessThan25dot4) {
            return
        }
        if (fragmentManager !is FragmentManagerImpl) {
            return
        }
        try {
            val `object` = getValue(fragmentManager) ?: return
            val arrayList = `object` as ArrayList<Int>
            if (arrayList.size > 1) {
                Collections.sort(arrayList, Collections.reverseOrder<Any>())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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

    /**
     * On 25.4.0+，fragmentManager.getFragments () returns mAdd, instead of the mActive on 25.4.0-
     */
    @Suppress("UNCHECKED_CAST")
    fun getActiveFragments(fragmentManager: FragmentManager?): List<Fragment?>? {
        if (fragmentManager !is FragmentManagerImpl) {
            return Collections.EMPTY_LIST as List<Fragment>
        }
        // For pre-25.4.0
        if (sSupportLessThan25dot4) {
            return fragmentManager.getFragments()
        }

        // For compat 25.4.0+
        try {
            // Since v4-25.4.0，mActive: ArrayList -> SparseArray
            return getActiveList(fragmentManager.mActive)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return fragmentManager.getFragments()
    }

    @Suppress("UNCHECKED_CAST")
    private fun getActiveList(active: SparseArray<Fragment>?): List<Fragment> {
        if (active == null) {
            return Collections.EMPTY_LIST as List<Fragment>
        }
        val count = active.size()
        val fragments = ArrayList<Fragment>(count)
        for (i in 0 until count) {
            fragments.add(active.valueAt(i))
        }
        return fragments
    }

    private fun getValue(`object`: Any): Any? {
        val clazz = `object`.javaClass
        try {
            val field = clazz.getDeclaredField("mAvailIndices")
            field.isAccessible = true
            return field.get(`object`)
        } catch (ignored: Exception) {
        }
        return null
    }

    private fun hookStateSaved(fragmentManager: FragmentManager?, runnable: Runnable) {
        if (fragmentManager !is FragmentManagerImpl) {
            return
        }

        if (isStateSaved(fragmentManager)) {
            fragmentManager.mStateSaved = false
            compatRunAction(fragmentManager, runnable)
            fragmentManager.mStateSaved = true
        } else {
            runnable.run()
        }
    }

    /**
     * Compat v27.1.0+
     *
     * So the code to compile Fragmentation needs v27.1.0+
     *
     * @see FragmentManager.isStateSaved
     */
    private fun compatRunAction(fragmentManagerImpl: FragmentManagerImpl, runnable: Runnable) {
        if (!sSupportGreaterThan27dot1dot0) {
            runnable.run()
            return
        }

        fragmentManagerImpl.mStopped = false
        runnable.run()
        fragmentManagerImpl.mStopped = true
    }
}