package com.pluscubed.logcat.util

fun interface Function<E, T> {
    fun apply(input: E): T
}
