package com.example.orgamam.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.orgamam.MainActivity

// Note: This is a hacky way to get context in KMP without DI for now
var appContext: Context? = null

actual fun getVersionCode(): Int {
    return appContext?.packageManager?.getPackageInfo(appContext?.packageName ?: "", 0)?.let {
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            it.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            it.versionCode
        }
    } ?: 0
}

actual fun openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    appContext?.startActivity(intent)
}
