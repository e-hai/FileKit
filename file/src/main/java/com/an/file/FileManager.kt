package com.an.file

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.an.file.functions.*


object FileManager {

    fun shared(context: Application): Shared {
        return Shared(context)
    }

    fun specific(context: Application, isCache: Boolean = false): Specific {
        return Specific(context, isCache)
    }

    fun send(context: Context): Send {
        return Send(context)
    }

    fun checkPermission(fragment: Fragment, listener: PermissionListener) {
        PermissionsFragment.load(fragment).requestPermissions(getPermissions(), listener)
    }

    fun checkPermission(activity: FragmentActivity, listener: PermissionListener) {
        PermissionsFragment.load(activity).requestPermissions(getPermissions(), listener)
    }

    private fun getPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }
}
