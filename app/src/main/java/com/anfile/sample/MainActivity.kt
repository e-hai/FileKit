package com.anfile.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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