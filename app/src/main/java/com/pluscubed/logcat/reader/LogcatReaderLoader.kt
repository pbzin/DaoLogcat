package com.pluscubed.logcat.reader
import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.pluscubed.logcat.helper.LogcatHelper
import com.pluscubed.logcat.helper.PreferenceHelper
import java.io.IOException
class LogcatReaderLoader : Parcelable {
    private val lastLines = mutableMapOf<String, String?>()
    private val recordingMode: Boolean
    private val multiple: Boolean
    constructor(parcel: Parcel) {
        recordingMode = parcel.readInt() == 1
        multiple = parcel.readInt() == 1
        val bundle = parcel.readBundle(javaClass.classLoader)
        if (bundle != null) { for (key in bundle.keySet()) { lastLines[key] = bundle.getString(key) } }
    }
    private constructor(buffers: Set<String>, recordingMode: Boolean) {
        this.recordingMode = recordingMode
        this.multiple = buffers.size > 1
        for (buffer in buffers) { lastLines[buffer] = if (recordingMode) LogcatHelper.getLastLogLine(buffer) else null }
    }
    @Throws(IOException::class)
    fun loadReader(): LogcatReader = if (!multiple) {
        val entry = lastLines.entries.iterator().next()
        SingleLogcatReader(recordingMode, entry.key, entry.value)
    } else { MultipleLogcatReader(recordingMode, lastLines) }
    override fun describeContents(): Int = 0
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(if (recordingMode) 1 else 0)
        dest.writeInt(if (multiple) 1 else 0)
        val bundle = Bundle()
        for ((key, value) in lastLines) { bundle.putString(key, value) }
        dest.writeBundle(bundle)
    }
    companion object CREATOR : Parcelable.Creator<LogcatReaderLoader> {
        override fun createFromParcel(parcel: Parcel): LogcatReaderLoader = LogcatReaderLoader(parcel)
        override fun newArray(size: Int): Array<LogcatReaderLoader?> = arrayOfNulls(size)
        @JvmStatic fun create(context: Context, recordingMode: Boolean): LogcatReaderLoader = LogcatReaderLoader(PreferenceHelper.getBuffers(context), recordingMode)
    }
}
