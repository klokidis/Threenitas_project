package com.example.threenitas_project.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.threenitas_project.R
import com.example.threenitas_project.network.ApiViewModel
import com.example.threenitas_project.ui.signIn.PageTitle

@Composable
fun Magazines(apiViewModel: ApiViewModel, bottomPadding: PaddingValues) {

    val scrollState = rememberScrollState()
    val uiState by apiViewModel.valueState.collectAsState()

    LaunchedEffect(uiState.books) {
        apiViewModel.getBooks()
        println("test")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState), //not needed but good to have
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PageTitle(stringResource(R.string.magazines))
        Text(text = uiState.books.toString())
    }

}

@Composable
fun PdfUi(items: List<String>) {
    Column {
        AsyncImage(
            modifier = Modifier
                .width(250.dp)
                .height(250.dp)
                //.fillParentMaxHeight()
                .clickable(onClick = { }),
            model = ImageRequest.Builder(context = LocalContext.current)
                //.data((photo.volumeInfo.imageLinks?.thumbnail)?.replace("http", "https"))
                .crossfade(true)
                .build(),
            contentDescription = "photo.volumeInfo.title",
            contentScale = ContentScale.FillBounds,
            error = painterResource(id = R.drawable.ic_broken_image),
            placeholder = painterResource(id = R.drawable.loading_img)
        )
        Spacer(modifier = Modifier.padding(10.dp))
        Text(
            text = "name",
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            fontWeight = FontWeight.Bold // this can be inside style
        )
    }
}

@Composable
fun StickyHeaderLazyColumn(items: List<String>) {
    val listState = rememberLazyListState()
    val stickyItem = remember { mutableStateOf(items.first()) }

    // Track the first visible item to update sticky item
    LaunchedEffect(remember { derivedStateOf { listState.firstVisibleItemIndex } }) {
        stickyItem.value = items.getOrNull(listState.firstVisibleItemIndex) ?: items.first()
    }

    Column {
        // Sticky item that stays on top
        Text(text = "Sticky Item: ${stickyItem.value}", style = MaterialTheme.typography.titleLarge)

        // LazyColumn for scrollable items
        LazyColumn(state = listState) {
            items(items) { item ->
                Box(modifier = Modifier.padding(8.dp)) {
                    Text(text = item)
                }
            }
        }
    }
}