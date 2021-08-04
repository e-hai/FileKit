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
import com.an.file.model.MediaStoreData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("InlinedApi")
class Shared internal constructor(
    private val context: Application) : Storage {

    //放置用户可用图片的公共目录
    override fun createPicture(fileName: String): Uri? {
        val resolver = context.contentResolver
        val collection = MediaStore.Images.Media.getContentUri(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            else
                MediaStore.VOLUME_EXTERNAL
        )
        val details = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        }
        return resolver.insert(collection, details)
    }


    //放置用户可用音乐的公共目录
    override fun createMusic(fileName: String): Uri? {
        val resolver = context.contentResolver
        val collection = MediaStore.Audio.Media.getContentUri(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            else
                MediaStore.VOLUME_EXTERNAL
        )
        val details = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
        }
        return resolver.insert(collection, details)
    }

    //放置用户可用视频的公共目录
    override fun createMovie(fileName: String): Uri? {
        val resolver = context.contentResolver
        val collection = MediaStore.Video.Media.getContentUri(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            else
                MediaStore.VOLUME_EXTERNAL
        )
        val details = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
        }
        return resolver.insert(collection, details)
    }


    //放置用户其他文件的公共目录
    override fun createOther(fileName: String): Uri? {
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            else
                MediaStore.VOLUME_EXTERNAL
        )
        val details = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        }
        return resolver.insert(collection, details)
    }


    private fun saveFile(saveUri: Uri, inputStream: InputStream): Uri? {
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
            null
        } finally {
            inputStream.close()
            outputStream?.close()
        }
    }

    override fun savePicture(fileName: String, bitmap: Bitmap): Uri? {
        var outputStream: OutputStream? = null
        return try {
            val saveUri = createPicture(fileName) ?: return null
            outputStream = context.contentResolver.openOutputStream(saveUri)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            saveUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            outputStream?.flush()
            outputStream?.close()
        }
    }

    override fun savePicture(
        fileName: String,
        inputStream: InputStream
    ): Uri? {
        val saveUri = createPicture(fileName) ?: return null
        return saveFile(saveUri, inputStream)
    }

    override fun saveMovie(fileName: String, inputStream: InputStream): Uri? {
        val saveUri = createMovie(fileName) ?: return null
        return saveFile(saveUri, inputStream)
    }

    override fun saveMusic(fileName: String, inputStream: InputStream): Uri? {
        val saveUri = createMusic(fileName) ?: return null
        return saveFile(saveUri, inputStream)
    }

    override fun saveOther(fileName: String, inputStream: InputStream): Uri? {
        val saveUri = createOther(fileName) ?: return null
        return saveFile(saveUri, inputStream)
    }

    private fun queryMedia(
        uri: Uri,
        projection: Array<String>,
        args: Bundle
    ): Cursor? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.contentResolver.query(
                uri,
                projection,
                args,
                null
            )
        } else {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )
        }
    }

    override suspend fun queryPicture(
        offset: Int,
        limit: Int
    ): List<MediaStoreData> {
        val medias = mutableListOf<MediaStoreData>()
        withContext(Dispatchers.IO) {
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
            )
            val args = Bundle()
            args.putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
            args.putInt(ContentResolver.QUERY_ARG_LIMIT, limit)

            queryMedia(uri, projection, args)?.use { cursor ->
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
        }

        Log.i(TAG, "Get ${medias.size} medias")
        return medias
    }

    override suspend fun queryMovie(
        offset: Int,
        limit: Int
    ): List<MediaStoreData> {
        val medias = mutableListOf<MediaStoreData>()

        withContext(Dispatchers.IO) {
            val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED
            )
            val args = Bundle()
            args.putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
            args.putInt(ContentResolver.QUERY_ARG_LIMIT, limit)

            queryMedia(uri, projection, args)?.use { cursor ->
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
        }

        Log.i(TAG, "Get ${medias.size} medias")
        return medias
    }


    override suspend fun queryMusic(
        offset: Int,
        limit: Int
    ): List<MediaStoreData> {
        val medias = mutableListOf<MediaStoreData>()

        withContext(Dispatchers.IO) {

            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATE_ADDED
            )
            val args = Bundle()
            args.putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
            args.putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
            queryMedia(uri, projection, args)?.use { cursor ->
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
        }

        Log.i(TAG, "Get ${medias.size} medias")
        return medias
    }

    companion object {
        val TAG = Shared::class.simpleName
    }
}