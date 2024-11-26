package com.kit.file

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.kit.file.functions.*
import com.kit.file.functions.PermissionsFragment
import com.kit.file.functions.Shared
import com.kit.file.functions.Specific
import java.io.File

/**
 * 文件管理类，用于处理文件读取和写入的权限检查与文件操作。
 * 目标SDK版本为Android 14及以上
 */
object FileManager {
    val TAG = Shared::class.simpleName

    enum class ReadType {
        ALL, VIDEO, IMAGE, AUDIO
    }

    /**
     * 在执行读取操作之前检查所需的权限。
     * 根据设备的SDK版本和指定的文件类型，检查是否需要权限。
     *
     * @param context Fragment或Activity实例
     * @param listener 权限请求的回调监听器
     * @param readType 文件类型，默认为ALL（所有类型）
     */
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

    /**
     * 内部方法，用于处理权限请求。
     * 根据不同的SDK版本和文件类型，动态生成所需的权限。
     *
     * @param fragmentManager FragmentManager实例
     * @param listener 权限请求的回调监听器
     * @param type 读取文件类型（ALL，VIDEO，IMAGE，AUDIO）
     */
    private fun checkPermissionBeforeRead(
        fragmentManager: FragmentManager,
        listener: PermissionListener,
        type: ReadType = ReadType.ALL
    ) {
        val perms = getRequiredPermissionsForRead(type)
        PermissionsFragment
            .load(fragmentManager)
            .requestPermissions(perms, listener)
    }

    /**
     * 获取不同读取类型的权限数组。
     *
     * @param type 读取文件类型（ALL，VIDEO，IMAGE，AUDIO）
     * @return 权限数组
     */
    private fun getRequiredPermissionsForRead(type: ReadType): Array<String> {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                when (type) {
                    ReadType.VIDEO -> arrayOf(android.Manifest.permission.READ_MEDIA_VIDEO)
                    ReadType.AUDIO -> arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO)
                    ReadType.IMAGE -> arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
                    else -> arrayOf(
                        android.Manifest.permission.READ_MEDIA_VIDEO,
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_AUDIO
                    )
                }
            }
        }
    }

    /**
     * 检查写入权限。
     *
     * @param fragment Fragment实例
     * @param listener 权限请求的回调监听器
     */
    fun checkPermissionBeforeWrite(fragment: Fragment, listener: PermissionListener) {
        checkPermissionBeforeWrite(fragment.childFragmentManager, listener)
    }

    fun checkPermissionBeforeWrite(activity: FragmentActivity, listener: PermissionListener) {
        checkPermissionBeforeWrite(activity.supportFragmentManager, listener)
    }

    /**
     * 内部方法，用于处理写入权限请求。
     * 如果SDK版本高于Q，直接通过权限检查，否则请求相关权限。
     *
     * @param fragmentManager FragmentManager实例
     * @param listener 权限请求的回调监听器
     */
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
     * 获取共享存储空间的文件操作对象，使用前必须先调用checkPermission检查权限。
     *
     * @param context Application实例
     * @return Shared存储对象
     */
    fun sharedStorage(context: Application): Storage {
        return Shared(context)
    }

    /**
     * 获取应用专属存储空间的文件操作对象。
     *
     * @param context Application实例
     * @param isCache 是否是缓存目录，默认为false
     * @return Specific存储对象
     */
    fun specificStorage(context: Application, isCache: Boolean = false): Storage {
        return Specific(context, isCache)
    }

    /**
     * 分享文件给其他应用。
     *
     * @param context 当前上下文
     * @param file 要分享的文件
     * @param title 分享时显示的标题
     * @return 是否成功发起分享
     */
    fun send(context: Context, file: File, title: String = ""): Boolean {
        return Send.sendFile(context, file, title)
    }

    /**
     * 分享文件给其他应用。
     *
     * @param context 当前上下文
     * @param fileUri 文件URI
     * @param title 分享时显示的标题
     * @return 是否成功发起分享
     */
    fun send(context: Context, fileUri: Uri, title: String = ""): Boolean {
        return Send.sendFile(context, fileUri, title)
    }

    /**
     * 选择照片。
     *
     * @param activity 当前活动
     * @param requestCode 请求代码
     */
    fun pickPhoto(activity: Activity, requestCode: Int) {
        Picker.pickPhoto(activity, requestCode)
    }

    /**
     * 选择视频。
     *
     * @param activity 当前活动
     * @param requestCode 请求代码
     */
    fun pickVideo(activity: Activity, requestCode: Int) {
        Picker.pickVideo(activity, requestCode)
    }

    /**
     * 从资产目录创建文件并保存。
     *
     * @param context 当前应用上下文
     * @param assetName 资产文件名
     * @return 创建的文件URI
     */
    fun createFileByAssets(context: Application, assetName: String): Uri {
        val inputStream = context.assets.open(assetName)
        return specificStorage(context).saveOther(assetName, inputStream)
    }
}
