package com.an.file.functions

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
import com.an.file.FileManager.TAG
import com.an.file.model.*
import java.io.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("InlinedApi")
internal class Shared constructor(
    private val context: Application
) : Storage {

    //放置用户可用图片的公共目录
    override fun createPicture(fileName: String, mimeType: ImageType): Uri {
        val resolver = context.contentResolver
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

        val details = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType.type)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis()) //拍摄日期
        }
        return resolver.insert(collection, details) ?: Uri.EMPTY
    }


    //放置用户可用音乐的公共目录
    override fun createMusic(fileName: String, mimeType: AudioType): Uri {
        val resolver = context.contentResolver
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val details = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Audio.Media.MIME_TYPE, mimeType.type)
            put(MediaStore.Audio.Media.DATE_TAKEN, System.currentTimeMillis()) //拍摄日期
        }
        return resolver.insert(collection, details) ?: Uri.EMPTY
    }

    //放置用户可用视频的公共目录
    override fun createMovie(fileName: String, mimeType: VideoType): Uri {
        val resolver = context.contentResolver
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

        val details = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType.type)
            put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis()) //拍摄日期
        }
        return resolver.insert(collection, details) ?: Uri.EMPTY
    }


    //放置用户其他文件的公共目录
    override fun createOther(fileName: String): Uri {
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            else
                MediaStore.VOLUME_EXTERNAL
        )
        val details = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.DATE_TAKEN, System.currentTimeMillis()) //拍摄日期

        }
        return resolver.insert(collection, details) ?: Uri.EMPTY
    }


    private fun saveFile(saveUri: Uri, inputStream: InputStream): Uri {
        var outputStream: OutputStream? = null
        return try {
            val fileReader = ByteArray(4096)
            outputStream = context.contentResolver.openOutputStream(saveUri)
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
            e.printStackTrace()
            Uri.EMPTY
        } finally {
            inputStream.close()
            outputStream?.close()
        }
    }

    override fun savePicture(fileName: String, bitmap: Bitmap): Uri {
        val picture = createPicture(fileName)
        var outputStream: OutputStream? = null
        return try {
            outputStream = context.contentResolver.openOutputStream(picture)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            picture
        } catch (e: Exception) {
            e.printStackTrace()
            Uri.EMPTY
        } finally {
            outputStream?.flush()
            outputStream?.close()
        }
    }

    override fun savePicture(fileName: String, inputStream: InputStream) = createPicture(fileName)
        .also {
            return if (it.path.isNullOrEmpty()) {
                it
            } else {
                saveFile(it, inputStream)
            }
        }

    override fun saveMovie(fileName: String, inputStream: InputStream) = createMovie(fileName)
        .also {
            return if (it.path.isNullOrEmpty()) {
                it
            } else {
                saveFile(it, inputStream)
            }
        }


    override fun saveMusic(fileName: String, inputStream: InputStream) = createMusic(fileName)
        .also {
            return if (it.path.isNullOrEmpty()) {
                it
            } else {
                saveFile(it, inputStream)
            }
        }

    override fun saveOther(fileName: String, inputStream: InputStream) = createOther(fileName)
        .also {
            return if (it.path.isNullOrEmpty()) {
                it
            } else {
                saveFile(it, inputStream)
            }
        }


    private fun queryFiles(
        uri: Uri,
        projection: Array<String>,
        args: Bundle
    ): Cursor? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            args.putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            )
            args.putStringArray(
                ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(MediaStore.Files.FileColumns.DATE_ADDED)
            )
            context.contentResolver.query(
                uri,
                projection,
                args,
                null
            )
        } else {
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DES"
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )
        }
    }

    override fun queryPicture(
        offset: Int,
        limit: Int
    ): List<MediaStoreData> {
        val medias = mutableListOf<MediaStoreData>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )
        val args = Bundle()
        args.putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
        args.putInt(ContentResolver.QUERY_ARG_LIMIT, limit)

        queryFiles(uri, projection, args)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateModifiedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            Log.i(TAG, "Found ${cursor.count} medias")
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateModified =
                    Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
                val displayName = cursor.getString(displayNameColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val media = MediaStoreData(id, displayName, dateModified, contentUri)
                medias += media
                if (medias.size > limit) {
                    Log.i(TAG, "count over limit $limit")
                    break
                }
            }
        }

        Log.i(TAG, "Get ${medias.size} medias")
        return medias
    }

    override fun queryMovie(
        offset: Int,
        limit: Int
    ): List<MediaStoreData> {
        val medias = mutableListOf<MediaStoreData>()
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED
        )
        val args = Bundle()
        args.putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
        args.putInt(ContentResolver.QUERY_ARG_LIMIT, limit)

        queryFiles(uri, projection, args)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val dateModifiedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

            Log.i(TAG, "Found ${cursor.count} medias")
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateModified =
                    Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
                val displayName = cursor.getString(displayNameColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val media = MediaStoreData(id, displayName, dateModified, contentUri)
                medias += media
                if (medias.size > limit) {
                    Log.i(TAG, "count over limit $limit")
                    break
                }
            }
        }
        Log.i(TAG, "Get ${medias.size} medias")
        return medias
    }


    override fun queryMusic(
        offset: Int,
        limit: Int
    ): List<MediaStoreData> {
        val medias = mutableListOf<MediaStoreData>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val args = Bundle()
        args.putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
        args.putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
        queryFiles(uri, projection, args)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val dateModifiedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

            Log.i(TAG, "Found ${cursor.count} medias")
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateModified =
                    Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
                val displayName = cursor.getString(displayNameColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val media = MediaStoreData(id, displayName, dateModified, contentUri)
                medias += media
                if (medias.size > limit) {
                    Log.i(TAG, "count over limit $limit")
                    break
                }
            }
        }
        Log.i(TAG, "Get ${medias.size} medias")
        return medias
    }

    override fun queryOther(offset: Int, limit: Int): List<MediaStoreData> {
        val medias = mutableListOf<MediaStoreData>()
        val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.DATE_ADDED
        )
        val args = Bundle()
        args.putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
        args.putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
        queryFiles(uri, projection, args)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            val dateModifiedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATE_ADDED)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)

            Log.i(TAG, "Found ${cursor.count} medias")
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val dateModified =
                    Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
                val displayName = cursor.getString(displayNameColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val media = MediaStoreData(id, displayName, dateModified, contentUri)
                medias += media
                if (medias.size > limit) {
                    Log.i(TAG, "count over limit $limit")
                    break
                }
            }
        }
        Log.i(TAG, "Get ${medias.size} medias")
        return medias
    }


}