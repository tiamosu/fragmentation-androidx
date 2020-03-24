package me.yokeyword.fragmentation.helper.internal

import android.view.View

import java.util.ArrayList

/**
 * @hide Created by YoKey on 16/11/25.
 */
class TransactionRecord {
    var tag: String? = null
    var targetFragmentEnter = Integer.MIN_VALUE
    var currentFragmentPopExit = Integer.MIN_VALUE
    var currentFragmentPopEnter = Integer.MIN_VALUE
    var targetFragmentExit = Integer.MIN_VALUE
    var dontAddToBackStack = false
    var sharedElementList: ArrayList<SharedElement>? = null

    class SharedElement(var sharedElement: View, var sharedName: String)
}
