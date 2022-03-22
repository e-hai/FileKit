package com.an.file.functions

import android.graphics.Bitmap
import android.net.Uri
import com.an.file.model.MediaStoreData
import java.io.InputStream

interface Storage {

    fun createPicture(fileName: String): Uri

    fun createMusic(fileName: String): Uri

    fun createMovie(fileName: String): Uri

    fun createOther(fileName: String): Uri

    fun savePicture(fileName: String, bitmap: Bitmap): Uri

    fun savePicture(fileName: String, inputStream: InputStream): Uri

    fun saveMovie(fileName: String, inputStream: InputStream): Uri

    fun saveMusic(fileName: String, inputStream: InputStream): Uri

    fun saveOther(fileName: String, inputStream: InputStream): Uri

    fun queryPicture(offset: Int, limit: Int): List<MediaStoreData>

    fun queryMovie(offset: Int, limit: Int): List<MediaStoreData>

    fun queryMusic(offset: Int, limit: Int): List<MediaStoreData>
}