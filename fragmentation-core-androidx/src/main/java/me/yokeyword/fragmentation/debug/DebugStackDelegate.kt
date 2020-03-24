package me.yokeyword.fragmentation.debug

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentationMagician
import me.yokeyword.fragmentation.Fragmentation
import me.yokeyword.fragmentation.ISupportFragment
import me.yokeyword.fragmentation.R
import java.util.*
import kotlin.math.abs


/**
 * Created by YoKey on 17/6/13.
 */

class DebugStackDelegate(private val mActivity: FragmentActivity) : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var stackDialog: AlertDialog? = null

    fun onCreate(mode: Int) {
        if (mode != Fragmentation.SHAKE) {
            return
        }
        sensorManager = mActivity.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        sensorManager?.registerListener(this,
                sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun onPostCreate(mode: Int) {
        if (mode != Fragmentation.BUBBLE) {
            return
        }
        val root = mActivity.findViewById<View>(android.R.id.content)
        if (root is FrameLayout) {
            val stackView = AppCompatImageView(mActivity)
            stackView.setImageResource(R.drawable.fragmentation_ic_stack)
            val params = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.END
            val dp18 = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 18f, mActivity.resources.displayMetrics).toInt()
            params.topMargin = dp18 * 7
            params.rightMargin = dp18
            stackView.layoutParams = params
            root.addView(stackView)
            stackView.setOnTouchListener(StackViewTouchListener(stackView, dp18 / 4))
            stackView.setOnClickListener { showFragmentStackHierarchyView() }
        }
    }

    fun onDestroy() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorType = event.sensor.type
        val values = event.values
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            val value = 12
            if (abs(values[0]) >= value || abs(values[1]) >= value || abs(values[2]) >= value) {
                showFragmentStackHierarchyView()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    /**
     * 调试相关:以dialog形式 显示 栈视图
     */
    fun showFragmentStackHierarchyView() {
        if (stackDialog?.isShowing == true) {
            return
        }
        val container = DebugHierarchyViewContainer(mActivity)
        container.bindFragmentRecords(getFragmentRecords())
        val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        container.layoutParams = params
        stackDialog = AlertDialog.Builder(mActivity)
                .setView(container)
                .setPositiveButton(android.R.string.cancel, null)
                .setCancelable(true)
                .create()
        stackDialog!!.show()
    }

    /**
     * 调试相关:以log形式 打印 栈视图
     */
    fun logFragmentRecords(tag: String) {
        val fragmentRecordList = getFragmentRecords() ?: return

        val builder = StringBuilder()
        for (i in fragmentRecordList.indices.reversed()) {
            val fragmentRecord = fragmentRecordList[i]

            if (i == fragmentRecordList.size - 1) {
                builder.append("═══════════════════════════════════════════════════════════════════════════════════\n")
                if (i == 0) {
                    builder.append("\t栈顶\t\t\t").append(fragmentRecord.fragmentName).append("\n")
                    builder.append("═══════════════════════════════════════════════════════════════════════════════════")
                } else {
                    builder.append("\t栈顶\t\t\t").append(fragmentRecord.fragmentName).append("\n\n")
                }
            } else if (i == 0) {
                builder.append("\t栈底\t\t\t").append(fragmentRecord.fragmentName).append("\n\n")
                processChildLog(fragmentRecord.childFragmentRecord, builder, 1)
                builder.append("═══════════════════════════════════════════════════════════════════════════════════")
                Log.i(tag, builder.toString())
                return
            } else {
                builder.append("\t↓\t\t\t").append(fragmentRecord.fragmentName).append("\n\n")
            }

            processChildLog(fragmentRecord.childFragmentRecord, builder, 1)
        }
    }

    private fun getFragmentRecords(): List<DebugFragmentRecord>? {
        val fragmentList = FragmentationMagician.getAddedFragments(mActivity.supportFragmentManager)
        if (fragmentList == null || fragmentList.isEmpty()) {
            return null
        }

        val fragmentRecordList = ArrayList<DebugFragmentRecord>()
        for (fragment in fragmentList) {
            addDebugFragmentRecord(fragmentRecordList, fragment)
        }
        return fragmentRecordList
    }

    private fun processChildLog(fragmentRecordList: List<DebugFragmentRecord>?, builder: StringBuilder, childHierarchy: Int) {
        var childHierarchyTemp = childHierarchy
        if (fragmentRecordList == null || fragmentRecordList.isEmpty()) {
            return
        }

        for (j in fragmentRecordList.indices) {
            val childFragmentRecord = fragmentRecordList[j]
            for (k in 0 until childHierarchyTemp) {
                builder.append("\t\t\t")
            }
            when (j) {
                0 -> builder.append("\t子栈顶\t\t").append(childFragmentRecord.fragmentName).append("\n\n")
                fragmentRecordList.size - 1 -> {
                    builder.append("\t子栈底\t\t").append(childFragmentRecord.fragmentName).append("\n\n")
                    processChildLog(childFragmentRecord.childFragmentRecord, builder, ++childHierarchyTemp)
                    return
                }
                else -> builder.append("\t↓\t\t\t").append(childFragmentRecord.fragmentName).append("\n\n")
            }

            processChildLog(childFragmentRecord.childFragmentRecord, builder, childHierarchyTemp)
        }
    }

    private fun getChildFragmentRecords(parentFragment: Fragment): List<DebugFragmentRecord>? {
        val fragmentRecords = ArrayList<DebugFragmentRecord>()
        val fragmentList = FragmentationMagician.getAddedFragments(parentFragment.childFragmentManager)
        if (fragmentList == null || fragmentList.isEmpty()) {
            return null
        }

        for (i in fragmentList.indices.reversed()) {
            val fragment = fragmentList[i]
            addDebugFragmentRecord(fragmentRecords, fragment)
        }
        return fragmentRecords
    }

    private fun addDebugFragmentRecord(fragmentRecords: MutableList<DebugFragmentRecord>, fragment: Fragment?) {
        if (fragment != null) {
            var backStackCount = 0
            if (fragment.fragmentManager != null) {
                backStackCount = fragment.fragmentManager!!.backStackEntryCount
            }
            var name: CharSequence = fragment.javaClass.simpleName
            if (backStackCount == 0) {
                name = span(name, " *")
            } else {
                for (j in 0 until backStackCount) {
                    val entry = fragment.fragmentManager!!.getBackStackEntryAt(j)
                    if (entry.name != null && entry.name == fragment.tag || entry.name == null && fragment.tag == null) {
                        break
                    }
                    if (j == backStackCount - 1) {
                        name = span(name, " *")
                    }
                }
            }

            if ((fragment as? ISupportFragment)?.isSupportVisible() == true) {
                name = span(name, " ☀")
            }
            fragmentRecords.add(DebugFragmentRecord(name, getChildFragmentRecords(fragment)))
        }
    }

    private fun span(name: CharSequence, string: String): CharSequence {
        var nameTemp = name
        nameTemp = nameTemp.toString() + string
        return nameTemp
    }

    private inner class StackViewTouchListener internal constructor(
            private val stackView: View, private val clickLimitValue: Int) : View.OnTouchListener {
        private var dx: Float = 0.toFloat()
        private var dy = 0f
        private var downX: Float = 0.toFloat()
        private var downY = 0f
        private var isClickState: Boolean = false

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val x = event.rawX
            val y = event.rawY
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isClickState = true
                    downX = x
                    downY = y
                    dx = stackView.x - event.rawX
                    dy = stackView.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> if (abs(x - downX) < clickLimitValue
                        && abs(y - downY) < clickLimitValue && isClickState) {
                    isClickState = true
                } else {
                    isClickState = false
                    stackView.x = event.rawX + dx
                    stackView.y = event.rawY + dy
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> if (x - downX < clickLimitValue && isClickState) {
                    stackView.performClick()
                }
                else -> return false
            }
            return true
        }
    }
}
