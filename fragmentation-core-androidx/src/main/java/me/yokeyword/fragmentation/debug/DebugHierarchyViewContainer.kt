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
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.scrollViewStyle)
    : NestedScrollView(context, attrs, defStyleAttr) {

    private lateinit var linearLayout: LinearLayoutCompat
    private var titleLayout: LinearLayoutCompat? = null
    private var itemHeight = 0
    private var padding = 0

    init {
        initView()
    }

    private fun initView() {
        val hScrollView = HorizontalScrollView(context)
        linearLayout = LinearLayoutCompat(context).also {
            it.orientation = LinearLayoutCompat.VERTICAL
            hScrollView.addView(it)
        }
        addView(hScrollView)

        itemHeight = dip2px(50f)
        padding = dip2px(16f)
    }

    private fun dip2px(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun bindFragmentRecords(fragmentRecords: List<DebugFragmentRecord>?) {
        linearLayout.removeAllViews()
        val ll = getTitleLayout()
        linearLayout.addView(ll)

        setView(fragmentRecords, 0, null)
    }

    @SuppressLint("SetTextI18n")
    private fun getTitleLayout(): LinearLayoutCompat {
        if (titleLayout != null) return titleLayout!!

        titleLayout = LinearLayoutCompat(context).also {
            it.setPadding(dip2px(24f), dip2px(24f), 0, dip2px(8f))
            it.orientation = LinearLayoutCompat.HORIZONTAL
            val flParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.layoutParams = flParams
        }

        val title = AppCompatTextView(context).also {
            it.text = context.getString(R.string.fragmentation_stack_view)
            it.textSize = 20f
            it.setTextColor(Color.BLACK)
            val p = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            p.gravity = Gravity.CENTER_VERTICAL
            it.layoutParams = p
        }
        titleLayout?.addView(title)

        val img = AppCompatImageView(context).also {
            it.setImageResource(R.drawable.fragmentation_help)
            val params = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.leftMargin = dip2px(16f)
            params.gravity = Gravity.CENTER_VERTICAL
            it.layoutParams = params
        }
        titleLayout?.addView(img)

        titleLayout?.setOnClickListener {
            Toast.makeText(context, R.string.fragmentation_stack_help, Toast.LENGTH_LONG).show()
        }
        return titleLayout!!
    }

    private fun setView(fragmentRecordList: List<DebugFragmentRecord>?,
                        hierarchy: Int,
                        tvItem: AppCompatTextView?) {
        fragmentRecordList ?: return
        for (i in fragmentRecordList.indices.reversed()) {
            val child = fragmentRecordList[i]
            var tempHierarchy = hierarchy

            val childTvItem = getTextView(child, tempHierarchy)
            childTvItem.setTag(R.id.hierarchy, tempHierarchy)

            val childFragmentRecord = child.childFragmentRecord
            if (childFragmentRecord?.isNotEmpty() == true) {
                tempHierarchy++
                childTvItem.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.fragmentation_ic_right, 0, 0, 0)

                val finalChilHierarchy = tempHierarchy
                childTvItem.setOnClickListener { v ->
                    if (v.getTag(R.id.isexpand) != null) {
                        val isExpand = v.getTag(R.id.isexpand) as? Boolean
                        if (isExpand == true) {
                            childTvItem.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.fragmentation_ic_right, 0, 0, 0)
                            removeView(finalChilHierarchy)
                        } else {
                            handleExpandView(childFragmentRecord, finalChilHierarchy, childTvItem)
                        }
                        isExpand?.let { v.setTag(R.id.isexpand, !it) }
                    } else {
                        childTvItem.setTag(R.id.isexpand, true)
                        handleExpandView(childFragmentRecord, finalChilHierarchy, childTvItem)
                    }
                }
            } else {
                childTvItem.setPadding(childTvItem.paddingLeft + padding, 0, padding, 0)
            }

            if (tvItem == null) {
                linearLayout.addView(childTvItem)
            } else {
                linearLayout.addView(childTvItem, linearLayout.indexOfChild(tvItem) + 1)
            }
        }
    }

    private fun handleExpandView(childFragmentRecord: List<DebugFragmentRecord>,
                                 finalChilHierarchy: Int,
                                 childTvItem: AppCompatTextView) {
        setView(childFragmentRecord, finalChilHierarchy, childTvItem)
        childTvItem.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.fragmentation_ic_expandable, 0, 0, 0)
    }

    private fun removeView(hierarchy: Int) {
        val size = linearLayout.childCount
        for (i in size - 1 downTo 0) {
            val view = linearLayout.getChildAt(i)
            val hierarchyTag = view.getTag(R.id.hierarchy)
            if ((hierarchyTag as? Int ?: -1) >= hierarchy) {
                linearLayout.removeView(view)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getTextView(fragmentRecord: DebugFragmentRecord, hierarchy: Int): AppCompatTextView {
        val tvItem = AppCompatTextView(context)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight)
        tvItem.layoutParams = params

        if (hierarchy == 0) {
            tvItem.setTextColor(Color.parseColor("#333333"))
            tvItem.textSize = 16f
        }
        tvItem.gravity = Gravity.CENTER_VERTICAL
        tvItem.setPadding((padding + hierarchy.toDouble() * padding.toDouble() * 1.5).toInt(), 0, padding, 0)
        tvItem.compoundDrawablePadding = padding / 2

        val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        tvItem.setBackgroundDrawable(a.getDrawable(0))
        a.recycle()

        tvItem.text = fragmentRecord.fragmentName
        return tvItem
    }
}
