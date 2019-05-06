package me.yokeyword.eventbusactivityscope

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log

import org.greenrobot.eventbus.EventBus
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Activity-scope EventBus.
 * Created by YoKey on 17/10/17.
 */
object EventBusActivityScope {
    private val TAG = EventBusActivityScope::class.java.simpleName
    private val ACTIVITY_EVENT_BUS_SCOPE_POOL = ConcurrentHashMap<Activity, LazyEventBusInstance>()
    private val sInitialized = AtomicBoolean(false)
    @Volatile
    private var mInvalidEventBus: EventBus? = null

    internal fun init(context: Context?) {
        if (sInitialized.getAndSet(true)) {
            return
        }

        (context?.applicationContext as? Application)
                ?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                    private val mainHandler = Handler(Looper.getMainLooper())

                    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
                        ACTIVITY_EVENT_BUS_SCOPE_POOL[activity] = LazyEventBusInstance()
                    }

                    override fun onActivityStarted(activity: Activity) {}

                    override fun onActivityResumed(activity: Activity) {}

                    override fun onActivityPaused(activity: Activity) {}

                    override fun onActivityStopped(activity: Activity) {}

                    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle?) {}

                    override fun onActivityDestroyed(activity: Activity) {
                        if (!ACTIVITY_EVENT_BUS_SCOPE_POOL.containsKey(activity)) {
                            return
                        }

                        // Make sure Fragment's onDestroy() has been called.
                        mainHandler.post {
                            ACTIVITY_EVENT_BUS_SCOPE_POOL.remove(activity)
                        }
                    }
                })
    }

    /**
     * Get the activity-scope EventBus instance
     */
    @JvmStatic
    fun getDefault(activity: Activity?): EventBus {
        if (activity == null) {
            Log.e(TAG, "Can't find the Activity, the Activity is null!")
            return invalidEventBus()!!
        }

        val lazyEventBusInstance = ACTIVITY_EVENT_BUS_SCOPE_POOL[activity]
        if (lazyEventBusInstance == null) {
            Log.e(TAG, "Can't find the Activity, it has been removed!")
            return invalidEventBus()!!
        }
        return lazyEventBusInstance.getInstance()!!
    }

    private fun invalidEventBus(): EventBus? {
        if (mInvalidEventBus == null) {
            synchronized(EventBusActivityScope::class.java) {
                if (mInvalidEventBus == null) {
                    mInvalidEventBus = EventBus()
                }
            }
        }
        return mInvalidEventBus
    }

    internal class LazyEventBusInstance {
        @Volatile
        private var mEventBus: EventBus? = null

        fun getInstance(): EventBus? {
            if (mEventBus == null) {
                synchronized(this) {
                    if (mEventBus == null) {
                        mEventBus = EventBus()
                    }
                }
            }
            return mEventBus
        }
    }
}
