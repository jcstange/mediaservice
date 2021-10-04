package com.example.mediaservice.shared

import android.app.Notification
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.*


const val NOTIFICATION_ICON_SIZE = 144

fun myMusicServiceNotificationBuilder(
    context: Context,
    channelId: String,
    mediaSession: MediaSessionCompat
) : NotificationCompat.Builder {
    val controller = mediaSession.controller
    val mediaMetaData = controller.metadata
    val description = mediaMetaData.description

    val glideOptions = RequestOptions()
        .fallback(R.drawable.notification_icon)
        .diskCacheStrategy(DiskCacheStrategy.DATA)

    suspend fun retrieveBitmapFromUri(uri: Uri) = withContext(Dispatchers.IO) {
            Glide.with(context).applyDefaultRequestOptions(glideOptions)
                .asBitmap()
                .load(uri)
                .submit(NOTIFICATION_ICON_SIZE, NOTIFICATION_ICON_SIZE)
                .get()

        }

    return runBlocking {
        NotificationCompat.Builder(context, channelId).apply {
            val iconBitmap : Bitmap
            runBlocking {
                iconBitmap = retrieveBitmapFromUri(description.iconUri!!)
            }
            setLargeIcon(iconBitmap)
            // Add the metadata fot the currently playing track
            setContentTitle(description.title)
            setContentText(description.subtitle)
            setSubText(description.description)

            // Enable launching the player by clicking the notification
            setContentIntent(controller.sessionActivity)

            //Stop the service when the notification is swiped away
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            // Make the transport controls visible on the lockscreen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            setSmallIcon(R.drawable.play)
            color = Color.GREEN

            addAction(
                NotificationCompat.Action(
                    R.drawable.pause,
                    context.getString(R.string.pause),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_PAUSE
                    )
                )
            )

            addAction(
                NotificationCompat.Action(
                    R.drawable.play,
                    context.getString(R.string.play),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_PLAY
                    )
                )
            )

            addAction(
                NotificationCompat.Action(
                    R.drawable.stop,
                    context.getString(R.string.stop),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
            )

            // Take advantage of MediaStyle features
            setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0)

                // Add a cancel button
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
            )

            setChannelId(channelId)
            setOngoing(true)
        }
    }
}