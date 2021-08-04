package com.an.file.functions

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.net.toFile
import com.an.file.model.MediaStoreData
import java.io.*
import java.lang.Exception


class Specific internal constructor(
    private val context: Application,
    private val isCache: Boolean = false
) : Storage {


    //删除已存在的文件，并新建一个文件，
    private fun createNewFile(
        fileName: String,
        type: String
    ): Uri? {
        val file = if (isExternalStorageWritable()) {
            val dir = if (isCache) context.externalCacheDir else context.getExternalFilesDir(type)
            File(dir, fileName)
        } else {
            val dir = if (isCache) File(context.cacheDir, type) else File(context.filesDir, type)
            dir.mkdirs()
            File(dir, fileName)
        }
        if (file.exists()) {
            file.delete()
        }
        return try {
            file.createNewFile()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    //放置用户可用图片的标准目录
    override fun createPicture(fileName: String): Uri? {
        return createNewFile(fileName, Environment.DIRECTORY_PICTURES)
    }

    //放置用户可用音乐的标准目录
    override fun createMusic(fileName: String): Uri? {
        return createNewFile(fileName, Environment.DIRECTORY_MUSIC)
    }

    //放置用户可用视频的标准目录
    override fun createMovie(fileName: String): Uri? {
        return createNewFile(fileName, Environment.DIRECTORY_MOVIES)
    }

    //存储文档或其他文件的标准目录
    override fun createOther(fileName: String): Uri? {
        return createNewFile(fileName, Environment.DIRECTORY_DOCUMENTS)
    }

    private fun saveFile(outputUri: Uri, inputStream: InputStream): Uri? {
        var outputStream: OutputStream? = null
        return try {
            val fileReader = ByteArray(4096)
            outputStream = FileOutputStream(outputUri.toFile())
            while (true) {
                val read = inputStream.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream.write(fileReader, 0, read)
            }
            outputStream.flush()
            outputUri
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            inputStream.close()
            outputStream?.close()
        }
    }


    override fun savePicture(fileName: String, bitmap: Bitmap): Uri? {
        var outputStream: FileOutputStream? = null
        return try {
            val outputUri = createPicture(fileName)
            outputStream = FileOutputStream(outputUri?.toFile())
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            outputStream?.flush()
            outputStream?.close()
        }
    }

    override fun savePicture(fileName: String, inputStream: InputStream): Uri? {
        createPicture(fileName)?.let {
            return saveFile(it, inputStream)
        }
        return null
    }

    override fun saveMusic(fileName: String, inputStream: InputStream): Uri? {
        createMusic(fileName)?.let {
            return saveFile(it, inputStream)
        }
        return null
    }

    override fun saveMovie(fileName: String, inputStream: InputStream): Uri? {
        createMovie(fileName)?.let {
            return saveFile(it, inputStream)
        }
        return null
    }

    override fun saveOther(fileName: String, inputStream: InputStream): Uri? {
        createOther(fileName)?.let {
            return saveFile(it, inputStream)
        }
        return null
    }

    override suspend fun queryPicture(offset: Int, limit: Int): List<MediaStoreData> {
        return emptyList()
    }

    override suspend fun queryMovie(offset: Int, limit: Int): List<MediaStoreData> {
        return emptyList()
    }

    override suspend fun queryMusic(offset: Int, limit: Int): List<MediaStoreData> {
        return emptyList()
    }

    /**
     * Checks if a volume containing external storage is available for read and write.
     * **/
    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

}