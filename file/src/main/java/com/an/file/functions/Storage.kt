package com.an.file.functions

import android.graphics.Bitmap
import android.net.Uri
import com.an.file.model.MediaStoreData
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface Storage {

    fun createPicture(fileName: String): Flow<Uri>

    fun createMusic(fileName: String): Flow<Uri>

    fun createMovie(fileName: String): Flow<Uri>

    fun createOther(fileName: String): Flow<Uri>

    fun savePicture(fileName: String, bitmap: Bitmap): Flow<Uri>

    fun savePicture(fileName: String, inputStream: InputStream): Flow<Uri>

    fun saveMovie(fileName: String, inputStream: InputStream): Flow<Uri>

    fun saveMusic(fileName: String, inputStream: InputStream): Flow<Uri>

    fun saveOther(fileName: String, inputStream: InputStream): Flow<Uri>

    fun queryPicture(offset: Int, limit: Int): Flow<List<MediaStoreData>>

    fun queryMovie(offset: Int, limit: Int): Flow<List<MediaStoreData>>

    fun queryMusic(offset: Int, limit: Int): Flow<List<MediaStoreData>>
}