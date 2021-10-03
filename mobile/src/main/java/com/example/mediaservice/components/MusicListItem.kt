package com.example.mediaservice.components

import android.net.Uri
import android.util.Log
import com.example.mediaservice.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.mediaservice.models.MusicItem
import com.example.mediaservice.ui.GrayBackground
import com.example.mediaservice.ui.Shapes
import com.google.accompanist.glide.rememberGlidePainter

@Preview(showBackground = true)
@Composable
fun MusicListItem(
    @PreviewParameter(ItemPreviewParameterProvider::class) item: MusicItem,
    onClick : () -> Unit = {}
) {
    Log.d("ListItem", "Rendering")
    Box(
        Modifier
        .padding(10.dp)
        .fillMaxWidth()
        .border(
            color= Color.Transparent,
            shape = Shapes.medium,
            width = 1.dp
        )
        .background(color= GrayBackground)
        .clickable {
            onClick.invoke()
        }
    ) {
        Row {
            Image(
                painter = if(item.cover == "")  painterResource(R.drawable.ic_launcher_foreground) else rememberGlidePainter(item.cover),
                contentDescription = "Album cover",
                modifier = Modifier.fillMaxWidth(.3f)
            )
            Spacer(Modifier.width(16.dp))
            Column(
                Modifier
                    .padding(top= 6.dp),
                verticalArrangement= Arrangement.SpaceBetween
            ) {
                Text(
                    text= item.title,
                    style= MaterialTheme.typography.body1.copy(Color.White)
                )
                Text(
                    text= item.description,
                    style= MaterialTheme.typography.body2.copy(Color.White)
                )
                Text(
                    text= item.subtitle,
                    style= MaterialTheme.typography.body2.copy(Color.White)
                )
            }
        }
    }
}

class ItemPreviewParameterProvider : PreviewParameterProvider<MusicItem> {
    override val values = sequenceOf(
        MusicItem(
            "",
            "Title 1",
            "Description 1",
            "Subtitle 1",
            "",
            "".toUri(),
        ),
        MusicItem(
            "",
            "Title 2",
            "Description 2",
            "Subtitle 2",
            "",
            "".toUri(),
        ),
    )
}

