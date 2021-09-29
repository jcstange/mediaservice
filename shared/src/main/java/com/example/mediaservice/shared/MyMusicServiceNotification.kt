package com.example.mediaservice.shared

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver

fun myMusicServiceNotificationBuilder(
    context: Context,
    channelId: String,
    mediaSession: MediaSessionCompat
) : NotificationCompat.Builder {
    val controller = mediaSession.controller
    val mediaMetaData = controller.metadata
    val description = mediaMetaData.description

    return NotificationCompat.Builder(context, channelId).apply {
        // Add the metadata fot the currently playing track
        setContentTitle(description.title)
        setContentText(description.subtitle)
        setSubText(description.description)
        setLargeIcon(description.iconBitmap)

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

        setSmallIcon(R.drawable.notification_icon)
        color = ContextCompat.getColor(context, R.color.primaryDark)

        addAction(
            NotificationCompat.Action(
                R.drawable.pause,
                context.getString(R.string.pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
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
    }
}