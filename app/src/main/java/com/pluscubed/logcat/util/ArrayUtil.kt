package com.pluscubed.logcat.util

import java.lang.reflect.Array

object ArrayUtil {
    @JvmStatic
    fun <T> indexOf(array: kotlin.Array<T>, `object`: T): Int {
        for (i in array.indices) {
            if (`object` == array[i]) {
                return i
            }
        }
        return -1
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> toArray(list: List<T>, clazz: Class<T>): kotlin.Array<T> {
        val result = Array.newInstance(clazz, list.size) as kotlin.Array<T>
        for (i in list.indices) {
            result[i] = list[i]
        }
        return result
    }
}
