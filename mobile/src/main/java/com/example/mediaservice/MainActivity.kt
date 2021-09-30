package com.example.mediaservice

import android.content.ComponentName
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.mediaservice.shared.MyMusicService
import androidx.compose.material.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.example.mediaservice.ui.ComposeTheme
import com.example.mediaservice.ui.GrayBackground
import com.example.mediaservice.ui.Shapes
import com.google.accompanist.glide.rememberGlidePainter
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.mediaservice.ui.Purple200

private var rootItems = MutableLiveData<MutableList<Item>>(mutableListOf())

class MainActivity : ComponentActivity() {
    private lateinit var browser : MediaBrowserCompat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootItems.observe(this, {
            Log.d("rootItems", "size ${rootItems.value?.size}")
        })
        setContent {
            DefaultPreview()
        }
        browser = MediaBrowserCompat(
            this,
            ComponentName(this, MyMusicService::class.java),
            mediaBrowserCallback,
            null
        )
        browser.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        browser.disconnect()
    }

    private val mediaBrowserCallback = object: MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            try {
                val token = browser.sessionToken
                val controller = MediaControllerCompat(this@MainActivity, token)
                MediaControllerCompat.setMediaController(this@MainActivity, controller)
                controller.registerCallback(controllerCallback)
                Log.d("mediaBrowserCallback", "onConnected")

            } catch (e: RemoteException) {
                Log.e(
                    this@MainActivity::class.java.canonicalName,
                    "Error creating controller",
                    e
                )
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

    val controllerCallback = object: MediaControllerCompat.Callback() {
        override fun binderDied() {
            super.binderDied()
            Log.d("controllerCallback", "binderDied")
        }

        override fun onSessionReady() {
            super.onSessionReady()
            Log.d("controllerCallback", "onSessionReady")
            Log.d("controllerCallback", "${mediaController?.playbackState}")
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

    fun subscribe(root: String, mediaBrowser: MediaBrowserCompat) {
        mediaBrowser.subscribe(
            root,
            subscriptionCallback
        )
    }

    val subscriptionCallback = object: MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            super.onChildrenLoaded(parentId, children)
            Log.d("onChildrenLoaded","parentId: $parentId")
            val mediaItems = children.map {
                Log.d("MediaItem","${it.mediaId}")
                Item(
                    it.description.title.toString(),
                    it.description.description.toString(),
                    it.description.subtitle.toString(),
                    it.description.iconUri.toString()
                )
            }.toMutableList()
            rootItems.postValue(mediaItems)

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

        override fun onError(parentId: String, options: Bundle) {
            super.onError(parentId, options)
        }
    }
}

data class Item(
    val title: String,
    val description: String,
    val subtitle: String,
    val cover: String
)

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Log.d("DefaultPreview", "Rendering")
    val _rootItems: List<Item> by rootItems.observeAsState(listOf())
    ComposeTheme {
        Surface() {
            Box(Modifier.fillMaxWidth().fillMaxHeight()) {
                if(_rootItems.size == 0) {
                    EmptyList(name = "Empty List")
                } else {
                    LazyColumn() {
                        items(_rootItems) { rootItem ->
                            ListItem(rootItem)
                        }
                    }
                }
            }
        }
      }
}

@Composable
fun EmptyList(name: String) {
    Box(
        Modifier
            .fillMaxSize()
    ) {
        Text(
            text = name,
            Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListItem(
    @PreviewParameter(ItemPreviewParameterProvider::class) item: Item
) {
    Log.d("ListItem", "Rendering")
    Box(Modifier
        .padding(10.dp)
        .fillMaxWidth()
        .border(
            color= Color.Transparent,
            shape = Shapes.medium,
            width = 1.dp
        )
        .background(color= GrayBackground)
    ) {
        Row(){
            Image(
                painter = rememberGlidePainter(item.cover),
                contentDescription = "Album cover"
            )
            Spacer(Modifier.width(16.dp))
            Column(
                Modifier
                    .padding(top= 6.dp)
                    .fillMaxHeight(),
                verticalArrangement= Arrangement.SpaceBetween
            ) {
                Text(
                    text= item.title,
                    style= MaterialTheme.typography.body1
                )
                Text(
                    text= item.description,
                    style= MaterialTheme.typography.body2
                )
                Text(
                    text= item.subtitle,
                    style= MaterialTheme.typography.body2
                )
            }
        }
    }
}

class ItemPreviewParameterProvider : PreviewParameterProvider<Item> {
    override val values = sequenceOf(
        Item(
            "Title 1",
            "Description 1",
            "Subtitle 1",
            "https://upload.wikimedia.org/wikipedia/commons/d/d7/Android_robot.svg"
        ),
        Item(
            "Title 2",
            "Description 2",
            "Subtitle 2",
            "https://upload.wikimedia.org/wikipedia/commons/d/d7/Android_robot.svg"
        ),
    )
}

