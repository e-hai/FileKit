package com.anfile.sample

import android.content.Intent
import android.media.MediaMetadataRetriever
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
                MediaMetadataRetriever().apply {
                    setDataSource(this@MainActivity, uri)
                    val time = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    Log.d(TAG, "视频时长=${time}毫秒")
                }
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
        FileManager.checkPermissionBeforeWrite(this, object : PermissionListener {
            override fun invoke(isGranted: Boolean) {
                if(!isGranted){
                    Toast.makeText(this@MainActivity, "没有权限", Toast.LENGTH_SHORT).show()
                    return
                }
                viewModel.save(fileName, R.raw.test)
                Toast.makeText(this@MainActivity, "保存成功", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clickQueryFile() {
        FileManager.checkPermissionBeforeRead(this, object : PermissionListener {
            override fun invoke(isGranted: Boolean) {
                if(!isGranted){
                    Toast.makeText(this@MainActivity, "没有权限", Toast.LENGTH_SHORT).show()
                    return
                }
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
        FileManager.checkPermissionBeforeWrite(this, object : PermissionListener {
            override fun invoke(isGranted: Boolean) {
                if(!isGranted){
                    Toast.makeText(this@MainActivity, "没有权限", Toast.LENGTH_SHORT).show()
                    return
                }
                viewModel.add(fileName)
                Toast.makeText(this@MainActivity, "添加成功", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        const val TAG = "MainActivity"
    }
}