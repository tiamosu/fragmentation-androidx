package me.yokeyword.fragmentation

import androidx.annotation.IntDef
import me.yokeyword.fragmentation.helper.ExceptionHandler

/**
 * Created by YoKey on 17/2/5.
 */
@Suppress("unused")
class Fragmentation internal constructor(builder: FragmentationBuilder) {
    private var isDebug: Boolean = false
    private var mode = 0
    private var exceptionHandler: ExceptionHandler? = null

    init {
        isDebug = builder.debug
        mode = if (isDebug) {
            builder.mode
        } else {
            NONE
        }
        exceptionHandler = builder.handler
    }

    fun isDebug(): Boolean {
        return isDebug
    }

    fun setDebug(debug: Boolean) {
        this.isDebug = debug
    }

    fun getExceptionHandler(): ExceptionHandler? {
        return exceptionHandler
    }

    fun setExceptionHandler(exceptionHandler: ExceptionHandler) {
        this.exceptionHandler = exceptionHandler
    }

    fun getMode(): Int {
        return mode
    }

    fun setMode(@StackViewMode mode: Int) {
        this.mode = mode
    }

    @IntDef(NONE, SHAKE, BUBBLE)
    @Retention(AnnotationRetention.SOURCE)
    internal annotation class StackViewMode

    class FragmentationBuilder {
        var debug: Boolean = false
        var mode: Int = 0
        var handler: ExceptionHandler? = null

        /**
         * @param debug Suppressed Exception("Can not perform this action after onSaveInstanceState!") when debug=false
         */
        fun debug(debug: Boolean): FragmentationBuilder {
            this.debug = debug
            return this
        }

        /**
         * Sets the mode to display the stack view
         *
         * None if debug(false).
         *
         * Default:NONE
         */
        fun stackViewMode(@StackViewMode mode: Int): FragmentationBuilder {
            this.mode = mode
            return this
        }

        /**
         * @param handler Handled Exception("Can not perform this action after onSaveInstanceState!") when debug=false.
         */
        fun handleException(handler: ExceptionHandler?): FragmentationBuilder {
            this.handler = handler
            return this
        }

        fun install(): Fragmentation? {
            INSTANCE = Fragmentation(this)
            return INSTANCE
        }
    }

    companion object {
        /**
         * Dont display stack view.
         */
        const val NONE = 0

        /**
         * Shake it to display stack view.
         */
        const val SHAKE = 1

        /**
         * As a bubble display stack view.
         */
        const val BUBBLE = 2

        @Volatile
        internal var INSTANCE: Fragmentation? = null

        @JvmStatic
        fun getDefault(): Fragmentation {
            if (INSTANCE == null) {
                synchronized(Fragmentation::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Fragmentation(FragmentationBuilder())
                    }
                }
            }
            return INSTANCE!!
        }

        @JvmStatic
        fun builder(): FragmentationBuilder {
            return FragmentationBuilder()
        }
    }
}
