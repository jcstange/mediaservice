package com.example.mediaservice.shared

data class Music(
    val id: String,
    val title : String,
    val album : String,
    val artist : String,
    val genre : String,
    val source : String,
    val image : String,
    val trackNumber : Int,
    val totalTrackNumber : Int,
    val duration : Int,
    val site : String,
): java.io.Serializable

data class MusicCatalogResponse(
    val music: List<Music>
)

