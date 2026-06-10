package com.pluscubed.logcat.data

import android.content.Context
import android.widget.ArrayAdapter

class SortedFilterArrayAdapter<T>(context: Context, resource: Int, objects: List<T>) :
    ArrayAdapter<T>(context, resource, objects)
