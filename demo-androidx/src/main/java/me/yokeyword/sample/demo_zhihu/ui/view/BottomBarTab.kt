package me.yokeyword.sample.demo_zhihu.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import me.yokeyword.sample.R

/**
 * Created by YoKeyword on 16/6/3.
 */
@SuppressLint("ViewConstructor")
class BottomBarTab @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                             defStyleAttr: Int = 0, icon: Int) : FrameLayout(context, attrs, defStyleAttr) {
    private var mIcon: ImageView? = null
    private var mContext: Context? = null
    private var mTabPosition = -1

    init {
        init(context, icon)
    }

    private fun init(context: Context, icon: Int) {
        mContext = context
        val typedArray = context.obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackgroundBorderless))
        val drawable = typedArray.getDrawable(0)
        setBackgroundDrawable(drawable)
        typedArray.recycle()

        mIcon = ImageView(context)
        val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 27f, resources.displayMetrics).toInt()
        val params = LayoutParams(size, size)
        params.gravity = Gravity.CENTER
        mIcon!!.setImageResource(icon)
        mIcon!!.layoutParams = params
        mIcon!!.setColorFilter(ContextCompat.getColor(context, R.color.tab_unselect))
        addView(mIcon)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            mIcon!!.setColorFilter(ContextCompat.getColor(mContext!!, R.color.colorPrimary))
        } else {
            mIcon!!.setColorFilter(ContextCompat.getColor(mContext!!, R.color.tab_unselect))
        }
    }

    fun getTabPosition(): Int {
        return mTabPosition
    }

    fun setTabPosition(position: Int) {
        mTabPosition = position
        if (position == 0) {
            isSelected = true
        }
    }
}
