package com.anfile.sample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.viewModels
import com.an.file.FileManager
import com.an.file.functions.PermissionListener

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.add).setOnClickListener {
            clickAddFile()
        }
        findViewById<Button>(R.id.query).setOnClickListener {
            clickQueryFile()
        }
        findViewById<Button>(R.id.save).setOnClickListener {
            clickSaveFile()
        }
        findViewById<Button>(R.id.share).setOnClickListener {
            clickShareFile()
        }
        findViewById<Button>(R.id.pick_photo).setOnClickListener {
            clickPickPhoto()
        }
        findViewById<Button>(R.id.pick_video).setOnClickListener {
            clickPickVideo()
        }
    }

    private fun clickPickVideo() {
        FileManager.pickVideo(this, 1)
    }

    private fun clickPickPhoto() {
        FileManager.pickPhoto(this, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                Log.d(TAG, "视频=${data?.data}")
                val uri = data?.data ?: return
                findViewById<VideoView>(R.id.videoView).apply {
                    setVideoURI(uri)
                    start()
                }
            }

            2 -> {
                Log.d(TAG, "图片=${data?.data}")
                val uri = data?.data ?: return
                findViewById<ImageView>(R.id.imageView).setImageURI(uri)
            }
        }
    }

    private fun clickShareFile() {
        val file = viewModel.saveFile
        if (null == file) {
            Toast.makeText(this, "没有可分享的文件", Toast.LENGTH_SHORT).show()
        } else {
            FileManager.send(this, file, "分享一个文件")
        }
    }

    private fun clickSaveFile() {
        val edit = findViewById<EditText>(R.id.editView)
        if (edit.text.isEmpty()) {
            Toast.makeText(this, "请输入文件名", Toast.LENGTH_SHORT).show()
            return
        }
        val fileName = edit.text.toString()
        FileManager.checkPermission(this, object : PermissionListener {
            override fun invoke(isGranted: Boolean) {
                viewModel.save(fileName, R.raw.test)
            }
        })
    }

    private fun clickQueryFile() {
        FileManager.checkPermission(this, object : PermissionListener {
            override fun invoke(isGranted: Boolean) {
                viewModel.query()
            }
        })
    }

    private fun clickAddFile() {
        val edit = findViewById<EditText>(R.id.editView)
        if (edit.text.isEmpty()) {
            Toast.makeText(this, "请输入文件名", Toast.LENGTH_SHORT).show()
            return
        }
        val fileName = edit.text.toString()
        FileManager.checkPermission(this, object : PermissionListener {
            override fun invoke(isGranted: Boolean) {
                viewModel.add(fileName)
            }
        })
    }

    companion object {
        const val TAG = "MainActivity"
    }
}