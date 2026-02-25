package com.twofasapp.core.common.ktx

import android.content.Context
import android.net.Uri
import java.io.InputStream

fun Uri.inputStream(context: Context): InputStream? {
    return context.contentResolver.openInputStream(this)
}