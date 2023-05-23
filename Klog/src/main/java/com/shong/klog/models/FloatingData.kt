package com.shong.klog.models

internal data class FloatingData(
    val key: String,
    val msg: String,
    val logLevel: LogLevel,
    val timeMillis: Long,
)