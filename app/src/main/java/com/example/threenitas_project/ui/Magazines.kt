package com.example.threenitas_project.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.threenitas_project.R
import com.example.threenitas_project.ui.signIn.PageTitle
import com.example.threenitas_project.ui.theme.Threenitas_projectTheme

@Composable
fun Magazines() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState), //not needed but good to have
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PageTitle(stringResource(R.string.magazines))
    }

}




@Preview(showBackground = true)
@Composable
fun MagazinesScreenPreview() {
    Threenitas_projectTheme {
        Magazines()
    }
}




@Composable
fun StickyHeaderLazyColumn(items: List<String>) {
    val listState = rememberLazyListState()
    val stickyItem = remember { mutableStateOf(items.first()) }

    // Track the first visible item to update sticky item
    LaunchedEffect(listState.firstVisibleItemIndex) {
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