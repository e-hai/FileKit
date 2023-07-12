package com.an.file

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.an.file.functions.*
import com.an.file.functions.PermissionsFragment
import com.an.file.functions.Send.sendFile
import com.an.file.functions.Shared
import com.an.file.functions.Specific

/**
 * 目标SDK版本为Android 11及以上
 * **/
object FileManager {

    /**
     * 共享的存储空间的文件操作
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
    fun send(context: Context, fileUri: Uri, title: String = ""): Boolean {
        return sendFile(context, fileUri, title)
    }

    fun checkPermission(fragment: Fragment, listener: PermissionListener) {
        PermissionsFragment.load(fragment).requestPermissions(getPermissions(), listener)
    }

    fun checkPermission(activity: FragmentActivity, listener: PermissionListener) {
        PermissionsFragment.load(activity).requestPermissions(getPermissions(), listener)
    }

    private fun getPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    fun createFileByAssets(context: Application, assetName: String): Uri {
        val inputStream = context.assets.open(assetName)
        return specificStorage(context).saveOther(assetName, inputStream)
    }
}
