package me.yokeyword.fragmentation.debug;

import java.util.List;

/**
 * 为了调试时 查看栈视图
 * Created by YoKeyword on 16/2/21.
 */
@SuppressWarnings("WeakerAccess")
public class DebugFragmentRecord {
    public CharSequence mFragmentName;
    public List<DebugFragmentRecord> mChildFragmentRecord;

    public DebugFragmentRecord(CharSequence fragmentName, List<DebugFragmentRecord> childFragmentRecord) {
        this.mFragmentName = fragmentName;
        this.mChildFragmentRecord = childFragmentRecord;
    }
}
