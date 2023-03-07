package com.ekzak.mycustomview

import java.sql.Time

typealias OnFieldChangedListener = (field: WatchTimeField) -> Unit

class WatchTimeField(val time: Time) {

    var listeners = mutableSetOf<OnFieldChangedListener>()

    set(value) {
        if (value != time) {
            field = value
            listeners.forEach { it.invoke(this) }
        }
    }
}
