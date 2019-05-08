package me.yokeyword.sample.demo_wechat.base

import android.widget.Toast

import me.yokeyword.fragmentation.SupportFragment
import me.yokeyword.sample.R

/**
 * 懒加载
 * Created by YoKeyword on 16/6/5.
 */
abstract class BaseMainFragment : SupportFragment() {
    private var mTouchTime: Long = 0

    /**
     * 处理回退事件
     */
    override fun onBackPressedSupport(): Boolean {
        if (System.currentTimeMillis() - mTouchTime < WAIT_TIME) {
            context.finish()
        } else {
            mTouchTime = System.currentTimeMillis()
            Toast.makeText(context, R.string.press_again_exit, Toast.LENGTH_SHORT).show()
        }
        return true
    }

    companion object {
        // 再点一次退出程序时间设置
        private const val WAIT_TIME = 2000L
    }
}
