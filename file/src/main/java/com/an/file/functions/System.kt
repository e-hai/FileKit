package com.an.file.functions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.an.file.BuildConfig
import com.an.file.FileManager.TAG
import java.io.File


/**
 * 调用系统自带API-分享文件
 * 注意点：只能分享应用自身的文件
 * **/
internal object Send {

    fun sendFile(context: Context, file: File, title: String): Boolean {
        val authority = "${context.packageName}.an.file.path"
        val fileUri: Uri = FileProvider.getUriForFile(context, authority, file)
        return sendFile(context, fileUri, title)
    }

    fun sendFile(context: Context, fileUri: Uri, title: String): Boolean {
        return try {
            val uri = if (fileUri.toString().startsWith("file://")) {
                val authority = "${context.packageName}.an.file.path"
                FileProvider.getUriForFile(context, authority, fileUri.toFile())
            } else {
                fileUri
            }

            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_STREAM, uri)
                type = context.contentResolver.getType(uri)
            }
            val chooser = Intent.createChooser(shareIntent, title)

            val resInfoList: List<ResolveInfo> =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.queryIntentActivities(
                        chooser,
                        PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                    )
                } else {
                    context.packageManager.queryIntentActivities(
                        chooser,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                }
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

/**
 * 调用系统自带API-选择媒体文件
 * **/
internal object Picker {

    fun pickPhoto(activity: Activity, requestCode: Int) {
        val intent = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
        } else {
            Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = "image/*"
            }
        }
        activity.startActivityForResult(intent, requestCode)
    }

    fun pickVideo(activity: Activity, requestCode: Int) {
        val intent = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
        } else {
            Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = "video/*"
            }
        }
        activity.startActivityForResult(intent, requestCode)
    }
}