package com.shong.klog.floating

import android.content.Context
import android.content.SharedPreferences

internal class LastDataPref constructor(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences("lastData", Context.MODE_PRIVATE)

    fun setY(y: Int) = pref.edit().putInt("y", y).apply()
    fun getY(): Int = pref.getInt("y", 0)
}