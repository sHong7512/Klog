package com.shong.klog.models

import android.graphics.Color


enum class LogLevel(val definedName : String, val backColorStr: String, val textColorStr: String){
    N("None","#ffffff", "#000000"),
    V("Verbose","#008000", "#ffffff"),
    D("Debugging", "#000000", "#ffffff"),
    I("Information", "#0000ff", "#ffffff"),
    W("Warning", "#dddd00", "#ffffff"),
    E("Error", "#ff0000", "#ffffff");

    fun getBackgroundColor() : Int = Color.parseColor(this.backColorStr)
    fun getTextColor() : Int = Color.parseColor(this.textColorStr)
}
