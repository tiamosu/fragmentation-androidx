package me.yokeyword.fragmentation.queue

import android.os.Handler
import android.os.Looper
import me.yokeyword.fragmentation.SupportHelper
import java.util.*

/**
 * The queue of perform action.
 *
 *
 * Created by YoKey on 17/12/29.
 */
class ActionQueue(private val mMainHandler: Handler) {
    private val mQueue = LinkedList<Action>()

    fun enqueue(action: Action) {
        if (isThrottleBACK(action)) {
            return
        }
        if (action.mAction == Action.ACTION_LOAD && mQueue.isEmpty()
                && Thread.currentThread() === Looper.getMainLooper().thread) {
            action.run()
            return
        }

        mMainHandler.post { enqueueAction(action) }
    }

    private fun enqueueAction(action: Action) {
        mQueue.add(action)
        if (mQueue.size == 1) {
            handleAction()
        }
    }

    private fun handleAction() {
        if (mQueue.isEmpty()) {
            return
        }

        val action = mQueue.peek()
        action.run()

        executeNextAction(action)
    }

    private fun executeNextAction(action: Action) {
        if (action.mAction == Action.ACTION_POP) {
            val top = SupportHelper.getBackStackTopFragment(action.mFragmentManager)
            action.mDuration = top?.getSupportDelegate()?.getExitAnimDuration() ?: Action.DEFAULT_POP_TIME
        }

        mMainHandler.postDelayed({
            mQueue.poll()
            handleAction()
        }, action.mDuration)
    }

    private fun isThrottleBACK(action: Action): Boolean {
        if (action.mAction == Action.ACTION_BACK) {
            val head = mQueue.peek()
            return head != null && head.mAction == Action.ACTION_POP
        }
        return false
    }
}
