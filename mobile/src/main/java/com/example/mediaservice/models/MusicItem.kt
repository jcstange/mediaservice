package com.example.mediaservice.models

import android.net.Uri

data class MusicItem(
    val mediaId: String,
    val title: String,
    val description: String,
    val subtitle: String,
    val cover: String,
    val source: Uri
)

