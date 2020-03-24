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
    private val tag = EventBusActivityScope::class.java.simpleName
    private val activityEventBusScopePool = ConcurrentHashMap<Activity, LazyEventBusInstance>()
    private val initialized = AtomicBoolean(false)

    @Volatile
    private var invalidEventBus: EventBus? = null

    internal fun init(context: Context?) {
        if (initialized.getAndSet(true)) {
            return
        }
        (context?.applicationContext as? Application)
                ?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                    private val mainHandler = Handler(Looper.getMainLooper())

                    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
                        activityEventBusScopePool[activity] = LazyEventBusInstance()
                    }

                    override fun onActivityStarted(activity: Activity) {}

                    override fun onActivityResumed(activity: Activity) {}

                    override fun onActivityPaused(activity: Activity) {}

                    override fun onActivityStopped(activity: Activity) {}

                    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle?) {}

                    override fun onActivityDestroyed(activity: Activity) {
                        if (!activityEventBusScopePool.containsKey(activity)) {
                            return
                        }
                        // Make sure Fragment's onDestroy() has been called.
                        mainHandler.post {
                            activityEventBusScopePool.remove(activity)
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
            Log.e(tag, "Can't find the Activity, the Activity is null!")
            return invalidEventBus()!!
        }

        val lazyEventBusInstance = activityEventBusScopePool[activity]
        if (lazyEventBusInstance == null) {
            Log.e(tag, "Can't find the Activity, it has been removed!")
            return invalidEventBus()!!
        }
        return lazyEventBusInstance.getInstance()!!
    }

    private fun invalidEventBus(): EventBus? {
        if (invalidEventBus == null) {
            synchronized(EventBusActivityScope::class.java) {
                if (invalidEventBus == null) {
                    invalidEventBus = EventBus()
                }
            }
        }
        return invalidEventBus
    }

    internal class LazyEventBusInstance {
        @Volatile
        private var eventBus: EventBus? = null

        fun getInstance(): EventBus? {
            if (eventBus == null) {
                synchronized(this) {
                    if (eventBus == null) {
                        eventBus = EventBus()
                    }
                }
            }
            return eventBus
        }
    }
}
