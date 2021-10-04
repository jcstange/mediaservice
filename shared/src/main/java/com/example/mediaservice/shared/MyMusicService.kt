package com.example.mediaservice.shared

import android.app.NotificationChannel
import android.app.NotificationChannel.DEFAULT_CHANNEL_ID
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.*
import java.net.URI

import java.util.ArrayList

/**
 * This class provides a MediaBrowser through a service. It exposes the media library to a browsing
 * client, through the onGetRoot and onLoadChildren methods. It also creates a MediaSession and
 * exposes it through its MediaSession.Token, which allows the client to create a MediaController
 * that connects to and send control commands to the MediaSession remotely. This is useful for
 * user interfaces that need to interact with your media session, like Android Auto. You can
 * (should) also use the same service from your app"s UI, which gives a seamless playback
 * experience to the user.
 *
 *
 * To implement a MediaBrowserService, you need to:
 *
 *  *  Extend [MediaBrowserServiceCompat], implementing the media browsing
 * related methods [MediaBrowserServiceCompat.onGetRoot] and
 * [MediaBrowserServiceCompat.onLoadChildren];
 *
 *  *  In onCreate, start a new [MediaSessionCompat] and notify its parent
 * with the session"s token [MediaBrowserServiceCompat.setSessionToken];
 *
 *  *  Set a callback on the [MediaSessionCompat.setCallback].
 * The callback will receive all the user"s actions, like play, pause, etc;
 *
 *  *  Handle all the actual music playing using any method your app prefers (for example,
 * [android.media.MediaPlayer])
 *
 *  *  Update playbackState, "now playing" metadata and queue, using MediaSession proper methods
 * [MediaSessionCompat.setPlaybackState]
 * [MediaSessionCompat.setMetadata] and
 * [MediaSessionCompat.setQueue])
 *
 *  *  Declare and export the service in AndroidManifest with an intent receiver for the action
 * android.media.browse.MediaBrowserService
 *
 * To make your app compatible with Android Auto, you also need to:
 *
 *  *  Declare a meta-data tag in AndroidManifest.xml linking to a xml resource
 * with a &lt;automotiveApp&gt; root element. For a media app, this must include
 * an &lt;uses name="media"/&gt; element as a child.
 * For example, in AndroidManifest.xml:
 * &lt;meta-data android:name="com.google.android.gms.car.application"
 * android:resource="@xml/automotive_app_desc"/&gt;
 * And in res/values/automotive_app_desc.xml:
 * &lt;automotiveApp&gt;
 * &lt;uses name="media"/&gt;
 * &lt;/automotiveApp&gt;
 *
 */

private const val MY_CHANNEL = "notification_channel"
private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
private const val ON_GOING_NOTIFICATION_ID = 1

class MyMusicService : MediaBrowserServiceCompat() {

    private lateinit var context : Context
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        context = this

        createNotificationChannel()

        mediaSession = MediaSessionCompat(this, "MyMusicService").apply {
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_STOP
                )
            setPlaybackState(stateBuilder.build())
            setCallback(callback)
            setSessionToken(sessionToken)
            MediaButtonReceiver.handleIntent(this, Intent(Intent.ACTION_MEDIA_BUTTON))
        }

    }

    override fun onDestroy() {
        mediaSession.release()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            MY_CHANNEL,
            MY_CHANNEL,
            NotificationManager.IMPORTANCE_LOW
        )
        channel.lightColor = Color.GREEN
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private val callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            Log.v("Debug => ", "onPlay")
            val mediaUri = mediaSession.controller.metadata.description.mediaUri
            Log.v("On Play => ", "mediaUri = ${mediaUri}")
            mediaPlayer?.start()
            stateBuilder.setState(
                PlaybackStateCompat.STATE_PLAYING,
                mediaPlayer!!.currentPosition.toLong(),
                1f
            )
            mediaSession.setPlaybackState(stateBuilder.build())

        }

        override fun onSkipToQueueItem(queueId: Long) {
            Log.v("Debug => ", "onSkipToQueueItem")
        }

        override fun onSeekTo(position: Long) {
            Log.v("Debug => ", "onSeekTo")
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            Log.v("Debug => ", "onPlayFromMediaId")

            if(mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                return
            }
            val mediaTitle = extras?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            val mediaUri = extras?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
            val artist = extras?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
            val album = extras?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
            val albumURI = extras?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
            Log.v("On Play => ", "mediaStringUri = ${mediaUri}")
            Log.v("On Play => ", "albumUri = ${albumURI}")
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder().apply {
                    putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUri)
                    putString(MediaMetadataCompat.METADATA_KEY_TITLE, mediaTitle)
                    putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                    putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumURI)
                    putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, albumURI)
                    putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artist)
                    putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, album)
                }.build()
            )
            mediaPlayer = MediaPlayer().apply  {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(mediaUri)
                setOnPreparedListener {
                    it.start()
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        mediaPlayer!!.currentPosition.toLong(),
                        1f
                    )
                    mediaSession.setPlaybackState(stateBuilder.build())
                }
                prepareAsync() // might take long! (for buffering, etc)
            }

            startForeground(
                ON_GOING_NOTIFICATION_ID,
                myMusicServiceNotificationBuilder(
                    context,
                    MY_CHANNEL,
                    mediaSession
                ).build()
            )

        }

        override fun onPause() {
            Log.v("Debug => ", "onPause")
            if(mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                stateBuilder.setState(
                    PlaybackStateCompat.STATE_PAUSED,
                    mediaPlayer!!.currentPosition.toLong(),
                    1f
                )
                mediaSession.setPlaybackState(stateBuilder.build())
            }
        }

        override fun onStop() {
            Log.v("Debug => ", "onStop")
            stateBuilder.setState(
                PlaybackStateCompat.STATE_STOPPED,
                mediaPlayer!!.currentPosition.toLong(),
                1f
            )
            mediaSession.setPlaybackState(stateBuilder.build())
            mediaPlayer?.release()

        }

        override fun onSkipToNext() {
            Log.v("Debug => ", "onSkipToNext")
        }

        override fun onSkipToPrevious() {
            Log.v("Debug => ", "onSkipToPrevious")
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            Log.v("Debug => ", "onCustomAction")
        }

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            Log.v("Debug => ", "onPlayFromSearch")
        }
    }


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        val browsing = true
        //return if(allowBrowsing(clientPackageName, clientUid)){
        return if(browsing){
            BrowserRoot(MY_MEDIA_ROOT_ID, null)
        } else {
            BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
        }
    }

    override fun onLoadChildren(
        parentMediaId: String,
        result: Result<MutableList<MediaItem>>
    ) {
        if(MY_EMPTY_MEDIA_ROOT_ID == parentMediaId) {
            result.sendResult(null)
            return
        }

        var mediaItems = mutableListOf<MediaItem>()

        if(MY_MEDIA_ROOT_ID == parentMediaId) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list.
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(MusicCatalogResponse::class.java)
            val list = adapter.fromJson(mockMediaList)

            val mediaDescriptionList = list!!.music.map {
                val mediaDescription = MediaDescriptionCompat.Builder()
                    .setTitle(it.title)
                    .setDescription(it.album)
                    .setSubtitle(it.trackNumber.toString())
                    .setMediaId(it.id)
                    .setIconUri(Uri.parse(it.image))
                    .setMediaUri(Uri.parse(it.source))
                    .build()
                MediaItem(mediaDescription, FLAG_PLAYABLE)
            }
            mediaItems = mediaDescriptionList.toMutableList()
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems)
    }


}
