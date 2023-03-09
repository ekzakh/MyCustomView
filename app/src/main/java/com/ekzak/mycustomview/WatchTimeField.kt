package com.ekzak.mycustomview

import java.util.*

class WatchTimeField(@JvmField var time: Date) {
    private val calendar = Calendar.getInstance()

    fun setTime(value: Date) {
        time = value
        calendar.time = value
    }

    val hour
        get() = calendar.get(Calendar.HOUR)
    val minute
        get() = calendar.get(Calendar.MINUTE)
    val seconds
        get() = calendar.get(Calendar.SECOND)

}
