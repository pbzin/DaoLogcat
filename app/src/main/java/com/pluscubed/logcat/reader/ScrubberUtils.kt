package com.pluscubed.logcat.reader

import java.util.regex.Pattern

object ScrubberUtils {
    private val EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9_]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*(@|%40)(?!([a-zA-Z0-9]*\\.[a-zA-Z0-9]*\\.[a-zA-Z0-9]*\\.))(?:[A-Za-z0-9](?:[a-zA-Z0-9-]*[A-Za-z0-9])?\\.)+[a-zA-Z](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?")
    private val PHONE_NUMBER_PATTERN = Pattern.compile("^(?:(?:\\+?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?$")
    private val WEB_URL_PATTERN = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
    private val IP_ADDRESS_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")
    private val PHONE_INFO_PATTERN = Pattern.compile("(msisdn=|mMsisdn=|iccid=|iccid: |mImsi=)[a-zA-Z0-9]*", Pattern.CASE_INSENSITIVE)
    private val USER_INFO_PATTERN = Pattern.compile("(UserInfo\\{\\d:)[a-zA-Z0-9\\s]*", Pattern.CASE_INSENSITIVE)
    private val ACCOUNT_INFO_PATTERN = Pattern.compile("(Account \\{name=)[a-zA-Z0-9]*", Pattern.CASE_INSENSITIVE)

    private const val IGNORE_DATA_RESOURCE_CACHE = "/data/resource-cache"
    private const val IGNORE_DATA_DALVIK_CACHE = "/data/dalvik-cache"
    private const val IGNORE_CACHE_DALVIK_CACHE = "/cache/dalvik-cache"

    @JvmStatic fun scrubLine(line: String): String {
        var scrubbed = line
        if (scrubbed.contains(IGNORE_DATA_RESOURCE_CACHE) ||
            scrubbed.contains(IGNORE_DATA_DALVIK_CACHE) ||
            scrubbed.contains(IGNORE_CACHE_DALVIK_CACHE)) {
            return scrubbed
        }
        scrubbed = IP_ADDRESS_PATTERN.matcher(scrubbed).replaceAll("<IP address omitted>")
        scrubbed = EMAIL_PATTERN.matcher(scrubbed).replaceAll("<email omitted>")
        scrubbed = PHONE_NUMBER_PATTERN.matcher(scrubbed).replaceAll("<phone number omitted>")
        scrubbed = WEB_URL_PATTERN.matcher(scrubbed).replaceAll("<web url omitted>")
        scrubbed = PHONE_INFO_PATTERN.matcher(scrubbed).replaceAll("<omitted>")
        scrubbed = USER_INFO_PATTERN.matcher(scrubbed).replaceAll("<omitted>")
        scrubbed = ACCOUNT_INFO_PATTERN.matcher(scrubbed).replaceAll("<omitted>")
        return scrubbed
    }
}
