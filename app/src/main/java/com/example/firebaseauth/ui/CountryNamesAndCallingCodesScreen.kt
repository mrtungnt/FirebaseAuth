package com.example.firebaseauth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.Pager
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.example.firebaseauth.data.CountryNamesAndCallingCodesModel
import timber.log.Timber

@Composable
fun CountryNamesAndCallingCodesScreen(
    pager: Pager<Int, CountryNamesAndCallingCodesModel>,
    onSelectCountry: (String) -> Unit,
    onNavigateToAuthHomeScreen: () -> Unit,
) {
    Column {
        Text(
            text = "Back",
            modifier = Modifier.clickable(onClick = { onNavigateToAuthHomeScreen() })
        )
        val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

        var colorAlternator = remember {
            1
        }
        var count = remember {
            0
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = lazyPagingItems, key = { it.alpha2Code }) { country ->
                CountryNamesAndCallingCodesRow(
                    country = country,
                    colorAlternatorProvider = { colorAlternator }
                ) {
                    onSelectCountry(
                        it?.name ?: ""
                    )
                    onNavigateToAuthHomeScreen()
                }
                Timber.d("${++count}: ${country?.name}: $colorAlternator ")
                colorAlternator *= (-1)
            }
        }
    }
}

@Composable
fun CountryNamesAndCallingCodesRow(
    country: CountryNamesAndCallingCodesModel?,
    colorAlternatorProvider: () -> Int,
    onClickItem: (CountryNamesAndCallingCodesModel?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickItem(country) }
            .background(Color(if (colorAlternatorProvider() > 0) 0xFFD4EFFA else 0xFFCEEAF3))
    ) {
        Text(
            text = country?.name ?: "", modifier = Modifier
                .fillMaxWidth(.7f)
                .padding(start = 5.dp)
        )

        Box(
            modifier = Modifier
                .background(Color(if (colorAlternatorProvider() > 0) 0xFFD4EAFA else 0xFFCEE6F3))
                .wrapContentHeight()
        ) {
            Text(
                text = country?.alpha2Code ?: "",
                modifier = Modifier.fillMaxWidth(.7f),
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = country?.callingCodes?.get(0) ?: "",
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(end = 5.dp),
            textAlign = TextAlign.Right
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CountryNamesAndCallingCodesRowPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        CountryNamesAndCallingCodesRow(
            CountryNamesAndCallingCodesModel("Vietnam", "VN", listOf("+84")),
            {1}
        ) {}
        CountryNamesAndCallingCodesRow(
            CountryNamesAndCallingCodesModel("USA", "US", listOf("+1")),
            {-1}
        ) {}
    }
}