package com.shong.klog

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.shong.klog.floating.FloatingService
import com.shong.klog.models.FloatingData
import com.shong.klog.models.LogLevel
import com.shong.klog.models.RemoveData
import com.shong.klog.models.RemoveMode
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

object Klog {
    private var isShow = true
    internal const val AUTO_STOP_BASE = true
    internal const val MAX_BASE = 6

    private var SEARCH_POINT = "_sHong"

    private var _logFlow: MutableSharedFlow<FloatingData>? = null
    internal var logFlow: SharedFlow<FloatingData>? = null

    private val _removeKeyFlow: MutableSharedFlow<RemoveData> =
        MutableSharedFlow(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    internal val removeKeyFlow = _removeKeyFlow.asSharedFlow()

    fun initialize(searchPoint: String, isShow: Boolean) {
        this.SEARCH_POINT = searchPoint
        this.isShow = this.isShow || isShow
        Log.d("KLog_sHong", "isShow? ${this.isShow}")
    }

    fun v(any: Any, msg: String) =
        show(any.javaClass.simpleName + SEARCH_POINT, msg, LogLevel.V)

    fun v(any: Any, msg: String, thr: Throwable?) =
        show(any.javaClass.simpleName + SEARCH_POINT, msg, thr, LogLevel.V)

    fun v(tag: String, msg: String) =
        show(tag + SEARCH_POINT, msg, LogLevel.V)

    fun v(tag: String, msg: String, thr: Throwable?) =
        show(tag + SEARCH_POINT, msg, thr, LogLevel.V)

    fun d(any: Any, msg: String) =
        show(any.javaClass.simpleName + SEARCH_POINT, msg, LogLevel.D)

    fun d(any: Any, msg: String, thr: Throwable?) =
        show(any.javaClass.simpleName + SEARCH_POINT, msg, thr, LogLevel.D)

    fun d(tag: String, msg: String) =
        show(tag + SEARCH_POINT, msg, LogLevel.D)

    fun d(tag: String, msg: String, thr: Throwable?) =
        show(tag + SEARCH_POINT, msg, thr, LogLevel.D)

    fun i(any: Any, msg: String) =
        show(any.javaClass.simpleName + SEARCH_POINT, msg, LogLevel.I)

    fun i(any: Any, msg: String, thr: Throwable?) =
        show(any.javaClass.simpleName + SEARCH_POINT, msg, thr, LogLevel.I)

    fun i(tag: String, msg: String) =
        show(tag + SEARCH_POINT, msg, LogLevel.I)

    fun i(tag: String, msg: String, thr: Throwable?) =
        show(tag + SEARCH_POINT, msg, thr, LogLevel.I)

    fun w(any: Any, msg: String) =
        show(any.javaClass.simpleName + SEARCH_POINT, msg, LogLevel.W)

    fun w(any: Any, msg: String, thr: Throwable?) =
        show(any.javaClass.simpleName + SEARCH_POINT, msg, thr, LogLevel.W)

    fun w(tag: String, msg: String) =
        show(tag + SEARCH_POINT, msg, LogLevel.W)

    fun w(tag: String, msg: String, thr: Throwable?) =
        show(tag + SEARCH_POINT, msg, thr, LogLevel.W)

    fun e(any: Any, msg: String) =
        show(any.javaClass.simpleName + SEARCH_POINT, msg, LogLevel.E)

    fun e(any: Any, msg: String, thr: Throwable?) =
        show(any.javaClass.simpleName + SEARCH_POINT, msg, thr, LogLevel.E)

    fun e(tag: String, msg: String) =
        show(tag + SEARCH_POINT, msg, LogLevel.E)

    fun e(tag: String, msg: String, thr: Throwable?) =
        show(tag + SEARCH_POINT, msg, thr, LogLevel.E)

    fun f(any: Any, msg: String) =
        showWithFloat(any.javaClass.simpleName, msg, timeMillis = System.currentTimeMillis())

    fun f(any: Any, msg: String, thr: Throwable?) =
        showWithFloat(any.javaClass.simpleName, msg + "\n" + thr, timeMillis = System.currentTimeMillis())

    fun f(tag: String, msg: String) =
        showWithFloat(tag, msg, timeMillis = System.currentTimeMillis())

    fun f(tag: String, msg: String, thr: Throwable?) =
        showWithFloat(tag, msg + "\n" + thr, timeMillis = System.currentTimeMillis())

    fun fl(tag: String, msg: String, thr: Throwable?, logLevel: LogLevel = LogLevel.D) =
        showWithFloat(tag, msg + "\n" + thr, logLevel, System.currentTimeMillis())

    fun fl(any: Any, msg: String, logLevel: LogLevel = LogLevel.D) =
        showWithFloat(any.javaClass.simpleName, msg, logLevel, System.currentTimeMillis())

    fun fl(any: Any, msg: String, thr: Throwable?, logLevel: LogLevel = LogLevel.D) =
        showWithFloat(any.javaClass.simpleName, msg + "\n" + thr, logLevel, System.currentTimeMillis())

    fun fl(tag: String, msg: String, logLevel: LogLevel = LogLevel.D) =
        showWithFloat(tag, msg, logLevel, System.currentTimeMillis())

    private fun show(tag: String, msg: String, level: LogLevel) {
        if (isShow) {
            when (level) {
                LogLevel.E -> Log.e(tag, msg)
                LogLevel.W -> Log.w(tag, msg)
                LogLevel.I -> Log.i(tag, msg)
                LogLevel.D -> Log.d(tag, msg)
                LogLevel.V -> Log.v(tag, msg)
                else -> Log.d(tag, msg)
            }
        }
    }

    private fun show(tag: String, msg: String, thr: Throwable?, level: LogLevel) {
        if (isShow) {
            when (level) {
                LogLevel.E -> Log.e(tag, msg, thr)
                LogLevel.W -> Log.w(tag, msg, thr)
                LogLevel.I -> Log.i(tag, msg, thr)
                LogLevel.D -> Log.d(tag, msg, thr)
                LogLevel.V -> Log.v(tag, msg, thr)
                else -> Log.d(tag, msg, thr)
            }
        }
    }

    private fun showWithFloat(key: String, value: String, logLevel: LogLevel = LogLevel.D, timeMillis: Long) {
        if (isShow) {
            show(key + SEARCH_POINT, value, logLevel)
            runBlocking {
                _logFlow?.emit(
                    FloatingData(
                        key = key,
                        msg = value,
                        logLevel = logLevel,
                        timeMillis = timeMillis,
                    )
                )
            }
        }
    }

    fun removeFloatLog(key: String) {
        runBlocking {
            _removeKeyFlow.emit(RemoveData(key, RemoveMode.Key))
        }
    }

    fun removeContainFloatLog(key: String) {
        runBlocking {
            _removeKeyFlow.emit(RemoveData(key, RemoveMode.Contain))
        }
    }

    fun removeAllFloatLog() {
        runBlocking {
            _removeKeyFlow.emit(RemoveData(null, RemoveMode.All))
        }
    }

    fun runFloating(activity: ComponentActivity) {
        runFloating(activity, AUTO_STOP_BASE, MAX_BASE, false, {},{ _ -> })
    }

    fun runFloating(
        activity: ComponentActivity,
        autoStop: Boolean = AUTO_STOP_BASE,
        max: Int = MAX_BASE,
        withActivityLog: Boolean = false,
        onPermissionOk: () -> Unit = {},
        onFailure: (String?) -> Unit = {},
    ) {
        if(isShow){
            try {
                if (Settings.canDrawOverlays(activity)) {
                    startFloating(activity, autoStop, max, withActivityLog)
                    Klog.d(this, "show Floating Succeed")
                    onPermissionOk()
                } else {
                    val resultLauncherOverlay = activity.registerForActivityResult(
                        ActivityResultContracts.StartActivityForResult()
                    ) { _ ->
                        if (Settings.canDrawOverlays(activity)) {
                            startFloating(activity, autoStop, max, withActivityLog)
                            Klog.d(this, "show Floating Succeed")
                            onPermissionOk()
                        } else {
                            Klog.d(this, "not accept permission")
                            onFailure("not accept permission")
                        }
                    }

                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${activity.packageName}")
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                    resultLauncherOverlay.launch(intent)
                }
            } catch (e: Exception) {
                Klog.e(this, "Occur Floating Error :  $e")
                onFailure("Occur Floating Error :  $e")
            }
        }
    }

    fun addBackPressedFloatingClose(activity: ComponentActivity) {
        if(isShow){
            // onBackPressed is >= sdk33 deprecated
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    stopFloating(activity)
                    activity.finish()
                    Klog.d(this, "BackPressed with activity finish, stopFloating")
                }
            }
            activity.onBackPressedDispatcher.addCallback(activity, callback)
        }
    }

    fun stopFloating(activity: ComponentActivity) {
        val intent = Intent(activity, FloatingService::class.java)
        activity.stopService(intent)
        stopActivityLog()
        Klog.d(this, "stopFloating")
    }

    private fun startFloating(activity: ComponentActivity, autoStop: Boolean, max: Int, withActivityLog: Boolean) {
        _logFlow = MutableSharedFlow(
                replay = max,
                extraBufferCapacity = max,
                onBufferOverflow = BufferOverflow.DROP_OLDEST)
        logFlow = _logFlow!!.asSharedFlow()

        val intent = Intent(activity, FloatingService::class.java).apply {
            putExtra("autoStop", autoStop)
            putExtra("max", max)
        }

        if (Build.VERSION.SDK_INT >= 26) {
            activity.startForegroundService(intent)
        } else {
            activity.startService(intent)
        }

        if(withActivityLog) addActivityLog(activity, 100)
        else stopActivityLog()
    }

    private var actLogJob: Job? = null
    private fun addActivityLog(context: Context, searchMillis: Long){
        actLogJob?.cancel()
        actLogJob = CoroutineScope(Dispatchers.Main).launch {
            activityFlow(context, searchMillis).collect()
        }
    }

    private fun stopActivityLog(){
        actLogJob?.cancel()
    }

    private fun activityFlow(context: Context, searchMillis: Long) = flow<Map<String, String>> {
        val lastLog = mutableMapOf<String, String>()

        var isUpdated = false
        fun updateLog(key: String, value: String) {
            if (lastLog[key] != value) {
                isUpdated = true
                lastLog[key] = value
            }
        }
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        while (true) {
            for (i in 0 until manager.appTasks.size) {
                val taskInfo = manager.appTasks[i].taskInfo
                val task = if (manager.appTasks.size > 1) "<$i>" else ""

                updateLog(
                    "TopActivity$task",
                    "${taskInfo.topActivity?.shortClassName}"
                )
                updateLog(
                    "BaseActivity$task",
                    "${taskInfo.baseActivity?.shortClassName}"
                )
                updateLog(
                    "Activities$task",
                    "${taskInfo.numActivities}"
                )
            }

            if (isUpdated) {
                for(l in lastLog){
                    Klog.f(l.key, l.value)
                }
                isUpdated = false
            }

            delay(searchMillis)
        }
    }
}
