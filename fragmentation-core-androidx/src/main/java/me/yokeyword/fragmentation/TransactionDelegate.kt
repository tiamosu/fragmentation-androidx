package me.yokeyword.fragmentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.*
import me.yokeyword.fragmentation.exception.AfterSaveStateTransactionWarning
import me.yokeyword.fragmentation.helper.internal.ResultRecord
import me.yokeyword.fragmentation.helper.internal.TransactionRecord
import me.yokeyword.fragmentation.queue.Action
import me.yokeyword.fragmentation.queue.ActionQueue
import java.util.*

/**
 * Controller
 * Created by YoKeyword on 16/1/22.
 */
class TransactionDelegate internal constructor(private val supportA: ISupportActivity) {
    private val activity: FragmentActivity
    private var handler: Handler = Handler(Looper.getMainLooper())
    internal var actionQueue: ActionQueue

    init {
        if (supportA !is FragmentActivity) {
            throw RuntimeException("must extends FragmentActivity/AppCompatActivity")
        }
        this.activity = supportA
        actionQueue = ActionQueue(handler)
    }

    internal fun post(runnable: Runnable) {
        actionQueue.enqueue(object : Action() {
            override fun run() {
                runnable.run()
            }
        })
    }

    internal fun loadRootTransaction(fm: FragmentManager?, containerId: Int, to: ISupportFragment?,
                                     addToBackStack: Boolean, allowAnimation: Boolean) {
        enqueue(fm, object : Action(ACTION_LOAD) {
            override fun run() {
                bindContainerId(containerId, to)

                var toFragmentTag: String? = to?.javaClass?.name
                val transactionRecord = to?.getSupportDelegate()?.transactionRecord
                if (transactionRecord != null) {
                    if (transactionRecord.tag != null) {
                        toFragmentTag = transactionRecord.tag
                    }
                }

                start(fm, null, to, toFragmentTag, !addToBackStack, null, allowAnimation, TYPE_REPLACE)
            }
        })
    }

    internal fun loadMultipleRootTransaction(fm: FragmentManager?, containerId: Int,
                                             showPosition: Int, tos: Array<out ISupportFragment?>) {
        enqueue(fm, object : Action(ACTION_LOAD) {
            override fun run() {
                val ft = fm?.beginTransaction()
                for (i in tos.indices) {
                    val to = tos[i] as Fragment
                    val args = getArguments(to)
                    args.putInt(FRAGMENTATION_ARG_ROOT_STATUS, SupportFragmentDelegate.STATUS_ROOT_ANIM_DISABLE)
                    bindContainerId(containerId, tos[i])

                    val toName = to.javaClass.name
                    ft?.add(containerId, to, toName)

                    if (i != showPosition) {
                        ft?.hide(to)
                    }
                }

                supportCommit(fm, ft)
            }
        })
    }

    /**
     * Dispatch the start transaction.
     */
    internal fun dispatchStartTransaction(fm: FragmentManager?, from: ISupportFragment?,
                                          to: ISupportFragment?, requestCode: Int, launchMode: Int, type: Int) {
        enqueue(fm, object : Action(if (launchMode == ISupportFragment.SINGLETASK) ACTION_POP_MOCK else ACTION_NORMAL) {
            override fun run() {
                doDispatchStartTransaction(fm, from, to, requestCode, launchMode, type)
            }
        })
    }

    /**
     * Show showFragment then hide hideFragment
     */
    internal fun showHideFragment(fm: FragmentManager?, showFragment: ISupportFragment?, hideFragment: ISupportFragment?) {
        enqueue(fm, object : Action() {
            override fun run() {
                doShowHideFragment(fm, showFragment, hideFragment)
            }
        })
    }

    /**
     * Start the target Fragment and pop itself
     */
    internal fun startWithPop(fm: FragmentManager?, from: ISupportFragment?, to: ISupportFragment?) {
        enqueue(fm, object : Action(ACTION_POP_MOCK) {
            override fun run() {
                val top = getTopFragmentForStart(from, fm)
                        ?: throw NullPointerException("There is no Fragment in the FragmentManager, maybe you need to call loadRootFragment() first!")

                val containerId = top.getSupportDelegate().containerId
                bindContainerId(containerId, to)

                handleAfterSaveInStateTransactionException(fm, "popTo()")
                FragmentationMagician.executePendingTransactionsAllowingStateLoss(fm)
                top.getSupportDelegate().lockAnim = true
                if (!FragmentationMagician.isStateSaved(fm)) {
                    mockStartWithPopAnim(SupportHelper.getTopFragment(fm), to, top.getSupportDelegate().animHelper!!.popExitAnim)
                }

                removeTopFragment(fm)
                FragmentationMagician.popBackStackAllowingStateLoss(fm)
                FragmentationMagician.executePendingTransactionsAllowingStateLoss(fm)
            }
        })

        dispatchStartTransaction(fm, from, to, 0, ISupportFragment.STANDARD, TYPE_ADD)
    }

    internal fun startWithPopTo(fm: FragmentManager?, from: ISupportFragment?, to: ISupportFragment?,
                                fragmentTag: String?, includeTargetFragment: Boolean) {
        enqueue(fm, object : Action(ACTION_POP_MOCK) {
            override fun run() {
                var flag = 0
                if (includeTargetFragment) {
                    flag = FragmentManager.POP_BACK_STACK_INCLUSIVE
                }

                val willPopFragments = SupportHelper.getWillPopFragments(fm, fragmentTag, includeTargetFragment)
                val top = getTopFragmentForStart(from, fm)
                        ?: throw NullPointerException("There is no Fragment in the FragmentManager, maybe you need to call loadRootFragment() first!")

                val containerId = top.getSupportDelegate().containerId
                bindContainerId(containerId, to)

                if (willPopFragments.isEmpty()) {
                    return
                }

                handleAfterSaveInStateTransactionException(fm, "startWithPopTo()")
                FragmentationMagician.executePendingTransactionsAllowingStateLoss(fm)
                if (!FragmentationMagician.isStateSaved(fm)) {
                    mockStartWithPopAnim(SupportHelper.getTopFragment(fm), to, top.getSupportDelegate().animHelper!!.popExitAnim)
                }

                safePopTo(fragmentTag, fm, flag, willPopFragments)
            }
        })

        dispatchStartTransaction(fm, from, to, 0, ISupportFragment.STANDARD, TYPE_ADD)
    }

    /**
     * Remove
     * Only allowed in interfaces  {@link [ExtraTransaction.DontAddToBackStackTransaction] # [ExtraTransaction.remove] }
     */
    internal fun remove(fm: FragmentManager?, fragment: Fragment, showPreFragment: Boolean) {
        enqueue(fm, object : Action(ACTION_POP, fm) {
            override fun run() {
                val ft = fm?.beginTransaction()
                        ?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        ?.remove(fragment)

                if (showPreFragment) {
                    val preFragment = SupportHelper.getPreFragment(fragment)
                    if (preFragment is Fragment) {
                        ft?.show(preFragment)
                    }
                }
                supportCommit(fm, ft)
            }
        })
    }

    /**
     * Pop
     */
    internal fun pop(fm: FragmentManager?) {
        enqueue(fm, object : Action(ACTION_POP, fm) {
            override fun run() {
                handleAfterSaveInStateTransactionException(fm, "pop()")
                FragmentationMagician.popBackStackAllowingStateLoss(fm)
                removeTopFragment(fm)
            }
        })
    }

    private fun removeTopFragment(fm: FragmentManager?) {
        try { // Safe popBackStack()
            val top = SupportHelper.getBackStackTopFragment(fm)
            if (top is Fragment) {
                fm?.beginTransaction()
                        ?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        ?.remove(top)
                        ?.commitAllowingStateLoss()
            }
        } catch (ignored: Exception) {
        }
    }

    internal fun popQuiet(fm: FragmentManager?, fragment: Fragment?) {
        enqueue(fm, object : Action(ACTION_POP_MOCK) {
            override fun run() {
                supportA.getSupportDelegate().popMultipleNoAnim = true
                removeTopFragment(fm)
                FragmentationMagician.popBackStackAllowingStateLoss(fm, fragment?.tag, 0)
                FragmentationMagician.popBackStackAllowingStateLoss(fm)
                FragmentationMagician.executePendingTransactionsAllowingStateLoss(fm)
                supportA.getSupportDelegate().popMultipleNoAnim = false
            }
        })
    }

    /**
     * Pop the last fragment transition from the manager's fragment pop stack.
     *
     * @param targetFragmentTag     Tag
     * @param includeTargetFragment Whether it includes targetFragment
     */
    internal fun popTo(targetFragmentTag: String?, includeTargetFragment: Boolean,
                       afterPopTransactionRunnable: Runnable?, fm: FragmentManager?, popAnim: Int) {
        enqueue(fm, object : Action(ACTION_POP_MOCK) {
            override fun run() {
                doPopTo(targetFragmentTag, includeTargetFragment, fm, popAnim)

                afterPopTransactionRunnable?.run()
            }
        })
    }

    /**
     * Dispatch the pop-event. Priority of the top of the stack of Fragment
     */
    internal fun dispatchBackPressedEvent(activeFragment: ISupportFragment?): Boolean {
        if (activeFragment != null) {
            val result = activeFragment.onBackPressedSupport()
            if (result) {
                return true
            }

            val parentFragment = (activeFragment as Fragment).parentFragment
            return dispatchBackPressedEvent(parentFragment as? ISupportFragment)
        }
        return false
    }

    internal fun handleResultRecord(from: Fragment?) {
        try {
            val args = from?.arguments ?: return
            val resultRecord = args.getParcelable<ResultRecord>(FRAGMENTATION_ARG_RESULT_RECORD)
                    ?: return

            var targetFragment: ISupportFragment? = null
            if (from.fragmentManager != null) {
                targetFragment = from.fragmentManager!!
                        .getFragment(from.arguments!!, FRAGMENTATION_STATE_SAVE_RESULT) as? ISupportFragment
            }
            targetFragment?.onFragmentResult(resultRecord.requestCode, resultRecord.resultCode, resultRecord.resultBundle)
        } catch (ignored: IllegalStateException) {
            // Fragment no longer exists
        }
    }

    private fun enqueue(fm: FragmentManager?, action: Action) {
        if (fm == null) {
            Log.w(TAG, "FragmentManager is null, skip the action!")
            return
        }
        actionQueue.enqueue(action)
    }

    private fun doDispatchStartTransaction(fm: FragmentManager?, from: ISupportFragment?,
                                           to: ISupportFragment?, requestCode: Int, launchMode: Int, type: Int) {
        var fromTemp = from
        if ((type == TYPE_ADD_RESULT || type == TYPE_ADD_RESULT_WITHOUT_HIDE) && fromTemp != null) {
            if (!(fromTemp as Fragment).isAdded) {
                Log.w(TAG, fromTemp.javaClass.simpleName + " has not been attached yet! startForResult() converted to start()")
            } else {
                saveRequestCode(fm, fromTemp, to as? Fragment, requestCode)
            }
        }

        fromTemp = getTopFragmentForStart(fromTemp, fm)

        val containerId = getArguments(to as? Fragment).getInt(FRAGMENTATION_ARG_CONTAINER, 0)
        if (fromTemp == null && containerId == 0) {
            Log.e(TAG, "There is no Fragment in the FragmentManager, maybe you need to call loadRootFragment()!")
            return
        }

        if (fromTemp != null && containerId == 0) {
            bindContainerId(fromTemp.getSupportDelegate().containerId, to)
        }

        // process ExtraTransaction
        var toFragmentTag: String? = to?.javaClass?.name
        var dontAddToBackStack = false
        var sharedElementList: ArrayList<TransactionRecord.SharedElement>? = null
        val transactionRecord = to?.getSupportDelegate()?.transactionRecord
        if (transactionRecord != null) {
            if (transactionRecord.tag != null) {
                toFragmentTag = transactionRecord.tag
            }
            dontAddToBackStack = transactionRecord.dontAddToBackStack
            if (transactionRecord.sharedElementList != null) {
                sharedElementList = transactionRecord.sharedElementList
            }
        }

        if (handleLaunchMode(fm, fromTemp, to, toFragmentTag, launchMode)) {
            return
        }

        start(fm, fromTemp, to, toFragmentTag, dontAddToBackStack, sharedElementList, false, type)
    }

    private fun getTopFragmentForStart(from: ISupportFragment?, fm: FragmentManager?): ISupportFragment? {
        val top: ISupportFragment?
        top = if (from == null) {
            SupportHelper.getTopFragment(fm)
        } else {
            if (from.getSupportDelegate().containerId == 0) {
                val fromF = from as? Fragment
                check(!(fromF?.tag != null && !fromF.tag!!.startsWith("android:switcher:"))) { "Can't find container, please call loadRootFragment() first!" }
            }
            SupportHelper.getTopFragment(fm, from.getSupportDelegate().containerId)
        }
        return top
    }

    private fun start(fm: FragmentManager?, from: ISupportFragment?, to: ISupportFragment?, toFragmentTag: String?,
                      dontAddToBackStack: Boolean, sharedElementList: ArrayList<TransactionRecord.SharedElement>?, allowRootFragmentAnim: Boolean, type: Int) {
        val ft = fm?.beginTransaction()
        val addMode = type == TYPE_ADD || type == TYPE_ADD_RESULT || type == TYPE_ADD_WITHOUT_HIDE || type == TYPE_ADD_RESULT_WITHOUT_HIDE
        val fromF = from as? Fragment
        val toF = to as Fragment
        val args = getArguments(toF)
        args.putBoolean(FRAGMENTATION_ARG_REPLACE, !addMode)

        if (sharedElementList == null) {
            if (addMode) { // Replace mode forbidden animation, the replace animations exist overlapping Bug on support-v4.
                val record = to.getSupportDelegate().transactionRecord
                if (record != null && record.targetFragmentEnter != Integer.MIN_VALUE) {
                    ft?.setCustomAnimations(record.targetFragmentEnter, record.currentFragmentPopExit,
                            record.currentFragmentPopEnter, record.targetFragmentExit)
                    args.putInt(FRAGMENTATION_ARG_CUSTOM_ENTER_ANIM, record.targetFragmentEnter)
                    args.putInt(FRAGMENTATION_ARG_CUSTOM_EXIT_ANIM, record.targetFragmentExit)
                    args.putInt(FRAGMENTATION_ARG_CUSTOM_POP_EXIT_ANIM, record.currentFragmentPopExit)
                } else {
                    ft?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                }
            } else {
                args.putInt(FRAGMENTATION_ARG_ROOT_STATUS, SupportFragmentDelegate.STATUS_ROOT_ANIM_DISABLE)
            }
        } else {
            args.putBoolean(FRAGMENTATION_ARG_IS_SHARED_ELEMENT, true)
            for (item in sharedElementList) {
                ft?.addSharedElement(item.sharedElement, item.sharedName)
            }
        }
        if (from == null) {
            ft?.replace(args.getInt(FRAGMENTATION_ARG_CONTAINER), toF, toFragmentTag)
            if (!addMode) {
                ft?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                args.putInt(FRAGMENTATION_ARG_ROOT_STATUS, if (allowRootFragmentAnim)
                    SupportFragmentDelegate.STATUS_ROOT_ANIM_ENABLE
                else
                    SupportFragmentDelegate.STATUS_ROOT_ANIM_DISABLE)
            }
        } else {
            if (addMode) {
                ft?.add(from.getSupportDelegate().containerId, toF, toFragmentTag)
                if (type != TYPE_ADD_WITHOUT_HIDE && type != TYPE_ADD_RESULT_WITHOUT_HIDE) {
                    ft?.hide(fromF!!)
                }
            } else {
                ft?.replace(from.getSupportDelegate().containerId, toF, toFragmentTag)
            }
        }

        if (!dontAddToBackStack && type != TYPE_REPLACE_DONT_BACK) {
            ft?.addToBackStack(toFragmentTag)
        }
        supportCommit(fm, ft)
    }

    private fun doShowHideFragment(fm: FragmentManager?, showFragment: ISupportFragment?, hideFragment: ISupportFragment?) {
        if (showFragment === hideFragment || showFragment == null) {
            return
        }

        val ft = fm?.beginTransaction()?.show(showFragment as Fragment)
        if (hideFragment == null) {
            val fragmentList = FragmentationMagician.getAddedFragments(fm) ?: return
            for (fragment in fragmentList) {
                if (fragment != null && fragment !== showFragment) {
                    ft?.hide(fragment)
                }
            }
        } else {
            ft?.hide((hideFragment as? Fragment)!!)
        }
        supportCommit(fm, ft)
    }

    private fun bindContainerId(containerId: Int, to: ISupportFragment?) {
        val args = getArguments(to as? Fragment)
        args.putInt(FRAGMENTATION_ARG_CONTAINER, containerId)
    }

    private fun getArguments(fragment: Fragment?): Bundle {
        var bundle = fragment?.arguments
        if (bundle == null) {
            bundle = Bundle()
            fragment?.arguments = bundle
        }
        return bundle
    }

    private fun supportCommit(fm: FragmentManager?, transaction: FragmentTransaction?) {
        handleAfterSaveInStateTransactionException(fm, "commit()")
        transaction?.commitAllowingStateLoss()
    }

    private fun handleLaunchMode(fm: FragmentManager?, topFragment: ISupportFragment?,
                                 to: ISupportFragment?, toFragmentTag: String?, launchMode: Int): Boolean {
        if (topFragment == null) {
            return false
        }
        val stackToFragment = SupportHelper.findBackStackFragment(to?.javaClass, toFragmentTag, fm)
                ?: return false

        if (launchMode == ISupportFragment.SINGLETOP) {
            if (to === topFragment || to?.javaClass?.name == topFragment.javaClass.name) {
                handleNewBundle(to, stackToFragment)
                return true
            }
        } else if (launchMode == ISupportFragment.SINGLETASK) {
            doPopTo(toFragmentTag, false, fm, DEFAULT_POPTO_ANIM)
            handler.post { handleNewBundle(to, stackToFragment) }
            return true
        }
        return false
    }

    private fun handleNewBundle(toFragment: ISupportFragment?, stackToFragment: ISupportFragment?) {
        val argsNewBundle = toFragment?.getSupportDelegate()?.newBundle
        val args = getArguments(toFragment as? Fragment)
        if (args.containsKey(FRAGMENTATION_ARG_CONTAINER)) {
            args.remove(FRAGMENTATION_ARG_CONTAINER)
        }

        if (argsNewBundle != null) {
            args.putAll(argsNewBundle)
        }

        stackToFragment!!.onNewBundle(args)
    }

    /**
     * save requestCode
     */
    private fun saveRequestCode(fm: FragmentManager?, from: Fragment, to: Fragment?, requestCode: Int) {
        val bundle = getArguments(to)
        val resultRecord = ResultRecord()
        resultRecord.requestCode = requestCode
        bundle.putParcelable(FRAGMENTATION_ARG_RESULT_RECORD, resultRecord)
        fm?.putFragment(bundle, FRAGMENTATION_STATE_SAVE_RESULT, from)
    }

    private fun doPopTo(targetFragmentTag: String?, includeTargetFragment: Boolean, fm: FragmentManager?, popAnim: Int) {
        handleAfterSaveInStateTransactionException(fm, "popTo()")

        val targetFragment = fm?.findFragmentByTag(targetFragmentTag)
        if (targetFragment == null) {
            Log.e(TAG, "Pop failure! Can't find FragmentTag:$targetFragmentTag in the FragmentManager's Stack.")
            return
        }

        var flag = 0
        if (includeTargetFragment) {
            flag = FragmentManager.POP_BACK_STACK_INCLUSIVE
        }

        val willPopFragments = SupportHelper.getWillPopFragments(fm, targetFragmentTag, includeTargetFragment)
        if (willPopFragments.isEmpty()) {
            return
        }

        val top = willPopFragments[0]
        mockPopToAnim(top, targetFragmentTag, fm, flag, willPopFragments, popAnim)
    }

    private fun safePopTo(fragmentTag: String?, fm: FragmentManager?, flag: Int, willPopFragments: List<Fragment>) {
        supportA.getSupportDelegate().popMultipleNoAnim = true

        val transaction = fm?.beginTransaction()
                ?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
        for (fragment in willPopFragments) {
            transaction?.remove(fragment)
        }
        transaction?.commitAllowingStateLoss()

        FragmentationMagician.popBackStackAllowingStateLoss(fm, fragmentTag, flag)
        FragmentationMagician.executePendingTransactionsAllowingStateLoss(fm)
        supportA.getSupportDelegate().popMultipleNoAnim = false
    }

    private fun mockPopToAnim(from: Fragment, targetFragmentTag: String?, fm: FragmentManager?,
                              flag: Int, willPopFragments: List<Fragment>, popAnim: Int) {
        if (from !is ISupportFragment) {
            safePopTo(targetFragmentTag, fm, flag, willPopFragments)
            return
        }

        val fromSupport = from as ISupportFragment
        val container = findContainerById(from, fromSupport.getSupportDelegate().containerId)
                ?: return

        val fromView = from.view ?: return
        if (fromView.animation != null) {
            fromView.clearAnimation()
        }

        container.removeViewInLayout(fromView)
        val mock = addMockView(fromView, container)

        safePopTo(targetFragmentTag, fm, flag, willPopFragments)

        var animation: Animation?
        if (popAnim == DEFAULT_POPTO_ANIM) {
            animation = fromSupport.getSupportDelegate().getExitAnim()
            if (animation == null) {
                animation = object : Animation() {}
            }
        } else if (popAnim == 0) {
            animation = object : Animation() {}
        } else {
            animation = AnimationUtils.loadAnimation(activity, popAnim)
        }

        mock.startAnimation(animation)
        handler.postDelayed({
            try {
                mock.clearAnimation()
                mock.removeViewInLayout(fromView)
                container.removeViewInLayout(mock)
            } catch (ignored: Exception) {
            }
        }, animation!!.duration)
    }

    private fun mockStartWithPopAnim(from: ISupportFragment?, to: ISupportFragment?, exitAnim: Animation?) {
        val fromF = from as? Fragment
        val fromView = fromF?.view ?: return
        if (fromView.animation != null) {
            fromView.clearAnimation()
        }

        val container = findContainerById(fromF, from.getSupportDelegate().containerId) ?: return
        container.removeViewInLayout(fromView)
        val mock = addMockView(fromView, container)

        to?.getSupportDelegate()?.enterAnimListener = object : SupportFragmentDelegate.EnterAnimListener {
            override fun onEnterAnimStart() {
                mock.startAnimation(exitAnim)
                handler.postDelayed({
                    try {
                        mock.clearAnimation()
                        mock.removeViewInLayout(fromView)
                        container.removeViewInLayout(mock)
                    } catch (ignored: Exception) {
                    }
                }, exitAnim!!.duration)
            }
        }
    }

    private fun addMockView(fromView: View, container: ViewGroup): ViewGroup {
        val mock = object : ViewGroup(activity) {
            override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
        }

        mock.addView(fromView)
        container.addView(mock)
        return mock
    }

    private fun findContainerById(fragment: Fragment, containerId: Int): ViewGroup? {
        if (fragment.view == null) {
            return null
        }

        val parentFragment = fragment.parentFragment
        val container = if (parentFragment != null) {
            if (parentFragment.view != null) {
                parentFragment.view!!.findViewById(containerId)
            } else {
                findContainerById(parentFragment, containerId)
            }
        } else {
            activity.findViewById(containerId)
        }

        return if (container is ViewGroup) {
            container
        } else null
    }

    private fun handleAfterSaveInStateTransactionException(fm: FragmentManager?, action: String) {
        val stateSaved = FragmentationMagician.isStateSaved(fm)
        if (stateSaved) {
            val e = AfterSaveStateTransactionWarning(action)
            if (Fragmentation.getDefault().getExceptionHandler() != null) {
                Fragmentation.getDefault().getExceptionHandler()!!.onException(e)
            }
        }
    }

    companion object {
        internal const val DEFAULT_POPTO_ANIM = Integer.MAX_VALUE
        internal const val FRAGMENTATION_ARG_RESULT_RECORD = "fragment_arg_result_record"
        internal const val FRAGMENTATION_ARG_ROOT_STATUS = "fragmentation_arg_root_status"
        internal const val FRAGMENTATION_ARG_IS_SHARED_ELEMENT = "fragmentation_arg_is_shared_element"
        internal const val FRAGMENTATION_ARG_CONTAINER = "fragmentation_arg_container"
        internal const val FRAGMENTATION_ARG_REPLACE = "fragmentation_arg_replace"
        internal const val FRAGMENTATION_ARG_CUSTOM_ENTER_ANIM = "fragmentation_arg_custom_enter_anim"
        internal const val FRAGMENTATION_ARG_CUSTOM_EXIT_ANIM = "fragmentation_arg_custom_exit_anim"
        internal const val FRAGMENTATION_ARG_CUSTOM_POP_EXIT_ANIM = "fragmentation_arg_custom_pop_exit_anim"
        internal const val FRAGMENTATION_STATE_SAVE_ANIMATOR = "fragmentation_state_save_animator"
        internal const val FRAGMENTATION_STATE_SAVE_IS_HIDDEN = "fragmentation_state_save_status"
        internal const val TYPE_ADD = 0
        internal const val TYPE_ADD_RESULT = 1
        internal const val TYPE_ADD_WITHOUT_HIDE = 2
        internal const val TYPE_ADD_RESULT_WITHOUT_HIDE = 3
        internal const val TYPE_REPLACE = 10
        internal const val TYPE_REPLACE_DONT_BACK = 11
        private const val TAG = "Fragmentation"
        private const val FRAGMENTATION_STATE_SAVE_RESULT = "fragmentation_state_save_result"
    }
}
