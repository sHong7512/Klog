package com.shong.klog.models

internal enum class RemoveMode{
    All,
    Key,
    Contain,
}

internal data class RemoveData(
    val key: String?,
    val removeMode: RemoveMode,
)