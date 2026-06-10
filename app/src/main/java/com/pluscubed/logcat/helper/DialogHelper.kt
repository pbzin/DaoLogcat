package com.pluscubed.logcat.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.pluscubed.logcat.data.FilterQueryWithLevel
import com.pluscubed.logcat.data.SortedFilterArrayAdapter
import com.pluscubed.logcat.util.ArrayUtil
import com.pluscubed.logcat.util.Callback
import org.omnirom.logcat.R
import java.text.DecimalFormat
import java.util.*

object DialogHelper {
    fun interface InputTextCallback {
        fun setInputText(text: String)
    }

    @JvmStatic
    fun startRecording(
        filename: String,
        filterQuery: String,
        logLevel: String,
        onPostExecute: Runnable?,
        context: Context
    ) {
        val handler = Handler(Looper.getMainLooper())
        Thread {
            ServiceHelper.startBackgroundServiceIfNotAlreadyRunning(context, filename, filterQuery, logLevel)
            handler.post {
                onPostExecute?.run()
            }
        }.start()
    }

    @JvmStatic
    fun isInvalidFilename(filename: CharSequence?): Boolean {
        if (TextUtils.isEmpty(filename)) return true
        val filenameAsString = filename.toString()
        return filenameAsString.contains("/") ||
                filenameAsString.contains(":") ||
                filenameAsString.contains(" ") ||
                !filenameAsString.endsWith(".txt")
    }

    @SuppressLint("InflateParams")
    @JvmStatic
    fun showFilterDialogForRecording(
        context: Context,
        queryFilterText: String?,
        logLevelText: String?,
        filterQuerySuggestions: List<String>,
        callback: Callback<FilterQueryWithLevel>
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val filterView = inflater.inflate(R.layout.dialog_recording_filter, null, false)

        val autoCompleteTextView = filterView.findViewById<AutoCompleteTextView>(R.id.filter_text)
        autoCompleteTextView.setText(queryFilterText)

        val suggestionAdapter = SortedFilterArrayAdapter(
            context, R.layout.list_item_dropdown, filterQuerySuggestions
        )
        autoCompleteTextView.setAdapter(suggestionAdapter)

        val spinner = filterView.findViewById<Spinner>(R.id.spinner)

        val logLevels = context.resources.getStringArray(R.array.log_levels)
        val logLevelsList = logLevels.toMutableList()
        val defaultLogLevel = PreferenceHelper.getDefaultLogLevelPreference(context).toString()
        val logLevelsValues = context.resources.getStringArray(R.array.log_levels_values)
        val index = ArrayUtil.indexOf(logLevelsValues, defaultLogLevel)
        logLevelsList[index] = logLevelsList[index] + " " + context.getString(R.string.default_in_parens)

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, logLevelsList as List<CharSequence>)
        adapter.setDropDownViewResource(R.layout.list_item_dropdown)
        spinner.adapter = adapter

        spinner.setSelection(ArrayUtil.indexOf(logLevelsValues, logLevelText ?: ""))

        AlertDialog.Builder(context)
            .setTitle(R.string.title_filter)
            .setView(filterView)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val logLevelIdx = spinner.selectedItemPosition
                val selectedLogLevelValue = logLevelsValues[logLevelIdx]
                val filterQuery = autoCompleteTextView.text.toString()
                callback.onCallback(FilterQueryWithLevel(filterQuery, selectedLogLevelValue))
            }
            .show()
    }

    @JvmStatic
    fun stopRecordingLog(context: Context) {
        ServiceHelper.stopBackgroundServiceIfRunning(context)
    }

    @JvmStatic
    fun showFilenameSuggestingDialog(
        context: Context,
        negativeCallback: Runnable?,
        inputCallback: InputTextCallback,
        titleResId: Int
    ) {
        val v = initFilenameInputDialog(context)
        val input = v.findViewById<EditText>(R.id.edit_text)
        AlertDialog.Builder(context)
            .setTitle(titleResId)
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                negativeCallback?.run()
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                inputCallback.setInputText(input.text.toString())
            }
            .setMessage(R.string.enter_filename)
            .setView(v)
            .show()
    }

    @JvmStatic
    fun initFilenameInputDialog(context: Context): View {
        val v = LayoutInflater.from(context).inflate(R.layout.dialog_input_text, null, false)
        val editText = v.findViewById<EditText>(R.id.edit_text)
        editText.setSingleLine()
        editText.inputType = InputType.TYPE_TEXT_VARIATION_FILTER
        editText.imeOptions = EditorInfo.IME_ACTION_DONE

        val filename = createLogFilename()
        editText.setText(filename)
        editText.setSelection(0, filename.length - 4)
        return v
    }

    @JvmStatic
    fun createLogFilename(): String {
        val calendar = GregorianCalendar()
        val twoDigit = DecimalFormat("00")
        val fourDigit = DecimalFormat("0000")

        val year = fourDigit.format(calendar.get(Calendar.YEAR).toLong())
        val month = twoDigit.format((calendar.get(Calendar.MONTH) + 1).toLong())
        val day = twoDigit.format(calendar.get(Calendar.DAY_OF_MONTH).toLong())
        val hour = twoDigit.format(calendar.get(Calendar.HOUR_OF_DAY).toLong())
        val minute = twoDigit.format(calendar.get(Calendar.MINUTE).toLong())
        val second = twoDigit.format(calendar.get(Calendar.SECOND).toLong())

        return "$year-$month-$day-$hour-$minute-$second.txt"
    }
}
