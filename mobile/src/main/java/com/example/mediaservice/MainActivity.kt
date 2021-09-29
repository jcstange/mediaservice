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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.mediaservice.shared.MyMusicService
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData

private var rootItems : MutableLiveData<MutableList<Item>> = MutableLiveData(mutableListOf())

class MainActivity : ComponentActivity() {
    private lateinit var browser : MediaBrowserCompat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootItems.observe(this, {
            Log.d("rootItems", "updated")
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
            children.forEach {
                rootItems.value?.add(Item(
                    it.description.title.toString(),
                    it.description.description.toString(),
                    it.description.subtitle.toString()
                ))
            }

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
    val subtitle: String
)

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Log.d("DefaultPreview", "Rendering")
    ComposeTheme {
        Surface(

        ) {
            Box() {
                if(rootItems.value?.size == 0) {
                    HelloWorld("Hello World")
                } else {
                    rootItems.value?.forEach {
                        ListItem(it)
                    }
                }

            }
        }
      }
}

@Composable
fun HelloWorld(name: String) {
    Log.d("HelloWorld", "Rendering")
    Text(
        text = name
    )
}

@Composable
fun ComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if(darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun ListItem(
    item: Item
) {
    Log.d("ListItem", "Rendering")
    Box() {
        Column() {
            Text(item.title)
            Text(item.description)
            Text(item.subtitle)
        }
    }
}

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp)
)

val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        color = Color.White,
        fontSize = 16.sp
    )
)

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)

private val LightColorPalette = darkColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200
)
