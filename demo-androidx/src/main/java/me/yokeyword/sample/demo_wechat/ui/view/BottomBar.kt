package me.yokeyword.sample.demo_wechat.ui.view

import android.content.Context
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import java.util.*

/**
 * Created by YoKeyword on 16/6/3.
 */
class BottomBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                          defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private val mInterpolator = AccelerateDecelerateInterpolator()
    private var mVisible = true

    private val mTabs = ArrayList<BottomBarTab>()

    private var mTabLayout: LinearLayout? = null

    private var mTabParams: LayoutParams? = null
    private var mCurrentPosition = 0
    private var mListener: OnTabSelectedListener? = null

    init {
        init(context)
    }

    private fun init(context: Context) {
        orientation = VERTICAL

        //        ImageView shadowView = new ImageView(context);
        //        shadowView.setBackgroundResource(R.drawable.actionbar_shadow_up);
        //        addView(shadowView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mTabLayout = LinearLayout(context)
        mTabLayout!!.setBackgroundColor(Color.WHITE)
        mTabLayout!!.orientation = HORIZONTAL
        addView(mTabLayout, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        mTabParams = LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        mTabParams!!.weight = 1f
    }

    fun addItem(tab: BottomBarTab): BottomBar {
        tab.setOnClickListener {
            if (mListener == null) {
                return@setOnClickListener
            }
            val pos = tab.getTabPosition()
            if (mCurrentPosition == pos) {
                mListener!!.onTabReselected(pos)
            } else {
                mListener!!.onTabSelected(pos, mCurrentPosition)
                tab.isSelected = true
                mListener!!.onTabUnselected(mCurrentPosition)
                mTabs[mCurrentPosition].isSelected = false
                mCurrentPosition = pos
            }
        }
        tab.setTabPosition(mTabLayout!!.childCount)
        tab.layoutParams = mTabParams
        mTabLayout!!.addView(tab)
        mTabs.add(tab)
        return this
    }

    fun setOnTabSelectedListener(onTabSelectedListener: OnTabSelectedListener) {
        mListener = onTabSelectedListener
    }

    fun setCurrentItem(position: Int) {
        mTabLayout!!.post { mTabLayout!!.getChildAt(position).performClick() }
    }

    fun getCurrentItemPosition(): Int {
        return mCurrentPosition
    }

    /**
     * 获取 Tab
     */
    fun getItem(index: Int): BottomBarTab? {
        return if (mTabs.size < index) {
            null
        } else mTabs[index]
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, mCurrentPosition)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)

        if (mCurrentPosition != ss.position) {
            mTabLayout!!.getChildAt(mCurrentPosition).isSelected = false
            mTabLayout!!.getChildAt(ss.position).isSelected = true
        }
        mCurrentPosition = ss.position
    }

    fun hide() {
        hide(true)
    }

    fun show() {
        show(true)
    }

    fun hide(anim: Boolean) {
        toggle(false, anim, false)
    }

    fun show(anim: Boolean) {
        toggle(true, anim, false)
    }

    fun isVisible(): Boolean {
        return mVisible
    }

    private fun toggle(visible: Boolean, animate: Boolean, force: Boolean) {
        if (mVisible != visible || force) {
            mVisible = visible
            val height = height
            if (height == 0 && !force) {
                val vto = viewTreeObserver
                if (vto.isAlive) {
                    // view树完成测量并且分配空间而绘制过程还没有开始的时候播放动画。
                    vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            val currentVto = viewTreeObserver
                            if (currentVto.isAlive) {
                                currentVto.removeOnPreDrawListener(this)
                            }
                            toggle(visible, animate, true)
                            return true
                        }
                    })
                    return
                }
            }
            val translationY = if (visible) 0 else height
            if (animate) {
                animate().setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS.toLong())
                        .translationY(translationY.toFloat())
            } else {
                setTranslationY(translationY.toFloat())
            }
        }
    }

    interface OnTabSelectedListener {
        fun onTabSelected(position: Int, prePosition: Int)

        fun onTabUnselected(position: Int)

        fun onTabReselected(position: Int)
    }

    internal class SavedState : BaseSavedState {
        var position: Int = 0

        constructor(source: Parcel) : super(source) {
            position = source.readInt()
        }

        constructor(superState: Parcelable?, position: Int) : super(superState) {
            this.position = position
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(position)
        }

        companion object {

            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        private const val TRANSLATE_DURATION_MILLIS = 200
    }
}
