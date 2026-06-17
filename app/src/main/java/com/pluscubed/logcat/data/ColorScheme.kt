package com.pluscubed.logcat.data

import android.content.Context
import com.pb.daologcat.R

enum class ColorScheme(private val tagColorsResource: Int) {
    Default(R.array.light_theme_colors);

    fun getTagColors(context: Context): IntArray {
        return context.resources.getIntArray(tagColorsResource)
    }
}
