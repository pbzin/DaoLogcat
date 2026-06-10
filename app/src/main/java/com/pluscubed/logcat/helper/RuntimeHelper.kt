package com.pluscubed.logcat.helper

import android.text.TextUtils
import com.pluscubed.logcat.util.ArrayUtil
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.PrintStream

object RuntimeHelper {
    @JvmStatic
    @Throws(IOException::class)
    fun exec(args: List<String>): Process {
        if (VersionHelper.getVersionSdkIntCompat() >= VersionHelper.VERSION_JELLYBEAN
            && !SuperUserHelper.isFailedToObtainRoot()
        ) {
            val process = Runtime.getRuntime().exec("su")
            var outputStream: PrintStream? = null
            try {
                outputStream = PrintStream(BufferedOutputStream(process.outputStream, 8192))
                outputStream.println(TextUtils.join(" ", args))
                outputStream.flush()
            } finally {
                outputStream?.close()
            }
            return process
        }
        return Runtime.getRuntime().exec(ArrayUtil.toArray(args, String::class.java))
    }

    @JvmStatic
    fun destroy(process: Process) {
        if (VersionHelper.getVersionSdkIntCompat() >= VersionHelper.VERSION_JELLYBEAN
            && !SuperUserHelper.isFailedToObtainRoot()
        ) {
            SuperUserHelper.destroy(process)
        } else {
            process.destroy()
        }
    }
}
