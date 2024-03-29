package com.example.firebaseauth.ui

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.paging.Pager
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.example.firebaseauth.R
import com.example.firebaseauth.services.CountryNamesAndCallingCodeModel
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import timber.log.Timber

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
            onKeywordChange = { onKeywordChange(it) }) {
            Box(modifier = Modifier.padding(end = 5.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_clear_24),
                    contentDescription = "clear keyword",
                    modifier = Modifier
                        .clickable { onKeywordChange("") }
                )
            }
        }
    }
}

@Composable
fun SearchBox(
    keywordProvider: () -> String,
    onKeywordChange: (String) -> Unit,
    trailingIcon: @Composable () -> Unit
) {
    var searchBoxHasFocus by rememberSaveable {
        mutableStateOf(false)
    }

    val keyword = keywordProvider()

    BasicTextField(
        value = keyword,
        onValueChange = { onKeywordChange(it) },
        modifier = Modifier.onFocusChanged { focusState ->
            searchBoxHasFocus = focusState.hasFocus
        },
        textStyle = TextStyle(color = MaterialTheme.colors.onBackground),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colors.onBackground)
    ) { innerTextField ->
        @Composable
        fun InputAndPlaceHolder() {
            Box(
                modifier = Modifier
                    .padding(start = 5.dp, end = if (keyword.isEmpty()) 10.dp else 31.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(modifier = Modifier.padding(start = 5.dp)) {
                    innerTextField()
                }
                if (keyword.isEmpty())
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

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(.98f),
            contentAlignment = Alignment.CenterEnd
        ) {
            val density = LocalDensity.current

            var searchBoxWidthWithFocus by rememberSaveable {
                mutableStateOf(0)
            }

            if (searchBoxHasFocus)
                searchBoxWidthWithFocus = with(density) { maxWidth.roundToPx() }

            val searchBoxWidthAnim by animateIntAsState(
                targetValue = searchBoxWidthWithFocus,
                animationSpec = spring(dampingRatio = 1.8f)
            )

            Box(
                modifier = Modifier
                    .height(35.dp)
                    .padding(5.dp)
                    .border(width = 1.dp, Color.LightGray, shape = RoundedCornerShape(12.dp))
                    .let {
                        if (searchBoxHasFocus)
                            it
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(
                                        Constraints(
                                            constraints.minWidth,
                                            searchBoxWidthAnim,
                                            constraints.minHeight,
                                            constraints.maxHeight
                                        )
                                    )
                                    layout(
                                        searchBoxWidthAnim - with(density) { 10.dp.roundToPx() }, // this has each side padding of 5.dp
                                        placeable.height
                                    ) {
                                        placeable.placeRelative(0, 0)
                                    }
                                }
                                .drawBehind {
                                    Timber.d("drawBehind search box")
                                    drawLine(
                                        color = Color.Magenta,
                                        start = Offset(0f, 10f),
                                        end = Offset(
                                            searchBoxWidthAnim.toFloat(), 10f
                                        )
                                    )
                                }
                        else
                            it
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                InputAndPlaceHolder()
            }

            if (keyword.isNotEmpty())
                trailingIcon()
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
            .background(
                Color(
                    if (colorAlternatorProvider() == 0)
                        if (MaterialTheme.colors.isLight) 0xFFEEFEF1 else 0xff262727
                    else
                        if (MaterialTheme.colors.isLight) 0xFFEAF8ED else 0xff1E1F1F
                )
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = country.name, modifier = Modifier
                .fillMaxWidth(.8f)
                .padding(start = 5.dp), color = MaterialTheme.colors.onBackground
        )

        Box(modifier = Modifier.fillMaxWidth())
        {
            Box(
                modifier = Modifier
                    .fillMaxWidth(.4f)
                    .fillMaxHeight()
                    .background(
                        Color(
                            if (colorAlternatorProvider() == 0)
                                if (MaterialTheme.colors.isLight) 0xFFEAFFEE else 0xff2E2F2
                            else
                                if (MaterialTheme.colors.isLight) 0xFFEBFEEF else 0xff242626
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = country.alpha2Code,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = if (country.callingCodes.isNotEmpty()) country.callingCodes[0] else "",
                    modifier = Modifier
                        .padding(end = 5.dp),
                    textAlign = TextAlign.Right, color = MaterialTheme.colors.onBackground
                )
            }
        }
    }
}

@Preview(name = "Dark")
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
                { 0 % 2 }
            ) {}
            CountryNamesAndCallingCodesRow(
                CountryNamesAndCallingCodeModel("El Salvador", "EL", listOf("1")),
                { 1 % 2 }
            ) {}
        }
    }
}

@Preview
@Composable
fun TopBarPreview() {
    FirebaseAuthTheme(darkTheme = false) {
        TopBar(keywordProvider = { "" }, onKeywordChange = {}, onNavigateBack = {})
    }
}