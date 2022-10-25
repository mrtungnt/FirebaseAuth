package com.example.firebaseauth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.Pager
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.example.firebaseauth.R
import com.example.firebaseauth.data.CountryNamesAndCallingCodesModel

@Composable
fun CountryNamesAndCallingCodesScreen(
    pager: Pager<Int, CountryNamesAndCallingCodesModel>,
    onSelectCountry: (String) -> Unit,
    onNavigateToAuthHomeScreen: () -> Unit,
) {
    Column {
        Image(
            painter = painterResource(R.drawable.ic_baseline_keyboard_backspace_24),
            contentDescription = null,
            modifier = Modifier
                .clickable(onClick = { onNavigateToAuthHomeScreen() })
                .padding(3.dp)
        )

        val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = lazyPagingItems, key = { it.alpha2Code }) { country ->
                CountryNamesAndCallingCodesRow(
                    country = country,
                    colorAlternatorProvider = { country?.ordinal!! % 2 }
                ) {
                    onSelectCountry(
                        it?.name ?: ""
                    )
                    onNavigateToAuthHomeScreen()
                }
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
            .height(IntrinsicSize.Min)
            .clickable { onClickItem(country) }
            .background(Color(if (colorAlternatorProvider() == 0) 0xFFB1F6FB else 0xFFAEECF0)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = country?.name ?: "", modifier = Modifier
                .fillMaxWidth(.7f)
                .padding(start = 5.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(.3f)
                .fillMaxHeight()
                .background(Color(if (colorAlternatorProvider() == 0) 0xFFB1EAF6 else 0xFFB1E5FB)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = country?.alpha2Code ?: "", textAlign = TextAlign.Center
            )
        }

        Text(
            text = "+${country?.callingCodes?.get(0) ?: ""}",
            modifier = Modifier
                .fillMaxWidth()
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
            CountryNamesAndCallingCodesModel(
                name = "Vietnam",
                alpha2Code = "VN",
                callingCodes = listOf("84")
            ),
            { 1 % 2 }
        ) {}
        CountryNamesAndCallingCodesRow(
            CountryNamesAndCallingCodesModel("USA", "US", listOf("1")),
            { 2 % 2 }
        ) {}
    }
}