package com.example.testandroidstudio.utility

import android.content.Context

class ResourceHelper(private val context: Context) {
    fun getString(resId: Int): String {
        return context.getString(resId)
    }

    fun getString(resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}