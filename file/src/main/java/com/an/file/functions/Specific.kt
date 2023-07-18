package com.an.file.functions

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import com.an.file.model.AudioType
import com.an.file.model.ImageType
import com.an.file.model.MediaStoreData
import com.an.file.model.VideoType
import java.io.*
import java.lang.Exception
import java.util.*


internal class Specific constructor(
    private val context: Application,
    private val isCache: Boolean = false
) : Storage {


    //删除已存在的文件，并新建一个文件，
    private fun createNewFile(
        fileName: String,
        path: String
    ): Uri {
        val file = File(getDir(path), fileName)
        if (file.exists()) {
            file.delete()
        }
        return try {
            file.createNewFile()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Uri.EMPTY
        }
    }

    private fun getDir(path: String): File {
        val dir = if (isCache) File(context.cacheDir, path) else File(context.filesDir, path)
        dir.mkdirs()
        return dir
    }

    //放置用户可用图片的标准目录
    override fun createPicture(fileName: String, mimeType: ImageType) =
        createNewFile(fileName, Environment.DIRECTORY_PICTURES)

    //放置用户可用音乐的标准目录
    override fun createMusic(fileName: String, mimeType: AudioType) =
        createNewFile(fileName, Environment.DIRECTORY_MUSIC)


    //放置用户可用视频的标准目录
    override fun createMovie(fileName: String, mimeType: VideoType) =
        createNewFile(fileName, Environment.DIRECTORY_MOVIES)


    //存储文档或其他文件的标准目录
    override fun createOther(fileName: String) =
        createNewFile(fileName, Environment.DIRECTORY_DOWNLOADS)


    private fun saveFile(outputUri: Uri, inputStream: InputStream): Uri {
        var outputStream: OutputStream? = null
        return try {
            val fileReader = ByteArray(4096)
            outputStream = context.contentResolver.openOutputStream(outputUri)
            while (true) {
                val read = inputStream.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream?.write(fileReader, 0, read)
            }
            outputStream?.flush()
            Log.d(TAG, "保存成功=${outputUri}")
            outputUri
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

    override fun savePicture(fileName: String, inputStream: InputStream): Uri {
        createPicture(fileName)
            .also {
                return saveFile(it, inputStream)
            }
    }


    override fun saveMusic(fileName: String, inputStream: InputStream): Uri {
        createMusic(fileName)
            .also {
                return saveFile(it, inputStream)
            }
    }

    override fun saveMovie(fileName: String, inputStream: InputStream): Uri {
        createMovie(fileName)
            .also {
                return saveFile(it, inputStream)
            }
    }

    override fun saveOther(fileName: String, inputStream: InputStream): Uri {
        createOther(fileName)
            .also {
                return saveFile(it, inputStream)
            }
    }

    private fun queryFiles(dir: File, offset: Int, limit: Int): List<MediaStoreData> {
        //按照时间排序，并取列表的一部分
        val fileList = dir.listFiles()?.toList()?.sortedBy {
            it.lastModified()
        }?.let {
            val maxIndex = it.size - 1
            val start = if (offset > maxIndex) maxIndex else offset
            val end = if (limit > maxIndex) maxIndex else limit
            it.subList(start, end)
        } ?: emptyList()

        val mediaList = mutableListOf<MediaStoreData>()
        fileList.forEach {
            mediaList.add(
                MediaStoreData(
                    System.currentTimeMillis(),
                    it.name,
                    Date(it.lastModified()),
                    it.toUri()
                )
            )
        }
        return mediaList
    }

    override fun queryPicture(offset: Int, limit: Int): List<MediaStoreData> {
        val dir = getDir(Environment.DIRECTORY_PICTURES)
        return queryFiles(dir, offset, limit)
    }

    override fun queryMovie(offset: Int, limit: Int): List<MediaStoreData> {
        val dir = getDir(Environment.DIRECTORY_MOVIES)
        return queryFiles(dir, offset, limit)
    }

    override fun queryMusic(offset: Int, limit: Int): List<MediaStoreData> {
        val dir = getDir(Environment.DIRECTORY_MUSIC)
        return queryFiles(dir, offset, limit)
    }

    override fun queryOther(offset: Int, limit: Int): List<MediaStoreData> {
        val dir = getDir(Environment.DIRECTORY_DOWNLOADS)
        return queryFiles(dir, offset, limit)
    }


    companion object {
        const val TAG = "Specific"
    }
}