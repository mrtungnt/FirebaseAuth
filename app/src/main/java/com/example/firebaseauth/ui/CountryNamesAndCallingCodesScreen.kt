/*
package com.example.firebaseauth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
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
    onKeywordChange: (String) -> Unit,
    countryNamesAndCallingCodesSearchResultProvider: () -> List<CountryNamesAndCallingCodesModel>,
    onNavigateToAuthHomeScreen: () -> Unit,
) {
    var keyword by rememberSaveable { mutableStateOf("") }

    Column {
        TopBar(
            onNavigateBack = { onNavigateToAuthHomeScreen() },
            keywordProvider = { keyword },
            onKeywordChange = { keyword = it; onKeywordChange(keyword) }
        )

        val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

        if (countryNamesAndCallingCodesSearchResultProvider().isEmpty())
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
        else
            LazyColumn {
                items(
                    items = countryNamesAndCallingCodesSearchResultProvider(),
                    key = { it.alpha2Code }) { country ->
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
fun TopBar(
    onNavigateBack: () -> Unit,
    keywordProvider: () -> String,
    onKeywordChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(R.drawable.ic_baseline_keyboard_backspace_24),
            contentDescription = null,
            modifier = Modifier
                .clickable(onClick = { onNavigateBack() })
                .padding(3.dp)
        )

        var hasFocus by rememberSaveable { mutableStateOf(false) }

        BasicTextField(
            value = keywordProvider(),
            onValueChange = { onKeywordChange(it) },
            modifier = Modifier.onFocusEvent { focusState ->
                if (!hasFocus) hasFocus = focusState.hasFocus
            }) { innerTextField ->
            Box(
                modifier = Modifier
                    .height(35.dp)
                    .fillMaxWidth(.85f)
                    .padding(5.dp)
                    .border(width = 1.dp, Color.LightGray, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier.padding(start = 5.dp, end = 5.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (hasFocus)
                        Box(modifier = Modifier.padding(start = 5.dp)) {
                            innerTextField()
                        }
                    else
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_baseline_search_24),
                                contentDescription = "countrySearch",
                                alpha = .8f
                            )
                            Text("Tìm nhanh", color = Color.Gray)
                        }
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
            .height(IntrinsicSize.Max)
            .clickable { onClickItem(country) }
            .background(Color(if (colorAlternatorProvider() == 0) 0xFFEEFEF1 else 0xFFEAF8ED)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = country?.name ?: "", modifier = Modifier
                .fillMaxWidth(.8f)
                .padding(start = 5.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(.4f)
                .fillMaxHeight()
                .background(Color(if (colorAlternatorProvider() == 0) 0xFFEAFFEE else 0xFFEBFEEF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = country?.alpha2Code ?: "", textAlign = TextAlign.Center
            )
        }

        Text(
            text = "${country?.callingCodes?.get(0) ?: ""}",
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
    Column(modifier = Modifier*/
/*.fillMaxSize()*//*
) {
        CountryNamesAndCallingCodesRow(
            CountryNamesAndCallingCodesModel(
                name = "Vietnam",
                alpha2Code = "VN",
                callingCodes = listOf("84")
            ),
            { 1 % 2 }
        ) {}
        CountryNamesAndCallingCodesRow(
            CountryNamesAndCallingCodesModel("El Salvador", "EL", listOf("1")),
            { 2 % 2 }
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar(keywordProvider = { "" }, onKeywordChange = {}, onNavigateBack = {})
}*/
