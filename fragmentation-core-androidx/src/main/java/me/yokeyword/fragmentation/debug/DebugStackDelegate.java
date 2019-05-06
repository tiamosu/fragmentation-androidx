package me.yokeyword.fragmentation.debug;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentationMagician;
import me.yokeyword.fragmentation.Fragmentation;
import me.yokeyword.fragmentation.R;

/**
 * Created by YoKey on 17/6/13.
 */

public class DebugStackDelegate implements SensorEventListener {
    private FragmentActivity mActivity;
    private SensorManager mSensorManager;
    private AlertDialog mStackDialog;

    public DebugStackDelegate(FragmentActivity activity) {
        this.mActivity = activity;
    }

    public void onCreate(int mode) {
        if (mode != Fragmentation.SHAKE) {
            return;
        }
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void onPostCreate(int mode) {
        if (mode != Fragmentation.BUBBLE) {
            return;
        }
        final View root = mActivity.findViewById(android.R.id.content);
        if (root instanceof FrameLayout) {
            final FrameLayout content = (FrameLayout) root;
            final AppCompatImageView stackView = new AppCompatImageView(mActivity);
            stackView.setImageResource(R.drawable.fragmentation_ic_stack);
            final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.END;
            final int dp18 = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 18, mActivity.getResources().getDisplayMetrics());
            params.topMargin = dp18 * 7;
            params.rightMargin = dp18;
            stackView.setLayoutParams(params);
            content.addView(stackView);
            stackView.setOnTouchListener(new StackViewTouchListener(stackView, dp18 / 4));
            stackView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFragmentStackHierarchyView();
                }
            });
        }
    }

    public void onDestroy() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final int sensorType = event.sensor.getType();
        final float[] values = event.values;
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            int value = 12;
            if ((Math.abs(values[0]) >= value || Math.abs(values[1]) >= value || Math.abs(values[2]) >= value)) {
                showFragmentStackHierarchyView();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * 调试相关:以dialog形式 显示 栈视图
     */
    public void showFragmentStackHierarchyView() {
        if (mStackDialog != null && mStackDialog.isShowing()) {
            return;
        }
        final DebugHierarchyViewContainer container = new DebugHierarchyViewContainer(mActivity);
        container.bindFragmentRecords(getFragmentRecords());
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        container.setLayoutParams(params);
        mStackDialog = new AlertDialog.Builder(mActivity)
                .setView(container)
                .setPositiveButton(android.R.string.cancel, null)
                .setCancelable(true)
                .create();
        mStackDialog.show();
    }

    /**
     * 调试相关:以log形式 打印 栈视图
     */
    public void logFragmentRecords(String tag) {
        final List<DebugFragmentRecord> fragmentRecordList = getFragmentRecords();
        if (fragmentRecordList == null) {
            return;
        }

        final StringBuilder builder = new StringBuilder();
        for (int i = fragmentRecordList.size() - 1; i >= 0; i--) {
            final DebugFragmentRecord fragmentRecord = fragmentRecordList.get(i);

            if (i == fragmentRecordList.size() - 1) {
                builder.append("═══════════════════════════════════════════════════════════════════════════════════\n");
                if (i == 0) {
                    builder.append("\t栈顶\t\t\t").append(fragmentRecord.mFragmentName).append("\n");
                    builder.append("═══════════════════════════════════════════════════════════════════════════════════");
                } else {
                    builder.append("\t栈顶\t\t\t").append(fragmentRecord.mFragmentName).append("\n\n");
                }
            } else if (i == 0) {
                builder.append("\t栈底\t\t\t").append(fragmentRecord.mFragmentName).append("\n\n");
                processChildLog(fragmentRecord.mChildFragmentRecord, builder, 1);
                builder.append("═══════════════════════════════════════════════════════════════════════════════════");
                Log.i(tag, builder.toString());
                return;
            } else {
                builder.append("\t↓\t\t\t").append(fragmentRecord.mFragmentName).append("\n\n");
            }

            processChildLog(fragmentRecord.mChildFragmentRecord, builder, 1);
        }
    }

    private List<DebugFragmentRecord> getFragmentRecords() {
        final List<DebugFragmentRecord> fragmentRecordList = new ArrayList<>();
        final List<Fragment> fragmentList = FragmentationMagician.getActiveFragments(mActivity.getSupportFragmentManager());

        if (fragmentList == null || fragmentList.size() < 1) {
            return null;
        }
        for (Fragment fragment : fragmentList) {
            addDebugFragmentRecord(fragmentRecordList, fragment);
        }
        return fragmentRecordList;
    }

    private void processChildLog(List<DebugFragmentRecord> fragmentRecordList, StringBuilder builder, int childHierarchy) {
        if (fragmentRecordList == null || fragmentRecordList.size() == 0) {
            return;
        }

        for (int j = 0; j < fragmentRecordList.size(); j++) {
            final DebugFragmentRecord childFragmentRecord = fragmentRecordList.get(j);
            for (int k = 0; k < childHierarchy; k++) {
                builder.append("\t\t\t");
            }
            if (j == 0) {
                builder.append("\t子栈顶\t\t").append(childFragmentRecord.mFragmentName).append("\n\n");
            } else if (j == fragmentRecordList.size() - 1) {
                builder.append("\t子栈底\t\t").append(childFragmentRecord.mFragmentName).append("\n\n");
                processChildLog(childFragmentRecord.mChildFragmentRecord, builder, ++childHierarchy);
                return;
            } else {
                builder.append("\t↓\t\t\t").append(childFragmentRecord.mFragmentName).append("\n\n");
            }

            processChildLog(childFragmentRecord.mChildFragmentRecord, builder, childHierarchy);
        }
    }

    private List<DebugFragmentRecord> getChildFragmentRecords(Fragment parentFragment) {
        final List<DebugFragmentRecord> fragmentRecords = new ArrayList<>();
        final List<Fragment> fragmentList = FragmentationMagician.getActiveFragments(parentFragment.getChildFragmentManager());
        if (fragmentList == null || fragmentList.size() < 1) {
            return null;
        }

        for (int i = fragmentList.size() - 1; i >= 0; i--) {
            final Fragment fragment = fragmentList.get(i);
            addDebugFragmentRecord(fragmentRecords, fragment);
        }
        return fragmentRecords;
    }

    private void addDebugFragmentRecord(List<DebugFragmentRecord> fragmentRecords, Fragment fragment) {
        if (fragment != null) {
            int backStackCount = 0;
            if (fragment.getFragmentManager() != null) {
                backStackCount = fragment.getFragmentManager().getBackStackEntryCount();
            }
            CharSequence name = fragment.getClass().getSimpleName();
            if (backStackCount == 0) {
                name = span(name);
            } else {
                for (int j = 0; j < backStackCount; j++) {
                    final FragmentManager.BackStackEntry entry = fragment.getFragmentManager().getBackStackEntryAt(j);
                    if ((entry.getName() != null && entry.getName().equals(fragment.getTag()))
                            || (entry.getName() == null && fragment.getTag() == null)) {
                        break;
                    }
                    if (j == backStackCount - 1) {
                        name = span(name);
                    }
                }
            }
            fragmentRecords.add(new DebugFragmentRecord(name, getChildFragmentRecords(fragment)));
        }
    }

    @NonNull
    private CharSequence span(CharSequence name) {
        name = name + " *";
        return name;
    }

    private class StackViewTouchListener implements View.OnTouchListener {
        private View mStackView;
        private float mDX, mDY = 0f;
        private float mDownX, mDownY = 0f;
        private boolean mIsClickState;
        private int mClickLimitValue;

        StackViewTouchListener(View stackView, int clickLimitValue) {
            this.mStackView = stackView;
            this.mClickLimitValue = clickLimitValue;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getRawX();
            float y = event.getRawY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mIsClickState = true;
                    mDownX = x;
                    mDownY = y;
                    mDX = mStackView.getX() - event.getRawX();
                    mDY = mStackView.getY() - event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(x - mDownX) < mClickLimitValue && Math.abs(y - mDownY) < mClickLimitValue && mIsClickState) {
                        mIsClickState = true;
                    } else {
                        mIsClickState = false;
                        mStackView.setX(event.getRawX() + mDX);
                        mStackView.setY(event.getRawY() + mDY);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (x - mDownX < mClickLimitValue && mIsClickState) {
                        mStackView.performClick();
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }
}
