package com.kit.file.functions

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.kit.file.FileManager.TAG
import com.kit.file.model.*
import java.io.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 外部存储-文件存取
 * 提供公共存储目录下的文件操作，包括创建文件、保存文件、查询文件等。
 */
@SuppressLint("InlinedApi")
internal class Shared constructor(
    private val context: Application
) : Storage {

    // 获取 ContentResolver
    private val resolver = context.contentResolver

    /**
     * 创建媒体文件（图片、音频、视频、下载文件）
     * 根据文件类型将文件保存到公共存储的不同目录。
     */
    private fun createMediaFile(collectionUri: Uri, fileName: String, mimeType: String): Uri {
        // 设置文件的相关信息
        val details = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName) // 文件名称
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType) // MIME 类型
            put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis()) // 文件创建时间
        }
        // 插入文件并返回其 URI
        return resolver.insert(collectionUri, details) ?: Uri.EMPTY
    }

    /**
     * 获取指定媒体类型的公共存储 URI
     * 根据系统版本和文件类型返回相应的 URI。
     */
    private fun getMediaCollectionUri(type: MediaType): Uri {
        return when (type) {
            is MediaType.Image -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) // Android 10及以上版本
            else MediaStore.Images.Media.EXTERNAL_CONTENT_URI // 旧版本

            is MediaType.Audio -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            is MediaType.Video -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            is MediaType.Download -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            else MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }
    }

    // 创建图片文件的公共目录
    override fun createPicture(fileName: String, mimeType: ImageType): Uri =
        createMediaFile(getMediaCollectionUri(MediaType.Image), fileName, mimeType.type)

    // 创建音乐文件的公共目录
    override fun createMusic(fileName: String, mimeType: AudioType): Uri =
        createMediaFile(getMediaCollectionUri(MediaType.Audio), fileName, mimeType.type)

    // 创建视频文件的公共目录
    override fun createMovie(fileName: String, mimeType: VideoType): Uri =
        createMediaFile(getMediaCollectionUri(MediaType.Video), fileName, mimeType.type)

    // 创建其他文件的公共目录
    override fun createOther(fileName: String): Uri =
        createMediaFile(
            getMediaCollectionUri(MediaType.Download),
            fileName,
            "application/octet-stream"
        )

    /**
     * 通用的保存文件方法
     * 将输入流中的数据保存到指定 URI（如：图片、音频、视频、下载文件）。
     */
    private fun saveFile(saveUri: Uri, inputStream: InputStream): Uri {
        var outputStream: OutputStream? = null
        return try {
            val fileReader = ByteArray(4096)
            // 打开输出流并写入数据
            outputStream = resolver.openOutputStream(saveUri)
            while (true) {
                val read = inputStream.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream?.write(fileReader, 0, read)
            }
            outputStream?.flush()
            saveUri
        } catch (e: IOException) {
            e.printStackTrace() // 捕获并打印错误
            Uri.EMPTY
        } finally {
            inputStream.close() // 关闭输入流
            outputStream?.close() // 关闭输出流
        }
    }

    // 保存图片（Bitmap）
    override fun savePicture(fileName: String, bitmap: Bitmap): Uri {
        val pictureUri = createPicture(fileName, ImageType.IMAGE_PNG) // 创建图片文件
        var outputStream: OutputStream? = null
        return try {
            outputStream = resolver.openOutputStream(pictureUri)?.apply {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, this) // 压缩并保存图片
            }
            pictureUri
        } catch (e: Exception) {
            e.printStackTrace() // 捕获并打印错误
            Uri.EMPTY
        } finally {
            outputStream?.flush() // 确保输出流刷新
            outputStream?.close() // 关闭输出流
        }
    }

    // 保存图片（InputStream）
    override fun savePicture(fileName: String, inputStream: InputStream): Uri =
        saveFile(createPicture(fileName), inputStream)

    // 保存视频文件（InputStream）
    override fun saveMovie(fileName: String, inputStream: InputStream): Uri =
        saveFile(createMovie(fileName), inputStream)

    // 保存音乐文件（InputStream）
    override fun saveMusic(fileName: String, inputStream: InputStream): Uri =
        saveFile(createMusic(fileName), inputStream)

    // 保存其他文件（InputStream）
    override fun saveOther(fileName: String, inputStream: InputStream): Uri =
        saveFile(createOther(fileName), inputStream)

    /**
     * 查询文件的通用方法
     * 使用 `ContentResolver` 根据指定 URI 和查询参数返回文件的 Cursor。
     */
    private fun queryFiles(uri: Uri, projection: Array<String>, args: Bundle?): Cursor? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0（Oreo）及以上，支持分页查询
            args?.putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            )
            args?.putStringArray(
                ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(MediaStore.Files.FileColumns.DATE_ADDED)
            )
            resolver.query(uri, projection, args, null)
        } else {
            // 旧版本使用排序字符串进行倒序排序
            resolver.query(
                uri,
                projection,
                null,
                null,
                "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
            )
        }
    }

    /**
     * 查询指定类型的媒体文件（图片、视频、音乐、其他文件）
     * @param uri 查询的 URI
     * @param projection 查询时需要的列
     * @param offset 查询的起始位置
     * @param limit 查询的数量限制
     * @return 查询到的文件列表
     */
    private fun queryMediaFiles(
        uri: Uri,
        projection: Array<String>,
        offset: Int,
        limit: Int
    ): List<MediaStoreData> {
        val args = Bundle().apply {
            putInt(ContentResolver.QUERY_ARG_OFFSET, offset) // 设置偏移量
            putInt(ContentResolver.QUERY_ARG_LIMIT, limit) // 设置限制数量
        }

        val medias = mutableListOf<MediaStoreData>()
        queryFiles(uri, projection, args)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)

            // 解析查询结果
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateColumn)))
                val displayName = cursor.getString(displayNameColumn)
                val contentUri = ContentUris.withAppendedId(uri, id) // 获取文件 URI
                medias.add(MediaStoreData(id, displayName, dateModified, contentUri))
            }
        }
        return medias
    }

    // 查询图片文件
    override fun queryPicture(offset: Int, limit: Int): List<MediaStoreData> =
        queryMediaFiles(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
            ),
            offset,
            limit
        )

    // 查询视频文件
    override fun queryMovie(offset: Int, limit: Int): List<MediaStoreData> =
        queryMediaFiles(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED
            ),
            offset,
            limit
        )

    // 查询音乐文件
    override fun queryMusic(offset: Int, limit: Int): List<MediaStoreData> =
        queryMediaFiles(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATE_ADDED
            ),
            offset,
            limit
        )

    // 查询其他文件
    override fun queryOther(offset: Int, limit: Int): List<MediaStoreData> =
        queryMediaFiles(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME,
                MediaStore.Downloads.DATE_ADDED
            ),
            offset,
            limit
        )

    /**
     * 支持的多媒体类型
     * 使用密封类表示不同类型的媒体文件。
     */
    sealed class MediaType {
        object Image : MediaType()
        object Audio : MediaType()
        object Video : MediaType()
        object Download : MediaType()
    }
}
