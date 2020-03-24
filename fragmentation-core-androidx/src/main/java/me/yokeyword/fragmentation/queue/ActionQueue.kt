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
class ActionQueue(private val mainHandler: Handler) {
    private val queue = LinkedList<Action>()

    fun enqueue(action: Action) {
        if (isThrottleBACK(action)) return

        if (action.action == Action.ACTION_LOAD && queue.isEmpty()
                && Thread.currentThread() == Looper.getMainLooper().thread) {
            action.run()
            return
        }

        mainHandler.post { enqueueAction(action) }
    }

    private fun enqueueAction(action: Action) {
        queue.add(action)
        if (queue.size == 1) {
            handleAction()
        }
    }

    private fun handleAction() {
        if (queue.isEmpty()) return

        val action = queue.peek()
        action?.apply {
            run()
            executeNextAction(this)
        }
    }

    private fun executeNextAction(action: Action) {
        if (action.action == Action.ACTION_POP) {
            val top = SupportHelper.getBackStackTopFragment(action.fragmentManager)
            action.duration = top?.getSupportDelegate()?.getExitAnimDuration()
                    ?: Action.DEFAULT_POP_TIME
        }

        mainHandler.postDelayed({
            queue.poll()
            handleAction()
        }, action.duration)
    }

    private fun isThrottleBACK(action: Action): Boolean {
        if (action.action == Action.ACTION_BACK) {
            val head = queue.peek()
            return head?.action == Action.ACTION_POP
        }
        return false
    }
}
