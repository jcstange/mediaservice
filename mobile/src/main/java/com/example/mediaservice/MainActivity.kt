package com.example.mediaservice

import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.mediaservice.ui.ComposeTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import com.example.mediaservice.components.MusicListItem
import com.example.mediaservice.models.MusicItem
import org.koin.android.ext.android.inject


class MainActivity : ComponentActivity() {
    private val vm : MainViewModel by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.instantiateBrowser(this)
        setContent {
            DefaultPreview(vm)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.browser.disconnect()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview(vm: MainViewModel? = null) {
    val _rootItems: List<MusicItem> by vm?.rootItems!!.observeAsState(listOf())
    ComposeTheme {
        Surface {
            Box(Modifier.fillMaxSize()) {
                if(_rootItems.isEmpty()) {
                    EmptyList(name = "Empty List")
                } else {
                    LazyColumn {
                        items(_rootItems) { rootItem ->
                            MusicListItem(
                                rootItem,
                                {
                                    vm!!.play(rootItem)
                                }
                            )
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

