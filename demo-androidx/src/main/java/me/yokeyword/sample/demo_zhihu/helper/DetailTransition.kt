package me.yokeyword.sample.demo_zhihu.helper

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.util.AttributeSet

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class DetailTransition : TransitionSet {
    constructor() {
        init()
    }

    // 允许资源文件使用
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        ordering = ORDERING_TOGETHER
        addTransition(ChangeBounds()).addTransition(ChangeTransform()).addTransition(ChangeImageTransform())
    }
}