package com.example.threenitas_project.ui

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.threenitas_project.R
import com.example.threenitas_project.network.ApiViewModel
import com.example.threenitas_project.ui.signIn.PageTitle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Magazines(apiViewModel: ApiViewModel, bottomPadding: PaddingValues) {

    val uiState by apiViewModel.valueState.collectAsState()

    val inputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val outputFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    LaunchedEffect(uiState.books) {
        if(uiState.books.isEmpty()) {
            apiViewModel.getBooks()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = bottomPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Display the title of the page
        item {
            PageTitle(stringResource(R.string.magazines))
        }

        uiState.groupedBooks.forEach { (month, books) ->
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val date = inputFormat.parse(month)  // Parse "2020-07"
                    val formattedMonth = outputFormat.format(date)  // Format as "Jul 2020"
                    Text(
                        text = formattedMonth,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 25.dp, top = 5.dp)
                    )
                }
            }

            items(books) { book ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 13.dp, end = 13.dp, bottom = 30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    PdfUi(imgUrl = book.img_url, title = book.title)
                    Spacer(modifier = Modifier.padding(13.dp))
                    PdfUi(imgUrl = book.img_url, title = book.title)
                }
            }
        }
    }
}


@Composable
fun PdfUi(imgUrl: String, title: String) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            modifier = Modifier
                .width(140.dp)
                .height(160.dp)
                .clickable(onClick = { }),
            model = ImageRequest.Builder(context = LocalContext.current)
                .data((imgUrl).replace("http", "https"))
                .crossfade(true)
                .build(),
            contentDescription = "photo.volumeInfo.title",
            contentScale = ContentScale.FillBounds,
            error = painterResource(id = R.drawable.ic_broken_image),
            placeholder = painterResource(id = R.drawable.loading_img)
        )
        Spacer(modifier = Modifier.padding(3.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(150.dp),
        )
    }
}
