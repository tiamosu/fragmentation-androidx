package me.yokeyword.fragmentation.queue

import androidx.fragment.app.FragmentManager

/**
 * Created by YoKey on 17/12/28.
 */
abstract class Action {
    var mFragmentManager: FragmentManager? = null
    var mAction = ACTION_NORMAL
    var mDuration: Long = 0

    constructor()

    constructor(action: Int) {
        this.mAction = action
    }

    constructor(action: Int, fragmentManager: FragmentManager?) : this(action) {
        this.mFragmentManager = fragmentManager
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
