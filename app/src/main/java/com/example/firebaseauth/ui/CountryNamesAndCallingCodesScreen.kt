package com.example.firebaseauth.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.paging.Pager
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.example.firebaseauth.R
import com.example.firebaseauth.services.CountryNamesAndCallingCodeModel
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme

@Composable
fun CountryNamesAndCallingCodesScreen(
    pager: Pager<Int, CountryNamesAndCallingCodeModel>,
    onSelectCountry: (String) -> Unit,
    onKeywordChange: (String) -> Unit,
    countryNamesAndCallingCodesSearchResultProvider: () -> List<CountryNamesAndCallingCodeModel>,
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

        if (keyword.isEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = lazyPagingItems, key = { it.alpha2Code }) { country ->
                    if (country != null) {
                        CountryNamesAndCallingCodesRow(
                            country = country,
                            colorAlternatorProvider = {
                                lazyPagingItems.itemSnapshotList.items.indexOf(
                                    country
                                ) % 2
                            }
                        ) {
                            onSelectCountry(
                                it.alpha2Code
                            )
                            onNavigateToAuthHomeScreen()
                        }
                    }
                }
            }
        } else {
            LazyColumn {
                items(
                    items = countryNamesAndCallingCodesSearchResultProvider(),
                    key = { it.alpha2Code }) { country ->
                    CountryNamesAndCallingCodesRow(
                        country = country,
                        colorAlternatorProvider = {
                            countryNamesAndCallingCodesSearchResultProvider().indexOf(
                                country
                            ) % 2
                        }
                    ) {
                        onSelectCountry(
                            it.alpha2Code
                        )
                        onNavigateToAuthHomeScreen()
                    }
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
                .padding(3.dp),
            colorFilter = ColorFilter.tint(Color(0xFF55AA00))
        )

        SearchBox(
            keywordProvider = { keywordProvider() },
            onKeywordChange = { onKeywordChange(it) })
    }
}

@Composable
fun SearchBox(keywordProvider: () -> String, onKeywordChange: (String) -> Unit) {
    var searchBoxHasFocus by rememberSaveable {
        mutableStateOf(false)
    }

    BasicTextField(
        value = keywordProvider(),
        onValueChange = { onKeywordChange(it) },
        modifier = Modifier.onFocusChanged { focusState ->
            searchBoxHasFocus = focusState.hasFocus
        },
        textStyle = TextStyle(color = MaterialTheme.colors.onBackground),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colors.onBackground)
    ) { innerTextField ->
        var searchBoxWidthWithFocus by remember {
            mutableStateOf(0.dp)
        }

        val searchBoxWidthAnim by animateDpAsState(
            targetValue = searchBoxWidthWithFocus ,
            animationSpec = spring(dampingRatio = 1.8f)
        )
        val density = LocalDensity.current

        @Composable
        fun InputAndPlaceHolder() {
            Box(
                modifier = Modifier
                    .padding(start = 5.dp, end = if (keywordProvider().isEmpty()) 10.dp else 31.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(modifier = Modifier.padding(start = 5.dp)) {
                    innerTextField()
                }
                if (keywordProvider().isEmpty())
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset {
                            IntOffset(
                                if (searchBoxHasFocus) 7.dp.roundToPx() else 5.dp.roundToPx(),
                                0
                            )
                        }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_baseline_search_24),
                            contentDescription = "countrySearch",
                            alpha = .8f
                        )
                        Text("Tìm nhanh", color = Color.Gray)
                    }

            }
        }

        if (searchBoxHasFocus)
            Box(
                modifier = Modifier
                    .fillMaxWidth(.85f)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        searchBoxWidthWithFocus = with(density) { placeable.width.toDp() }
                        layout(placeable.width, placeable.height) {}
                    }
            )

        Box(
            modifier = Modifier
                .height(35.dp)
                .padding(5.dp)
                .border(width = 1.dp, Color.LightGray, shape = RoundedCornerShape(12.dp)).let {
                    if (searchBoxHasFocus)
                        it.width(searchBoxWidthAnim)
                    else
                        it
                },
            contentAlignment = Alignment.CenterStart
        ) {
            InputAndPlaceHolder()

            if (keywordProvider().isNotEmpty())
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_clear_24),
                    contentDescription = "clear keyword",
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .align(Alignment.CenterEnd)
                        .clickable { onKeywordChange("") }
                )
        }
    }
}

@Composable
fun CountryNamesAndCallingCodesRow(
    country: CountryNamesAndCallingCodeModel,
    colorAlternatorProvider: () -> Int,
    onClickItem: (CountryNamesAndCallingCodeModel) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .clickable { onClickItem(country) }
            .background(Color(if (colorAlternatorProvider() == 0) 0xFFEEFEF1 else 0xFFEAF8ED)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = country.name, modifier = Modifier
                .fillMaxWidth(.8f)
                .padding(start = 5.dp), color = Color.Black
        )

        Box(modifier = Modifier.fillMaxWidth())
        {
            Box(
                modifier = Modifier
                    .fillMaxWidth(.4f)
                    .fillMaxHeight()
                    .background(Color(if (colorAlternatorProvider() == 0) 0xFFEAFFEE else 0xFFEBFEEF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = country.alpha2Code, textAlign = TextAlign.Center, color = Color.Black
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = if (country.callingCodes.isNotEmpty()) country.callingCodes[0] else "",
                    modifier = Modifier
                        .padding(end = 5.dp),
                    textAlign = TextAlign.Right, color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CountryNamesAndCallingCodesRowPreview() {
    FirebaseAuthTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            CountryNamesAndCallingCodesRow(
                CountryNamesAndCallingCodeModel(
                    name = "South Georgia and the South Sandwich Islands",
                    alpha2Code = "...",
                    callingCodes = listOf("84")
                ),
                { 1 % 2 }
            ) {}
            CountryNamesAndCallingCodesRow(
                CountryNamesAndCallingCodeModel("El Salvador", "EL", listOf("1")),
                { 2 % 2 }
            ) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    FirebaseAuthTheme(darkTheme = true) {
        TopBar(keywordProvider = { "" }, onKeywordChange = {}, onNavigateBack = {})
    }
}