package com.example.threenitas_project.ui

import android.content.Context
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.threenitas_project.R
import com.example.threenitas_project.model.Book
import com.example.threenitas_project.model.DownloadStatus
import com.example.threenitas_project.network.ApiViewModel
import com.example.threenitas_project.ui.signIn.PageTitle
import java.util.Locale
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Magazines(apiViewModel: ApiViewModel, bottomPadding: PaddingValues) {

    val uiState by apiViewModel.valueState.collectAsState()

    val inputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val outputFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    LaunchedEffect(uiState.books) {
        if (uiState.books.isEmpty()) {
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
            item {
                books.chunked(2).forEach { chunk ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        chunk.forEach { book ->
                            PdfUi(
                                book = book,
                                downloadPdf = apiViewModel::downloadPdf,
                                isBookDownloaded = apiViewModel::isBookDownloaded,
                                openPdf = apiViewModel::openPdf
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PdfUi(
    book: Book,
    downloadPdf: KFunction2<Context, Book, Unit>,
    isBookDownloaded: KFunction1<Book, DownloadStatus>,
    openPdf: KFunction2<Context, String, Unit>,
) {
    val context = LocalContext.current
    val isNotDownloaded = isBookDownloaded(book) != DownloadStatus.DOWNLOADED
    Column(
        modifier = Modifier.padding(start = 13.dp, end = 13.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(160.dp)
                .clickable(onClick = {
                    if (isBookDownloaded(book) != DownloadStatus.DOWNLOADING) {
                        if (isBookDownloaded(book) == DownloadStatus.DOWNLOADED) {
                            println("open")
                            openPdf(context, book.title)
                        } else {
                            println("download")
                            downloadPdf(context, book)
                        }
                    }
                }),
            contentAlignment = Alignment.Center // Centers the overlay content
        ) {
            AsyncImage(
                modifier = Modifier.matchParentSize(),
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data((book.img_url).replace("http", "https"))
                    .crossfade(true)
                    .build(),
                contentDescription = book.title,
                contentScale = ContentScale.FillBounds,
                error = painterResource(id = R.drawable.ic_broken_image),
                placeholder = painterResource(id = R.drawable.loading_img)
            )
            // Overlay Box
            Box(
                modifier = Modifier
                    .fillMaxSize() // Covers entire image
                    .background(
                        if (isNotDownloaded) Color.Black.copy(
                            alpha = 0.2f
                        ) else Color.Transparent
                    ),
                contentAlignment = if (isNotDownloaded) Alignment.Center else Alignment.BottomEnd
            ) {
                when (book.isDownloaded) {
                    DownloadStatus.NOT_DOWNLOADED -> {
                        Icon(
                            painter = painterResource(id = R.drawable.download_24px),
                            contentDescription = stringResource(R.string.info_icon),
                        )
                    }

                    DownloadStatus.DOWNLOADING -> {
                        CircularProgressIndicator()
                    }

                    else -> {
                        TriangleWithIcon()
                    }
                }
            }
        }
        Spacer(modifier = Modifier.padding(3.dp))
        Text(
            text = book.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(140.dp)
                .height(50.dp),
        )
    }
}

@Composable
fun TriangleWithIcon() {
    Box(
        modifier = Modifier
            .size(40.dp)
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .rotate(270f)
        ) {
            val width = size.width
            val height = size.height

            // Draw the triangle
            drawPath(
                path = Path().apply {
                    moveTo(width, height)
                    lineTo(0f, height)
                    lineTo(0f, 0f)
                    close()
                },
                color = Color(0xFF28961B)
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.check_24px),
            contentDescription = stringResource(R.string.info_icon),
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}
