package com.pluscubed.logcat.db

class FilterItem private constructor(val id: Int, val text: String?) : Comparable<FilterItem> {
    companion object {
        @JvmField
        val DEFAULT_COMPARATOR = Comparator<FilterItem> { lhs, rhs ->
            val leftText = lhs.text ?: ""
            val rightText = rhs.text ?: ""
            leftText.compareTo(rightText, ignoreCase = true)
        }

        @JvmStatic
        fun create(id: Int, text: String?): FilterItem {
            return FilterItem(id, text)
        }
    }

    override fun compareTo(other: FilterItem): Int {
        return DEFAULT_COMPARATOR.compare(this, other)
    }
}
