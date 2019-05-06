package me.yokeyword.fragmentation.helper.internal

import android.view.View

import java.util.ArrayList

/**
 * @hide Created by YoKey on 16/11/25.
 */
class TransactionRecord {
    var mTag: String? = null
    var mTargetFragmentEnter = Integer.MIN_VALUE
    var mCurrentFragmentPopExit = Integer.MIN_VALUE
    var mCurrentFragmentPopEnter = Integer.MIN_VALUE
    var mTargetFragmentExit = Integer.MIN_VALUE
    var mDontAddToBackStack = false
    var mSharedElementList: ArrayList<SharedElement>? = null

    class SharedElement(var mSharedElement: View, var mSharedName: String)
}
