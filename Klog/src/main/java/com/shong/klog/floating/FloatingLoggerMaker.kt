package com.shong.klog.floating

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.shong.klog.Klog
import com.shong.klog.R
import com.shong.klog.models.FloatingData
import com.shong.klog.models.RemoveMode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import java.text.SimpleDateFormat

internal class FloatingLoggerMaker constructor(private val context: Context) {
    private var inflater: LayoutInflater =
        context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private lateinit var baseLayout: ViewGroup
    private val windowManager: WindowManager =
        context.getSystemService(Service.WINDOW_SERVICE) as WindowManager

    private val lastDataPref = LastDataPref(context)

    private var logJob: Job? = null
    private var logFlow: SharedFlow<FloatingData>? = null
    private var removeKeyJob: Job? = null
    private val removeKeyFlow = Klog.removeKeyFlow

    private val logItemMap = mutableMapOf<String, View>()
    private val keyOrderList = mutableListOf<String>()

    private var autoStopJob: Job? = null
    private var max = Klog.MAX_BASE

    private var deviceWidth: Int = -1
    private var deviceHeight: Int = -1

    @SuppressLint("SimpleDateFormat")
    private var simpleDateFormat: SimpleDateFormat = SimpleDateFormat("HH:mm:ss.SSS")

    private fun startViewUpdate() {
        logJob?.cancel()
        val itemsLayout = baseLayout.findViewById<LinearLayout>(R.id.itemsLayout)

        val setItems: (View, FloatingData) -> Unit = { v, data ->
            v.run {
                findViewById<TextView>(R.id.levelTextView).text = data.logLevel.name
                findViewById<TextView>(R.id.levelTextView)
                    .setBackgroundColor(data.logLevel.getBackgroundColor())
                findViewById<TextView>(R.id.levelTextView).setTextColor(data.logLevel.getTextColor())
                findViewById<TextView>(R.id.timeTextView).text =
                    simpleDateFormat.format(data.timeMillis)
                findViewById<TextView>(R.id.keyTextView).text = data.key
                findViewById<TextView>(R.id.valueTextView).text = data.msg
            }
        }
        logJob = CoroutineScope(Dispatchers.Main).launch {
            logFlow?.collect {
                if (logItemMap.keys.contains(it.key)) {
                    itemsLayout.removeView(logItemMap[it.key]!!)
                    setItems(logItemMap[it.key]!!, it)
                } else {
                    val layout = inflater.inflate(R.layout.item_log, null)
                    setItems(layout, it)
                    logItemMap[it.key] = layout
                }

                if (keyOrderList.contains(it.key)) {
                    keyOrderList.remove(it.key)
                }
                keyOrderList.add(it.key)

                itemsLayout.addView(logItemMap[it.key]!!)
                removeForMax()
            }
        }
        removeKeyJob?.cancel()
        removeKeyJob = CoroutineScope(Dispatchers.Main).launch {
            removeKeyFlow.collect {
                when (it.removeMode) {
                    RemoveMode.All -> {
                        for (l in logItemMap) {
                            itemsLayout.removeView(l.value)
                        }
                        logItemMap.clear()
                        keyOrderList.clear()
                    }
                    RemoveMode.Key -> {
                        if (it.key != null && logItemMap.keys.contains(it.key)) {
                            itemsLayout.removeView(logItemMap[it.key]!!)
                            logItemMap.remove(it.key)
                            keyOrderList.remove(it.key)
                        }
                    }
                    RemoveMode.Contain -> {
                        if (it.key != null) {
                            val list = mutableListOf<String>()
                            for (key in logItemMap.keys) {
                                if (key.contains(it.key)) {
                                    list.add(key)
                                }
                            }
                            for (key in list) {
                                itemsLayout.removeView(logItemMap[key]!!)
                                logItemMap.remove(key)
                                keyOrderList.remove(key)
                            }
                        }
                    }
                    else -> {
                        Klog.w(this, "undefined RemoveMode")
                    }
                }
            }
        }
    }

    private fun stopViewUpdate() {
        logJob?.cancel()
        removeKeyJob?.cancel()
    }

    private fun startActCheck() {
        autoStopJob?.cancel()
        autoStopJob = CoroutineScope(Dispatchers.Default).launch {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            while (true) {
                var actCnt = 0
                for (i in 0 until manager.appTasks.size) {
                    val taskInfo = manager.appTasks[i].taskInfo
                    actCnt += taskInfo.numActivities
                }

                if (actCnt == 0) stopService()
                delay(100)
            }
        }
    }

    private fun stopActCheck() {
        autoStopJob?.cancel()
    }

    private fun removeForMax() {
        if (keyOrderList.size > max) {
            val itemsLayout = baseLayout.findViewById<LinearLayout>(R.id.itemsLayout)
            val len = keyOrderList.size - max
            for (i in 0 until len) {
                itemsLayout.removeView(logItemMap[keyOrderList[0]]!!)
                logItemMap.remove(keyOrderList[0])
                keyOrderList.removeAt(0)
            }
        }
    }

    internal fun makeWindow(autoStop: Boolean, max: Int): View {
        logFlow = Klog.logFlow
        this.max = max

        if (autoStop) startActCheck()
        else stopActCheck()

        if (this::baseLayout.isInitialized) {
            removeForMax()
            startViewUpdate()
            Klog.w(this, "Only update options!! Because it was already made.")
            return baseLayout
        }

        if (Build.VERSION.SDK_INT >= 30) {
            val metrics = windowManager.currentWindowMetrics
            deviceHeight = metrics.bounds.height()
            deviceWidth = metrics.bounds.width()
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            deviceHeight = displayMetrics.heightPixels
            deviceWidth = displayMetrics.widthPixels
        }

        val bufView = inflater.inflate(R.layout.float_logger_layout, null) as ViewGroup
        bufView.findViewById<ConstraintLayout>(R.id.floatingLayout).apply {
            setOnClickListener { Klog.d(this, "floatingView Click!") }
            setOnTouchListener(moveOnTouchListener())
        }
        bufView.findViewById<TextView>(R.id.closeButton).setOnClickListener {
            stopService()
        }

        val lpType =
            if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            lpType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        params.y = lastDataPref.getY()

        windowManager.addView(bufView, params)
        this.baseLayout = bufView

        startViewUpdate()

        return bufView
    }

    internal fun closeWindow() {
        stopViewUpdate()
        stopActCheck()
        if (this::baseLayout.isInitialized) {
            val params = baseLayout.layoutParams as WindowManager.LayoutParams
            lastDataPref.run {
                setY(params.y)
            }

            windowManager.removeView(baseLayout)
        }
    }

    private fun stopService() {
        val intent = Intent(context, FloatingService::class.java)
        context.stopService(intent)
    }

    private var yPos = 0f

    @SuppressLint("ClickableViewAccessibility")
    private fun moveOnTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { _, event ->
            val params = baseLayout.layoutParams as WindowManager.LayoutParams

            if (event.pointerCount >= 1) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        yPos = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dy: Float = yPos - event.rawY

                        var bufY = (params.y - dy).toInt()
                        if (bufY > deviceHeight / 2) bufY = deviceHeight / 2
                        else if (bufY < -deviceHeight / 2) bufY = -deviceHeight / 2
                        params.y = bufY

                        windowManager.updateViewLayout(baseLayout, params)

                        yPos = event.rawY
                    }
                    MotionEvent.ACTION_UP -> {
                        lastDataPref.run {
                            setY(params.y)
                        }
                    }
                }
            }

            false
        }
    }

}