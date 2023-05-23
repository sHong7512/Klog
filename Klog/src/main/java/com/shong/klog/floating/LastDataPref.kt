package com.shong.klog.floating

import android.content.Context
import android.content.SharedPreferences

internal class LastDataPref constructor(context: Context){
    private val pref: SharedPreferences = context.getSharedPreferences("lastData", Context.MODE_PRIVATE)

    fun setX(x: Int) = pref.edit().putInt("x", x).apply()
    fun getX(): Int = pref.getInt("x", 0)

    fun setY(y: Int) = pref.edit().putInt("y", y).apply()
    fun getY(): Int = pref.getInt("y", 0)
}