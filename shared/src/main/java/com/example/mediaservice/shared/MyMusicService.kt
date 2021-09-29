package com.example.mediaservice.shared

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log

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

private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
private const val ON_GOING_NOTIFICATION_ID = 1

class MyMusicService : MediaBrowserServiceCompat() {

    private lateinit var context : Context
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private val callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            Log.v("Debug => ", "onPlay")
            startForeground(
                ON_GOING_NOTIFICATION_ID,
                myMusicServiceNotificationBuilder(
                    context,
                    "dummy",
                    mediaSession
                ).build()
            )
        }

        override fun onSkipToQueueItem(queueId: Long) {
            Log.v("Debug => ", "onSkipToQueueItem")
        }

        override fun onSeekTo(position: Long) {
            Log.v("Debug => ", "onSeekTo")
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            Log.v("Debug => ", "onPlayFromMediaId")
        }

        override fun onPause() {
            Log.v("Debug => ", "onPause")
        }

        override fun onStop() {
            Log.v("Debug => ", "onStop")
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

    override fun onCreate() {
        super.onCreate()
        context = this

        mediaSession = MediaSessionCompat(this, "MyMusicService").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())
            setCallback(callback)
            setSessionToken(sessionToken)
        }
    }

    override fun onDestroy() {
        mediaSession.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): MediaBrowserServiceCompat.BrowserRoot? {
        val browsing = true
        //return if(allowBrowsing(clientPackageName, clientUid)){
        return if(browsing){
            MediaBrowserServiceCompat.BrowserRoot(MY_MEDIA_ROOT_ID, null)
        } else {
            MediaBrowserServiceCompat.BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
        }
    }

    override fun onLoadChildren(
        parentMediaId: String,
        result: Result<MutableList<MediaItem>>
    ) {
        //result.sendResult(ArrayList())
        if(MY_EMPTY_MEDIA_ROOT_ID == parentMediaId) {
            result.sendResult(null)
            return
        }

        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()

        if(MY_MEDIA_ROOT_ID == parentMediaId) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
            val mediaDescription = MediaDescriptionCompat.Builder()
                .setTitle("TestTitle")
                .setDescription("Media description")
                .setSubtitle("Subtitle")
                .setMediaId("Fuck me")
                .build()
            val mediaItem = MediaItem(mediaDescription, FLAG_PLAYABLE)
            mediaItems.add(mediaItem)
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems)
    }
}

