package com.an.file.functions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.core.content.FileProvider
import com.filex.BuildConfig
import java.io.File

class Send internal constructor(private val context: Context) {

    //分享文件
    fun sendFile(file: File, title: String = ""): Boolean {
        return try {
            val fileUri = FileProvider.getUriForFile(context, BuildConfig.FILES_AUTHORITY, file)
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = context.contentResolver.getType(fileUri)
            }
            context.startActivity(Intent.createChooser(shareIntent, title))

            val chooser = Intent.createChooser(shareIntent, title)
            val resInfoList: List<ResolveInfo> = context.packageManager
                .queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)

            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName, fileUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}