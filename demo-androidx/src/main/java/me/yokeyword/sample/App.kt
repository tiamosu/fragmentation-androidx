package me.yokeyword.sample

import android.app.Application

import me.yokeyword.fragmentation.Fragmentation
import me.yokeyword.fragmentation.helper.ExceptionHandler

/**
 * Created by YoKey on 16/11/23.
 */
@Suppress("unused")
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Fragmentation.builder()
                // 设置 栈视图 模式为 （默认）悬浮球模式   SHAKE: 摇一摇唤出  NONE：隐藏， 仅在Debug环境生效
                .stackViewMode(Fragmentation.BUBBLE)
                .debug(true) // 实际场景建议.debug(BuildConfig.DEBUG)
                /**
                 * 可以获取到 [me.yokeyword.fragmentation.exception.AfterSaveStateTransactionWarning]
                 * 在遇到After onSaveInstanceState时，不会抛出异常，会回调到下面的ExceptionHandler
                 */
                .handleException(object : ExceptionHandler {
                    override fun onException(e: Exception) {
                    }
                })
                .install()
    }
}
