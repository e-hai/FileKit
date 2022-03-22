package com.anfile

import android.net.Uri
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.file.FileManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {


    fun add(fileName: String) {
        viewModelScope.launch {
            flow {
                val uri = FileManager.specificStorage(App.application)
                    .createPicture(fileName)
                emit(uri)
            }.collectLatest {
                Log.d(TAG, "add finish=${it.path}")
            }
        }
    }

    fun query() {
        viewModelScope.launch {
            flow {
                val uri = FileManager.specificStorage(App.application)
                    .queryPicture(0, Int.MAX_VALUE)
                emit(uri)
            }.collectLatest {
                Log.d(TAG, "query finish=$it")
            }
        }
    }

    fun save(fileName: String, @RawRes res: Int) {
        viewModelScope.launch {
            flow {
                val inputStream = App.application.resources.openRawResource(res)
                val uri = FileManager.specificStorage(App.application)
                    .savePicture(fileName, inputStream)
                emit(uri)
            }.collectLatest {
                Log.d(TAG, "save finish=$it")
            }
        }
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}