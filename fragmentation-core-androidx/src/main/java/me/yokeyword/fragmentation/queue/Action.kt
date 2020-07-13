package me.yokeyword.fragmentation.queue

import androidx.fragment.app.FragmentManager

/**
 * Created by YoKey on 17/12/28.
 */
abstract class Action {
    var fragmentManager: FragmentManager? = null
    var action = ACTION_NORMAL
    var duration = 0L

    constructor(fragmentManager: FragmentManager?) {
        this.fragmentManager = fragmentManager
    }

    constructor(action: Int) {
        this.action = action
    }

    constructor(action: Int, fragmentManager: FragmentManager?) {
        this.action = action
        this.fragmentManager = fragmentManager
    }

    abstract fun run()

    companion object {
        const val DEFAULT_POP_TIME = 300L
        const val ACTION_NORMAL = 0
        const val ACTION_POP = 1
        const val ACTION_POP_MOCK = 2
        const val ACTION_BACK = 3
        const val ACTION_LOAD = 4
    }
}
