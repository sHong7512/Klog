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
import com.shong.klog.Klog
import com.shong.klog.databinding.FloatLoggerLayoutBinding
import com.shong.klog.databinding.ItemLogBinding
import com.shong.klog.models.FloatingData
import com.shong.klog.models.RemoveMode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import java.text.SimpleDateFormat

internal class FloatingLoggerMaker constructor(private val context: Context) {
    private var inflater: LayoutInflater =
        context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private lateinit var binding: FloatLoggerLayoutBinding
    private val windowManager: WindowManager =
        context.getSystemService(Service.WINDOW_SERVICE) as WindowManager

    private val lastDataPref = LastDataPref(context)

    private var logJob: Job? = null
    private var logFlow:SharedFlow<FloatingData>? = null
    private var removeKeyJob: Job? = null
    private val removeKeyFlow = Klog.removeKeyFlow

    private val logItemMap = mutableMapOf<String, ItemLogBinding>()
    private val keyOrderList = mutableListOf<String>()

    private var autoStopJob: Job? = null
    private var max = Klog.MAX_BASE

    private var deviceWidth: Int = -1
    private var deviceHeight: Int = -1

    private var simpleDateFormat: SimpleDateFormat = SimpleDateFormat("HH:mm:ss.SSS")
    private fun startViewUpdate() {
        val baseLayout = binding.floatingLayout
        logJob?.cancel()
        logJob = CoroutineScope(Dispatchers.Main).launch {
            logFlow?.collect {
                if (logItemMap.keys.contains(it.key)) {
                    baseLayout.removeView(logItemMap[it.key]!!.root)
                    logItemMap[it.key]!!.levelTextView.text = it.logLevel.name
                    logItemMap[it.key]!!.levelTextView.setBackgroundColor(it.logLevel.getBackgroundColor())
                    logItemMap[it.key]!!.levelTextView.setTextColor(it.logLevel.getTextColor())
                    logItemMap[it.key]!!.timeTextView.text = simpleDateFormat.format(it.timeMillis)
                    logItemMap[it.key]!!.keyTextView.text = it.key
                    logItemMap[it.key]!!.valueTextView.text = it.msg
                } else {
                    val layout = ItemLogBinding.inflate(inflater, baseLayout, false)
                    layout.levelTextView.text = it.logLevel.name
                    layout.levelTextView.setBackgroundColor(it.logLevel.getBackgroundColor())
                    layout.levelTextView.setTextColor(it.logLevel.getTextColor())
                    layout.timeTextView.text = simpleDateFormat.format(it.timeMillis)
                    layout.keyTextView.text = it.key
                    layout.valueTextView.text = it.msg
                    logItemMap[it.key] = layout
                }

                if (keyOrderList.contains(it.key)) {
                    keyOrderList.remove(it.key)
                }
                keyOrderList.add(it.key)

                baseLayout.addView(logItemMap[it.key]!!.root)
                removeForMax()
            }
        }
        removeKeyJob?.cancel()
        removeKeyJob = CoroutineScope(Dispatchers.Main).launch {
            removeKeyFlow.collect {
                when (it.removeMode) {
                    RemoveMode.All -> {
                        for (l in logItemMap) {
                            baseLayout.removeView(l.value.root)
                        }
                        logItemMap.clear()
                        keyOrderList.clear()
                    }
                    RemoveMode.Key -> {
                        if (it.key != null && logItemMap.keys.contains(it.key)) {
                            baseLayout.removeView(logItemMap[it.key]!!.root)
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
                                baseLayout.removeView(logItemMap[key]!!.root)
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
            val len = keyOrderList.size - max
            for(i in 0 until len){
                binding.floatingLayout.removeView(logItemMap[keyOrderList[0]]!!.root)
                logItemMap.remove(keyOrderList[0])
                keyOrderList.removeAt(0)
            }
//            for (key in subList) {
//                binding.floatingLayout.removeView(logItemMap[key]!!.root)
//                logItemMap.remove(key)
//                keyOrderList.remove(key)
//            }
        }
    }

    internal fun makeWindow(autoStop: Boolean, max: Int): View {
        logFlow = Klog.logFlow
        this.max = max

        if (autoStop) startActCheck()
        else stopActCheck()

        if (this::binding.isInitialized) {
            Klog.w(this, "Refused!! Because it was already made.")
            removeForMax()
            startViewUpdate()
            return binding.root
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


        val binding: FloatLoggerLayoutBinding = FloatLoggerLayoutBinding.inflate(inflater).apply {
            floatingLayout.setOnTouchListener(moveOnTouchListener())
            floatingLayout.setOnClickListener {
                Klog.d(this, "floatingView Click!")
            }
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

//        params.x = lastDataPref.getX()
        params.y = lastDataPref.getY()

        windowManager.addView(binding.root, params)
        this.binding = binding

        startViewUpdate()

        return binding.root
    }

    internal fun closeWindow() {
        stopViewUpdate()
        stopActCheck()
        if (this::binding.isInitialized) {
            val params = binding.root.layoutParams as WindowManager.LayoutParams
            lastDataPref.run {
//                setX(params.x)
                setY(params.y)
            }

            windowManager.removeView(binding.root)
        }
    }

    private fun stopService() {
        val intent = Intent(context, FloatingService::class.java)
        context.stopService(intent)
    }

    //    private var xPos = 0f
    private var yPos = 0f

    @SuppressLint("ClickableViewAccessibility")
    private fun moveOnTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { _, event ->
            val params = binding.root.layoutParams as WindowManager.LayoutParams

            if (event.pointerCount >= 1) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
//                        xPos = event.rawX
                        yPos = event.rawY
//                        return@OnTouchListener true
                    }
                    MotionEvent.ACTION_MOVE -> {
//                        val dx: Float = xPos - event.rawX
                        val dy: Float = yPos - event.rawY

//                        var bufX = (params.x - dx).toInt()
//                        if (bufX > deviceWidth / 2) bufX = deviceWidth / 2
//                        else if (bufX < -deviceWidth / 2) bufX = -deviceWidth / 2
//                        params.x = bufX

                        var bufY = (params.y - dy).toInt()
                        if (bufY > deviceHeight / 2) bufY = deviceHeight / 2
                        else if (bufY < -deviceHeight / 2) bufY = -deviceHeight / 2
                        params.y = bufY

                        windowManager.updateViewLayout(binding.root, params)

//                        xPos = event.rawX
                        yPos = event.rawY
//                        return@OnTouchListener true
                    }
                    MotionEvent.ACTION_UP -> {
                        lastDataPref.run {
//                            setX(params.x)
                            setY(params.y)
                        }
                    }
                }
            }

            false
        }
    }

}