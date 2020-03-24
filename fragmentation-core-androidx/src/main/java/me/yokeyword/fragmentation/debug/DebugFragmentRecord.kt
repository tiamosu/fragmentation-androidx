package me.yokeyword.fragmentation.debug

/**
 * 为了调试时 查看栈视图
 * Created by YoKeyword on 16/2/21.
 */
class DebugFragmentRecord(var fragmentName: CharSequence, var childFragmentRecord: List<DebugFragmentRecord>?)
