package com.pluscubed.logcat.data

import com.pluscubed.logcat.util.StringUtil
import java.util.regex.Pattern

class SearchCriteria(inputQuery: CharSequence?) {
    private var pid = -1
    private var tag: String? = null
    private var searchText: String
    private var searchTextAsInt = -1

    init {
        // check for the "pid" keyword
        val query = StringBuilder(StringUtil.nullToEmpty(inputQuery))
        val pidMatcher = PID_PATTERN.matcher(query)
        if (pidMatcher.find()) {
            try {
                pid = pidMatcher.group(1)!!.toInt()
                query.replace(pidMatcher.start(), pidMatcher.end(), "") // remove from search string
            } catch (ignore: NumberFormatException) {
            }
        }

        // check for the "tag" keyword
        val tagMatcher = TAG_PATTERN.matcher(query)
        if (tagMatcher.find()) {
            tag = tagMatcher.group(1)
            if (tag!!.startsWith("\"") && tag!!.endsWith("\"")) {
                tag = tag!!.substring(1, tag!!.length - 1) // remove quotes
            }
            query.replace(tagMatcher.start(), tagMatcher.end(), "") // remove from search string
        }

        // everything else becomes a search term
        searchText = query.toString().trim { it <= ' ' }

        try {
            searchTextAsInt = searchText.toInt()
        } catch (ignore: NumberFormatException) {
        }
    }

    fun isEmpty(): Boolean {
        return pid == -1 && tag.isNullOrEmpty() && searchText.isEmpty()
    }

    fun matches(logLine: LogLine): Boolean {
        // consider the criteria to be ANDed
        if (!checkFoundPid(logLine)) {
            return false
        }
        if (!checkFoundTag(logLine)) {
            return false
        }
        return checkFoundText(logLine)
    }

    private fun checkFoundText(logLine: LogLine): Boolean {
        return searchText.isEmpty()
                || (searchTextAsInt != -1 && searchTextAsInt == logLine.processId)
                || (logLine.tag != null && StringUtil.containsIgnoreCase(logLine.tag, searchText))
                || (logLine.logOutput != null && StringUtil.containsIgnoreCase(logLine.logOutput, searchText))
    }

    private fun checkFoundTag(logLine: LogLine): Boolean {
        return tag.isNullOrEmpty()
                || (logLine.tag != null && StringUtil.containsIgnoreCase(logLine.tag, tag))
    }

    private fun checkFoundPid(logLine: LogLine): Boolean {
        return pid == -1 || logLine.processId == pid
    }

    companion object {
        const val PID_KEYWORD = "pid:"
        const val TAG_KEYWORD = "tag:"

        private val PID_PATTERN = Pattern.compile("pid:(\\d+)", Pattern.CASE_INSENSITIVE)
        private val TAG_PATTERN = Pattern.compile("tag:(\"[^\"]+\"|\\S+)", Pattern.CASE_INSENSITIVE)
    }
}
