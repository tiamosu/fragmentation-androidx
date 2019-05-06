package me.yokeyword.fragmentation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import me.yokeyword.fragmentation.helper.ExceptionHandler;

/**
 * Created by YoKey on 17/2/5.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Fragmentation {
    /**
     * Dont display stack view.
     */
    public static final int NONE = 0;
    /**
     * Shake it to display stack view.
     */
    public static final int SHAKE = 1;
    /**
     * As a bubble display stack view.
     */
    public static final int BUBBLE = 2;

    static volatile Fragmentation INSTANCE;

    private boolean mDebug;
    private int mMode;
    private ExceptionHandler mExceptionHandler;

    Fragmentation(FragmentationBuilder builder) {
        mDebug = builder.debug;
        if (mDebug) {
            mMode = builder.mode;
        } else {
            mMode = NONE;
        }
        mExceptionHandler = builder.handler;
    }

    public static Fragmentation getDefault() {
        if (INSTANCE == null) {
            synchronized (Fragmentation.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Fragmentation(new FragmentationBuilder());
                }
            }
        }
        return INSTANCE;
    }

    public static FragmentationBuilder builder() {
        return new FragmentationBuilder();
    }

    public boolean isDebug() {
        return mDebug;
    }

    public void setDebug(boolean debug) {
        this.mDebug = debug;
    }

    public ExceptionHandler getExceptionHandler() {
        return mExceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.mExceptionHandler = exceptionHandler;
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(@StackViewMode int mode) {
        this.mMode = mode;
    }

    @IntDef({NONE, SHAKE, BUBBLE})
    @Retention(RetentionPolicy.SOURCE)
    @interface StackViewMode {
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class FragmentationBuilder {
        private boolean debug;
        private int mode;
        private ExceptionHandler handler;

        /**
         * @param debug Suppressed Exception("Can not perform this action after onSaveInstanceState!") when debug=false
         */
        public FragmentationBuilder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        /**
         * Sets the mode to display the stack view
         * <p>
         * None if debug(false).
         * <p>
         * Default:NONE
         */
        public FragmentationBuilder stackViewMode(@StackViewMode int mode) {
            this.mode = mode;
            return this;
        }

        /**
         * @param handler Handled Exception("Can not perform this action after onSaveInstanceState!") when debug=false.
         */
        public FragmentationBuilder handleException(ExceptionHandler handler) {
            this.handler = handler;
            return this;
        }

        public Fragmentation install() {
            synchronized (Fragmentation.class) {
                if (Fragmentation.INSTANCE != null) {
                    throw new RuntimeException("Default instance already exists." +
                            " It may be only set once before it's used the first time to ensure consistent behavior.");
                }
                Fragmentation.INSTANCE = new Fragmentation(this);
                return Fragmentation.INSTANCE;
            }
        }
    }
}
