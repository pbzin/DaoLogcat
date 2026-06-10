package com.pluscubed.logcat.util

fun interface Callback<T> {
    fun onCallback(`object`: T)
}
