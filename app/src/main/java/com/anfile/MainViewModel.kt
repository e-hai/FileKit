package com.anfile

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.file.FileManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {


    fun add(fileName: String) {
        viewModelScope.launch {
            FileManager.specificStorage(App.application)
                .createPicture(fileName)
                .collectLatest {
                    Log.d(TAG, "add finish=${it.path}")
                }
        }
    }

    fun query() {
        viewModelScope.launch {
            FileManager.specificStorage(App.application)
                .queryPicture(0, Int.MAX_VALUE)
                .collectLatest {
                    Log.d(TAG, "query finish=$it")
                }
        }
    }

    fun save(fileName: String, @RawRes res: Int) {
        viewModelScope.launch {
            val inputStream = App.application.resources.openRawResource(res)
            FileManager.specificStorage(App.application)
                .savePicture(fileName, inputStream)
                .collectLatest {
                    Log.d(TAG, "save finish=$it")
                }
        }
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}