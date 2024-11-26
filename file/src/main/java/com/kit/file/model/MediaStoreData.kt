package com.kit.file.model

import android.net.Uri
import java.util.*

data class MediaStoreData(
    val id: Long,
    val displayName: String,
    val dateAdded: Date,
    val contentUri: Uri
)