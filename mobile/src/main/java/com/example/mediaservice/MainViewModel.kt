package com.example.mediaservice

import android.content.ComponentName
import android.content.Context
import android.media.MediaRouter2
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaservice.models.MusicItem
import com.example.mediaservice.network.Repository
import com.example.mediaservice.shared.MyMusicService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    repository: Repository
) : ViewModel() {

    private val _rootItems = MutableLiveData<MutableList<MusicItem>>(mutableListOf())
    val rootItems : LiveData<MutableList<MusicItem>> = _rootItems
    lateinit var browser : MediaBrowserCompat
    lateinit var controller : MediaControllerCompat
    var mediaBrowserCallback : MediaBrowserCompat.ConnectionCallback? = null
    var controllerCallback : MediaControllerCompat.Callback? = null

    fun instantiateBrowser(context: Context) {
        browser = MediaBrowserCompat(
            context,
            ComponentName(context, MyMusicService::class.java),
            configMediaBrowserCallback(context),
            null
        )
        browser.connect()
    }

    fun subscribe(root: String, mediaBrowser: MediaBrowserCompat) {
        mediaBrowser.subscribe(
            root,
            subscriptionCallback
        )
    }

    fun play(item: MusicItem) {
        Log.d("MainViewModel","playing -> ${item.title}")
        controller.transportControls.playFromMediaId(item.mediaId, Bundle().apply {
            putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, item.cover)
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST, item.subtitle)
            putString(MediaMetadataCompat.METADATA_KEY_TITLE, item.title)
            putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, item.source.toString())
            putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, item.mediaId)
        })
    }

    private val subscriptionCallback = object: MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            super.onChildrenLoaded(parentId, children)
            Log.d("onChildrenLoaded","parentId: $parentId")
            val mediaItems = children.map {
                Log.d("MediaItem","${it.mediaId}")
                MusicItem(
                    it.mediaId.toString(),
                    it.description.title.toString(),
                    it.description.description.toString(),
                    it.description.subtitle.toString(),
                    it.description.iconUri.toString(),
                    it.description.mediaUri!!
                )
            }.toMutableList()
            _rootItems.postValue(mediaItems)

        }

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
            options: Bundle
        ) {
            super.onChildrenLoaded(parentId, children, options)
        }

        override fun onError(parentId: String) {
            super.onError(parentId)
        }
    }

    private fun configMediaBrowserCallback(context: Context) = object: MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            try {
                val token = browser.sessionToken
                controller = MediaControllerCompat(context, token)
                MediaControllerCompat.setMediaController(context as MainActivity, controller)
                controller.registerCallback(configControllerCallback(context))
                Log.d("mediaBrowserCallback", "onConnected")

            } catch (e: RemoteException) {
                Log.e("ConnectionCallback", "Error creating controller", e)
            }
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            Log.d("mediaBrowserCallback", "onConnectionSuspended")
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            Log.d("mediaBrowserCallback", "onConnectionFailed")
        }
    }

    fun configControllerCallback(context: Context) = object: MediaControllerCompat.Callback() {
        override fun binderDied() {
            super.binderDied()
            Log.d("controllerCallback", "binderDied")
        }

        override fun onSessionReady() {
            super.onSessionReady()
            Log.d("controllerCallback", "onSessionReady")
            Log.d("controllerCallback", "${MediaControllerCompat.getMediaController(context as MainActivity)?.playbackState}")
            subscribe(browser.root, browser)
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            Log.d("controllerCallback", "onSessionDestroyed")
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            Log.d("controllerCallback", "onSessionEvent")
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            Log.d("controllerCallback", "onPlaybackStateChanged")
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)

            Log.d("controllerCallback", "onMetadataChanged")
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            super.onQueueChanged(queue)
            Log.d("controllerCallback", "onQueueChanged")
        }

        override fun onQueueTitleChanged(title: CharSequence?) {
            super.onQueueTitleChanged(title)
            Log.d("controllerCallback", "onQueueTitleChanged")
        }

        override fun onExtrasChanged(extras: Bundle?) {
            super.onExtrasChanged(extras)
            Log.d("controllerCallback", "onExtrasChanged")
        }

        override fun onAudioInfoChanged(info: MediaControllerCompat.PlaybackInfo?) {
            super.onAudioInfoChanged(info)
            Log.d("controllerCallback", "onAudioInfoChanged")
        }

        override fun onCaptioningEnabledChanged(enabled: Boolean) {
            super.onCaptioningEnabledChanged(enabled)
            Log.d("controllerCallback", "onCaptioningEnabledChanged")
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            super.onRepeatModeChanged(repeatMode)
            Log.d("controllerCallback", "onRepeatModeChanged")
        }

        override fun onShuffleModeChanged(shuffleMode: Int) {
            super.onShuffleModeChanged(shuffleMode)
            Log.d("controllerCallback", "onShuffleModeChanged")
        }
    }

    var userData: String? = null
    suspend fun checkSessionExpiry() = withContext(Dispatchers.IO) {
        delay(5000)
        userData = "some_user_data"
    }
}

