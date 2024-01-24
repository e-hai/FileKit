package com.an.file

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.an.file.functions.*
import com.an.file.functions.PermissionsFragment
import com.an.file.functions.Shared
import com.an.file.functions.Specific
import java.io.File

/**
 * 目标SDK版本为Android 14及以上
 * **/
object FileManager {
    val TAG = Shared::class.simpleName

    enum class ReadType {
        ALL, VIDEO, IMAGE, AUDIO
    }

    fun checkPermissionBeforeRead(
        context: Fragment,
        listener: PermissionListener,
        readType: ReadType = ReadType.ALL
    ) {
        checkPermissionBeforeRead(context.childFragmentManager, listener, readType)
    }

    fun checkPermissionBeforeRead(
        context: FragmentActivity,
        listener: PermissionListener,
        readType: ReadType = ReadType.ALL
    ) {
        checkPermissionBeforeRead(context.supportFragmentManager, listener, readType)
    }

    private fun checkPermissionBeforeRead(
        fragmentManager: FragmentManager,
        listener: PermissionListener,
        type: ReadType = ReadType.ALL
    ) {
        val perms = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            when (type) {
                ReadType.VIDEO -> {
                    arrayOf(
                        android.Manifest.permission.READ_MEDIA_VIDEO
                    )
                }

                ReadType.AUDIO -> {
                    arrayOf(
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    )
                }

                ReadType.IMAGE -> {
                    arrayOf(
                        android.Manifest.permission.READ_MEDIA_AUDIO
                    )
                }

                else -> {
                    arrayOf(
                        android.Manifest.permission.READ_MEDIA_VIDEO,
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_AUDIO
                    )
                }
            }
        }

        PermissionsFragment
            .load(fragmentManager)
            .requestPermissions(perms, listener)
    }


    fun checkPermissionBeforeWrite(fragment: Fragment, listener: PermissionListener) {
        checkPermissionBeforeRead(fragment.childFragmentManager, listener)
    }

    fun checkPermissionBeforeWrite(activity: FragmentActivity, listener: PermissionListener) {
        checkPermissionBeforeRead(activity.supportFragmentManager, listener)
    }

    private fun checkPermissionBeforeWrite(
        fragmentManager: FragmentManager,
        listener: PermissionListener
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            listener.invoke(true)
        } else {
            val perms = arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            PermissionsFragment
                .load(fragmentManager)
                .requestPermissions(perms, listener)
        }
    }


    /**
     * 共享的存储空间的文件操作-调用该方法前请先调用checkPermission
     * **/
    fun sharedStorage(context: Application): Storage {
        return Shared(context)
    }

    /**
     * 应用专属存储空间的文件操作
     * **/
    fun specificStorage(context: Application, isCache: Boolean = false): Storage {
        return Specific(context, isCache)
    }

    /**
     * 分享文件给其他应用
     * **/
    fun send(context: Context, file: File, title: String = ""): Boolean {
        return Send.sendFile(context, file, title)
    }

    fun send(context: Context, fileUri: Uri, title: String = ""): Boolean {
        return Send.sendFile(context, fileUri, title)
    }

    fun pickPhoto(activity: Activity, requestCode: Int) {
        Picker.pickPhoto(activity, requestCode)
    }

    fun pickVideo(activity: Activity, requestCode: Int) {
        Picker.pickVideo(activity, requestCode)
    }

    fun createFileByAssets(context: Application, assetName: String): Uri {
        val inputStream = context.assets.open(assetName)
        return specificStorage(context).saveOther(assetName, inputStream)
    }
}
