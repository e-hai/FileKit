package com.kit.file.functions

import android.graphics.Bitmap
import android.net.Uri
import com.kit.file.model.AudioType
import com.kit.file.model.ImageType
import com.kit.file.model.MediaStoreData
import com.kit.file.model.VideoType
import java.io.InputStream

/**
 * 存取文件的操作类接口。定义了用于处理不同类型文件的创建、保存和查询方法。
 * 该接口通常用于管理应用内或外部存储的文件操作。
 */
interface Storage {

    /**
     * 创建一个图片文件，并返回其URI。
     *
     * @param fileName 图片文件的名称
     * @param mimeType 图片文件的MIME类型，默认为JPEG类型
     * @return 创建的图片文件的URI
     */
    fun createPicture(fileName: String, mimeType: ImageType = ImageType.IMAGE_JPEG): Uri

    /**
     * 创建一个音乐文件，并返回其URI。
     *
     * @param fileName 音乐文件的名称
     * @param mimeType 音乐文件的MIME类型，默认为AAC类型
     * @return 创建的音乐文件的URI
     */
    fun createMusic(fileName: String, mimeType: AudioType = AudioType.AUDIO_AAC): Uri

    /**
     * 创建一个视频文件，并返回其URI。
     *
     * @param fileName 视频文件的名称
     * @param mimeType 视频文件的MIME类型，默认为MPEG类型
     * @return 创建的视频文件的URI
     */
    fun createMovie(fileName: String, mimeType: VideoType = VideoType.VIDEO_MPEG): Uri

    /**
     * 创建一个其他类型的文件，并返回其URI。
     *
     * @param fileName 其他类型文件的名称
     * @return 创建的其他文件的URI
     */
    fun createOther(fileName: String): Uri

    /**
     * 保存一张图片文件，返回该图片文件的URI。
     *
     * @param fileName 图片文件的名称
     * @param bitmap 要保存的图片数据
     * @return 保存的图片文件的URI
     */
    fun savePicture(fileName: String, bitmap: Bitmap): Uri

    /**
     * 使用输入流保存图片文件，返回保存后图片的URI。
     *
     * @param fileName 图片文件的名称
     * @param inputStream 输入流，其中包含要保存的图片数据
     * @return 保存的图片文件的URI
     */
    fun savePicture(fileName: String, inputStream: InputStream): Uri

    /**
     * 保存一部电影文件，返回该电影文件的URI。
     *
     * @param fileName 电影文件的名称
     * @param inputStream 输入流，其中包含要保存的电影数据
     * @return 保存的电影文件的URI
     */
    fun saveMovie(fileName: String, inputStream: InputStream): Uri

    /**
     * 保存一段音乐文件，返回该音乐文件的URI。
     *
     * @param fileName 音乐文件的名称
     * @param inputStream 输入流，其中包含要保存的音乐数据
     * @return 保存的音乐文件的URI
     */
    fun saveMusic(fileName: String, inputStream: InputStream): Uri

    /**
     * 保存一个其他类型的文件，返回该文件的URI。
     *
     * @param fileName 其他类型文件的名称
     * @param inputStream 输入流，其中包含要保存的文件数据
     * @return 保存的文件的URI
     */
    fun saveOther(fileName: String, inputStream: InputStream): Uri

    /**
     * 查询图片文件，返回指定偏移量和限制数量的图片文件列表。
     *
     * @param offset 查询的起始位置
     * @param limit 查询的数量限制
     * @return 查询到的图片文件列表
     */
    fun queryPicture(offset: Int, limit: Int): List<MediaStoreData>

    /**
     * 查询视频文件，返回指定偏移量和限制数量的视频文件列表。
     *
     * @param offset 查询的起始位置
     * @param limit 查询的数量限制
     * @return 查询到的视频文件列表
     */
    fun queryMovie(offset: Int, limit: Int): List<MediaStoreData>

    /**
     * 查询音乐文件，返回指定偏移量和限制数量的音乐文件列表。
     *
     * @param offset 查询的起始位置
     * @param limit 查询的数量限制
     * @return 查询到的音乐文件列表
     */
    fun queryMusic(offset: Int, limit: Int): List<MediaStoreData>

    /**
     * 查询其他类型的文件，返回指定偏移量和限制数量的文件列表。
     *
     * @param offset 查询的起始位置
     * @param limit 查询的数量限制
     * @return 查询到的其他类型的文件列表
     */
    fun queryOther(offset: Int, limit: Int): List<MediaStoreData>
}
