package com.kit.file.sample

import android.net.Uri
import android.util.Log
import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kit.file.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader


class MainViewModel : ViewModel() {
    private val storage = FileManager.specificStorage(App.application)

    var saveFile: Uri? = null

    data class Position(val x: Float, val y: Float, val z: Float)

    fun init() {
        viewModelScope.launch {
            flow {
                val inputStream = App.application.resources.openRawResource(R.raw.position)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonArray = JSONArray()
                var jsonItem = JSONObject()
                var line: String?
                var index = 0
                while (reader.readLine().also { line = it } != null) {
                    when (index) {
                        3 -> jsonItem.put("x", line)
                        4 -> jsonItem.put("y", line)
                    }

                    index++
                    if (index == 5) {
                        index = 0
                        jsonArray.put(jsonItem)
                        jsonItem = JSONObject()
                    }
                }
                reader.close()
                inputStream.close()
                val result = JSONObject().also {
                    it.put("position", jsonArray)
                }.toString()
                emit(result)
            }.flowOn(Dispatchers.IO)
                .onEach {
                    Log.d(TAG, it)
                }
                .collect()
        }
    }

    fun add(fileName: String) {
        viewModelScope.launch {
            flow {
                val uri = storage
                    .createPicture(fileName)
                emit(uri)
            }.flowOn(Dispatchers.IO)
                .collectLatest {
                    Log.d(TAG, "add finish=${it.path}")
                    saveFile = it
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
                val uri = storage.savePicture(fileName, inputStream)
                emit(uri)
            }.flowOn(Dispatchers.IO).collectLatest {
                Log.d(TAG, "save finish=$it")
                saveFile = it
            }
        }
    }


    companion object {
        const val TAG = "MainViewModel"
    }
}