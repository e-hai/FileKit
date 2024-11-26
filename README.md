# Shared Storage SDK

### 简介

`Shared Storage SDK` 是一个 Android SDK，提供了对公共存储目录中文件（图片、音频、视频及其他文件）的管理和存取功能。通过该
SDK，开发者可以轻松地在应用中处理图片、音频、视频文件的创建、保存和查询等操作。

### 功能

1. **创建文件**：可以在公共存储中创建图片、音频、视频、下载文件等。
2. **保存文件**：支持将图片、音频、视频等文件从输入流保存到公共存储目录。
3. **查询文件**：提供了查询图片、音频、视频、下载文件等功能，支持分页查询。
4. **兼容性**：支持 Android 版本 10 及以上（包含 Android 10 对存储权限的改变），并向下兼容低版本
   Android。

### 使用示例

1. 创建并保存图片
```kotlin
FileManager.checkPermissionBeforeWrite(this, object : PermissionListener {
            override fun invoke(isGranted: Boolean) {
                if(!isGranted){
                    Toast.makeText(this@MainActivity, "没有权限", Toast.LENGTH_SHORT).show()
                    return
                }
                val sharedStorage = FileManager.sharedStorage(context)

                // 创建图片文件并保存
                val fileName = "my_image.png"
                val imageBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.my_image)
                val savedUri = sharedStorage.savePicture(fileName, imageBitmap)
                
                if (savedUri != Uri.EMPTY) {
                    // 成功保存图片
                    Log.d("SharedStorage", "图片已保存：$savedUri")
                } else {
                    // 保存失败
                    Log.e("SharedStorage", "图片保存失败")
                }
            }
        })
```

2. 创建并保存音频文件
```kotlin
FileManager.checkPermissionBeforeWrite(this, object : PermissionListener {
            override fun invoke(isGranted: Boolean) {
                if(!isGranted){
                    Toast.makeText(this@MainActivity, "没有权限", Toast.LENGTH_SHORT).show()
                    return
                }
                val sharedStorage = FileManager.sharedStorage(context)

                val audioInputStream: InputStream = FileInputStream(File("path/to/audio/file"))
                val audioFileName = "my_audio.mp3"
                val savedAudioUri = sharedStorage.saveMusic(audioFileName, audioInputStream)
                
                if (savedAudioUri != Uri.EMPTY) {
                // 成功保存音频
                Log.d("SharedStorage", "音频已保存：$savedAudioUri")
                } else {
                // 保存失败
                Log.e("SharedStorage", "音频保存失败")
                }
            }
        })
```
3. 查询媒体文件（图片、音频、视频）
```kotlin
 FileManager.checkPermissionBeforeRead(this, object : PermissionListener {
    override fun invoke(isGranted: Boolean) {
        if(!isGranted){
            Toast.makeText(this@MainActivity, "没有权限", Toast.LENGTH_SHORT).show()
            return
        }
        val sharedStorage = FileManager.sharedStorage(context)
        // 查询图片
        val offset = 0
        val limit = 10
        val pictures = sharedStorage.queryPicture(offset, limit)
        pictures.forEach {
            Log.d("SharedStorage", "图片: ${it.displayName}, 日期: ${it.dateAdded}")
        }

        // 查询音频
        val musicFiles = sharedStorage.queryMusic(offset, limit)
        musicFiles.forEach {
            Log.d("SharedStorage", "音频: ${it.displayName}, 日期: ${it.dateAdded}")
        }

        // 查询视频
        val videos = sharedStorage.queryMovie(offset, limit)
        videos.forEach {
            Log.d("SharedStorage", "视频: ${it.displayName}, 日期: ${it.dateAdded}")
        }

        // 查询其他文件（如下载文件）
        val downloadFiles = sharedStorage.queryOther(offset, limit)
        downloadFiles.forEach {
            Log.d("SharedStorage", "下载文件: ${it.displayName}, 日期: ${it.dateAdded}")
        }
    }
})
   
```
### 支持的文件类型
该 SDK 支持以下几种常见的文件类型：

图片：PNG, JPEG, GIF 等
音频：MP3, WAV 等
视频：MP4, AVI 等
其他文件：任意类型的下载文件（例如 PDF, 文档等）
