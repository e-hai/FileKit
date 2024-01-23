package com.anfile.sample

import android.util.Log
import androidx.annotation.RawRes
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.file.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel : ViewModel() {
    private val storage = FileManager.sharedStorage(App.application)

     var saveFile: File? = null

    fun add(fileName: String) {
        viewModelScope.launch {
            flow {
                val uri = storage
                    .createPicture(fileName)
                emit(uri)
            }.flowOn(Dispatchers.IO)
                .collectLatest {
                    Log.d(TAG, "add finish=${it.path}")
                }
        }
    }

    fun query() {
        viewModelScope.launch {
            flow {
                val uri = storage
                    .queryPicture(0, Int.MAX_VALUE)
                emit(uri)
            }.flowOn(Dispatchers.IO).collectLatest {
                Log.d(TAG, "query finish=$it")
            }
        }
    }

    fun save(fileName: String, @RawRes res: Int) {
        viewModelScope.launch {
            flow {
                val inputStream = App.application.resources.openRawResource(res)
                val uri = storage
                    .savePicture(fileName, inputStream)
                emit(uri)
            }.flowOn(Dispatchers.IO).collectLatest {
                Log.d(TAG, "save finish=$it")
            }
        }
    }


    companion object {
        const val TAG = "MainViewModel"
    }
}