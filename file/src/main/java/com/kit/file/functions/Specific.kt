package com.kit.file.functions

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import com.kit.file.model.AudioType
import com.kit.file.model.ImageType
import com.kit.file.model.MediaStoreData
import com.kit.file.model.VideoType
import java.io.*
import java.lang.Exception
import java.util.*

/**
 * 内部存储-存取文件
 * 该类负责管理应用的内部存储（包括缓存目录和文件目录）中文件的创建、保存和查询操作。
 * 通过提供标准化的方法管理应用内部存储的文件。
 */
internal class Specific constructor(
    private val context: Application,
    private val isCache: Boolean = false
) : Storage {

    // 日志标签
    companion object {
        const val TAG = "Specific"
    }

    /**
     * 获取存储目录
     * 根据 `isCache` 的值决定使用缓存目录还是文件目录。
     *
     * @param path 目录路径（如：Environment.DIRECTORY_PICTURES）
     * @return 返回一个目录文件对象
     */
    private fun getDir(path: String): File {
        val dir = if (isCache) File(context.cacheDir, path) else File(context.filesDir, path)
        dir.mkdirs()  // 如果目录不存在，则创建
        return dir
    }

    /**
     * 创建一个新文件，如果文件已存在则先删除
     *
     * @param fileName 文件名
     * @param path 文件所在的目录路径
     * @return 返回新创建文件的 URI，失败时返回 Uri.EMPTY
     */
    private fun createNewFile(fileName: String, path: String): Uri {
        val file = File(getDir(path), fileName)
        if (file.exists()) {
            file.delete()  // 如果文件已存在，先删除
        }
        return try {
            file.createNewFile()  // 创建新文件
            Uri.fromFile(file)  // 返回文件的 URI
        } catch (e: Exception) {
            Log.e(TAG, "文件创建失败: $fileName", e)
            Uri.EMPTY  // 文件创建失败，返回空 URI
        }
    }

    /**
     * 保存文件的通用方法
     *
     * @param outputUri 输出文件的 URI
     * @param inputStream 输入流，其中包含要保存的数据
     * @return 返回保存成功后的 URI，失败时返回 Uri.EMPTY
     */
    private fun saveFile(outputUri: Uri, inputStream: InputStream): Uri {
        return try {
            context.contentResolver.openOutputStream(outputUri).use { outputStream ->
                val buffer = ByteArray(4096)  // 使用4KB的缓冲区
                var bytesRead: Int
                // 持续读取输入流并写入到输出流
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream?.write(buffer, 0, bytesRead)
                }
                outputStream?.flush()  // 确保数据完全写入
                Log.d(TAG, "文件保存成功: $outputUri")
                outputUri  // 返回保存后的 URI
            }
        } catch (e: IOException) {
            Log.e(TAG, "文件保存失败: $outputUri", e)
            Uri.EMPTY  // 保存失败，返回空 URI
        } finally {
            inputStream.close()  // 确保关闭输入流
        }
    }

    /**
     * 创建并保存图片文件
     *
     * @param fileName 图片文件的名称
     * @param mimeType 图片的 MIME 类型（默认为 PNG）
     * @return 返回图片文件的 URI
     */
    override fun createPicture(fileName: String, mimeType: ImageType): Uri =
        createNewFile(fileName, Environment.DIRECTORY_PICTURES)

    /**
     * 创建并保存音乐文件
     *
     * @param fileName 音乐文件的名称
     * @param mimeType 音乐的 MIME 类型（默认为 AAC）
     * @return 返回音乐文件的 URI
     */
    override fun createMusic(fileName: String, mimeType: AudioType): Uri =
        createNewFile(fileName, Environment.DIRECTORY_MUSIC)

    /**
     * 创建并保存视频文件
     *
     * @param fileName 视频文件的名称
     * @param mimeType 视频的 MIME 类型（默认为 MPEG）
     * @return 返回视频文件的 URI
     */
    override fun createMovie(fileName: String, mimeType: VideoType): Uri =
        createNewFile(fileName, Environment.DIRECTORY_MOVIES)

    /**
     * 创建并保存其他类型的文件
     *
     * @param fileName 其他文件的名称
     * @return 返回其他类型文件的 URI
     */
    override fun createOther(fileName: String): Uri =
        createNewFile(fileName, Environment.DIRECTORY_DOWNLOADS)

    /**
     * 保存图片（通过 Bitmap）
     *
     * @param fileName 图片文件的名称
     * @param bitmap 要保存的图片数据
     * @return 返回保存的图片文件的 URI
     */
    override fun savePicture(fileName: String, bitmap: Bitmap): Uri {
        val pictureUri = createPicture(fileName)
        return try {
            context.contentResolver.openOutputStream(pictureUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)  // 保存为 PNG 格式
            }
            pictureUri  // 返回保存后的 URI
        } catch (e: Exception) {
            Log.e(TAG, "保存图片失败: $fileName", e)
            Uri.EMPTY  // 保存失败，返回空 URI
        }
    }

    /**
     * 保存图片（通过输入流）
     *
     * @param fileName 图片文件的名称
     * @param inputStream 输入流，包含要保存的图片数据
     * @return 返回保存后的图片文件 URI
     */
    override fun savePicture(fileName: String, inputStream: InputStream): Uri {
        val pictureUri = createPicture(fileName)
        return saveFile(pictureUri, inputStream)
    }

    /**
     * 保存音乐文件
     *
     * @param fileName 音乐文件的名称
     * @param inputStream 输入流，包含要保存的音乐数据
     * @return 返回保存后的音乐文件 URI
     */
    override fun saveMusic(fileName: String, inputStream: InputStream): Uri {
        val musicUri = createMusic(fileName)
        return saveFile(musicUri, inputStream)
    }

    /**
     * 保存视频文件
     *
     * @param fileName 视频文件的名称
     * @param inputStream 输入流，包含要保存的视频数据
     * @return 返回保存的视频文件 URI
     */
    override fun saveMovie(fileName: String, inputStream: InputStream): Uri {
        val movieUri = createMovie(fileName)
        return saveFile(movieUri, inputStream)
    }

    /**
     * 保存其他文件
     *
     * @param fileName 其他文件的名称
     * @param inputStream 输入流，包含要保存的文件数据
     * @return 返回保存后的其他文件 URI
     */
    override fun saveOther(fileName: String, inputStream: InputStream): Uri {
        val otherUri = createOther(fileName)
        return saveFile(otherUri, inputStream)
    }

    /**
     * 查询指定目录中的文件
     * 根据时间戳排序，并返回指定偏移量和数量限制的文件列表。
     *
     * @param dir 目标目录
     * @param offset 查询的起始位置
     * @param limit 查询的数量限制
     * @return 返回查询到的文件列表
     */
    private fun queryFiles(dir: File, offset: Int, limit: Int): List<MediaStoreData> {
        val fileList = dir.listFiles()?.toList()?.sortedBy { it.lastModified() } ?: emptyList()
        val maxIndex = fileList.size - 1
        val start = offset.coerceAtMost(maxIndex)  // 确保偏移量不超过最大索引
        val end = (offset + limit).coerceAtMost(maxIndex + 1)  // 确保查询数量不超过最大索引
        return fileList.subList(start, end).map {
            MediaStoreData(
                System.currentTimeMillis(),  // 当前时间戳
                it.name,  // 文件名
                Date(it.lastModified()),  // 文件的最后修改时间
                it.toUri()  // 文件的 URI
            )
        }
    }

    /**
     * 查询图片文件
     *
     * @param offset 查询的起始位置
     * @param limit 查询的数量限制
     * @return 返回查询到的图片文件列表
     */
    override fun queryPicture(offset: Int, limit: Int): List<MediaStoreData> =
        queryFiles(getDir(Environment.DIRECTORY_PICTURES), offset, limit)

    /**
     * 查询视频文件
     *
     * @param offset 查询的起始位置
     * @param limit 查询的数量限制
     * @return 返回查询到的视频文件列表
     */
    override fun queryMovie(offset: Int, limit: Int): List<MediaStoreData> =
        queryFiles(getDir(Environment.DIRECTORY_MOVIES), offset, limit)

    /**
     * 查询音乐文件
     *
     * @param offset 查询的起始位置
     * @param limit 查询的数量限制
     * @return 返回查询到的音乐文件列表
     */
    override fun queryMusic(offset: Int, limit: Int): List<MediaStoreData> =
        queryFiles(getDir(Environment.DIRECTORY_MUSIC), offset, limit)

    /**
     * 查询其他类型文件
     *
     * @param offset 查询的起始位置
     * @param limit 查询的数量限制
     * @return 返回查询到的其他类型文件列表
     */
    override fun queryOther(offset: Int, limit: Int): List<MediaStoreData> =
        queryFiles(getDir(Environment.DIRECTORY_DOWNLOADS), offset, limit)
}
