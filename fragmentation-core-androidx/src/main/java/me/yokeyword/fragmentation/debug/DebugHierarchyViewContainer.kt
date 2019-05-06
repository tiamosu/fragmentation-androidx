package me.yokeyword.fragmentation.debug

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.NestedScrollView
import me.yokeyword.fragmentation.R

/**
 * Created by YoKeyword on 16/2/21.
 */
class DebugHierarchyViewContainer @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.scrollViewStyle) : NestedScrollView(context, attrs, defStyleAttr) {

    private var mContext: Context? = null
    private var mLinearLayout: LinearLayoutCompat? = null
    private var mTitleLayout: LinearLayoutCompat? = null
    private var mItemHeight: Int = 0
    private var mPadding: Int = 0

    init {
        initView(context)
    }

    private fun initView(context: Context) {
        mContext = context
        val hScrollView = HorizontalScrollView(context)
        mLinearLayout = LinearLayoutCompat(context)
        mLinearLayout!!.orientation = LinearLayoutCompat.VERTICAL
        hScrollView.addView(mLinearLayout)
        addView(hScrollView)

        mItemHeight = dip2px(50f)
        mPadding = dip2px(16f)
    }

    private fun dip2px(dp: Float): Int {
        val scale = mContext!!.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun bindFragmentRecords(fragmentRecords: List<DebugFragmentRecord>?) {
        mLinearLayout!!.removeAllViews()
        val ll = getTitleLayout()
        mLinearLayout!!.addView(ll)

        if (fragmentRecords == null) {
            return
        }
        this@DebugHierarchyViewContainer.setView(fragmentRecords, 0, null)
    }

    @SuppressLint("SetTextI18n")
    private fun getTitleLayout(): LinearLayoutCompat {
        if (mTitleLayout != null) {
            return mTitleLayout!!
        }

        mTitleLayout = LinearLayoutCompat(mContext!!)
        mTitleLayout!!.setPadding(dip2px(24f), dip2px(24f), 0, dip2px(8f))
        mTitleLayout!!.orientation = LinearLayoutCompat.HORIZONTAL
        val flParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mTitleLayout!!.layoutParams = flParams

        val title = AppCompatTextView(mContext!!)
        title.text = "栈视图(Stack)"
        title.textSize = 20f
        title.setTextColor(Color.BLACK)
        val p = LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        p.gravity = Gravity.CENTER_VERTICAL
        title.layoutParams = p
        mTitleLayout!!.addView(title)

        val img = AppCompatImageView(mContext!!)
        img.setImageResource(R.drawable.fragmentation_help)
        val params = LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = dip2px(16f)
        params.gravity = Gravity.CENTER_VERTICAL
        img.layoutParams = params
        mTitleLayout!!.setOnClickListener { Toast.makeText(mContext, "* means not in backBack.", Toast.LENGTH_SHORT).show() }
        mTitleLayout!!.addView(img)
        return mTitleLayout!!
    }

    private fun setView(fragmentRecordList: List<DebugFragmentRecord>,
                        hierarchy: Int, tvItem: AppCompatTextView?) {
        for (i in fragmentRecordList.indices.reversed()) {
            val child = fragmentRecordList[i]
            var tempHierarchy = hierarchy

            val childTvItem: AppCompatTextView
            childTvItem = getTextView(child, tempHierarchy)
            childTvItem.setTag(R.id.hierarchy, tempHierarchy)

            val childFragmentRecord = child.mChildFragmentRecord
            if (childFragmentRecord != null && childFragmentRecord.isNotEmpty()) {
                tempHierarchy++
                childTvItem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.fragmentation_ic_right, 0, 0, 0)
                val finalChilHierarchy = tempHierarchy
                childTvItem.setOnClickListener { v ->
                    if (v.getTag(R.id.isexpand) != null) {
                        val isExpand = v.getTag(R.id.isexpand) as Boolean
                        if (isExpand) {
                            childTvItem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.fragmentation_ic_right, 0, 0, 0)
                            this@DebugHierarchyViewContainer.removeView(finalChilHierarchy)
                        } else {
                            handleExpandView(childFragmentRecord, finalChilHierarchy, childTvItem)
                        }
                        v.setTag(R.id.isexpand, !isExpand)
                    } else {
                        childTvItem.setTag(R.id.isexpand, true)
                        handleExpandView(childFragmentRecord, finalChilHierarchy, childTvItem)
                    }
                }
            } else {
                childTvItem.setPadding(childTvItem.paddingLeft + mPadding, 0, mPadding, 0)
            }

            if (tvItem == null) {
                mLinearLayout!!.addView(childTvItem)
            } else {
                mLinearLayout!!.addView(childTvItem, mLinearLayout!!.indexOfChild(tvItem) + 1)
            }
        }
    }

    private fun handleExpandView(childFragmentRecord: List<DebugFragmentRecord>,
                                 finalChilHierarchy: Int, childTvItem: AppCompatTextView) {
        this@DebugHierarchyViewContainer.setView(childFragmentRecord, finalChilHierarchy, childTvItem)
        childTvItem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.fragmentation_ic_expandable, 0, 0, 0)
    }

    private fun removeView(hierarchy: Int) {
        val size = mLinearLayout!!.childCount
        for (i in size - 1 downTo 0) {
            val view = mLinearLayout!!.getChildAt(i)
            if (view.getTag(R.id.hierarchy) != null && view.getTag(R.id.hierarchy) as Int >= hierarchy) {
                mLinearLayout!!.removeView(view)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getTextView(fragmentRecord: DebugFragmentRecord, hierarchy: Int): AppCompatTextView {
        val tvItem = AppCompatTextView(mContext!!)
        val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight)
        tvItem.layoutParams = params
        if (hierarchy == 0) {
            tvItem.setTextColor(Color.parseColor("#333333"))
            tvItem.textSize = 16f
        }
        tvItem.gravity = Gravity.CENTER_VERTICAL
        tvItem.setPadding((mPadding + hierarchy.toDouble() * mPadding.toDouble() * 1.5).toInt(), 0, mPadding, 0)
        tvItem.compoundDrawablePadding = mPadding / 2

        val a = mContext!!.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        tvItem.setBackgroundDrawable(a.getDrawable(0))
        a.recycle()

        tvItem.text = fragmentRecord.mFragmentName
        return tvItem
    }
}
