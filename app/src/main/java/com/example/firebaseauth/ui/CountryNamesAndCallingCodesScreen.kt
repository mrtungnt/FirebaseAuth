package com.example.firebaseauth.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
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
        Row() {
            Image(
                painter = painterResource(R.drawable.ic_baseline_keyboard_backspace_24),
                contentDescription = null,
                modifier = Modifier
                    .clickable(onClick = { onNavigateToAuthHomeScreen() })
                    .padding(3.dp)
            )


        }

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
fun TopBar() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(R.drawable.ic_baseline_keyboard_backspace_24),
            contentDescription = null,
            modifier = Modifier
                .clickable(onClick = { })
                .padding(3.dp)
        )

        BasicTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .width(300.dp)
//                .height(40.dp)
                .padding(5.dp)
                .border(
                    border = BorderStroke(width = 1.dp, color = Color.LightGray),
                    shape = MaterialTheme.shapes.medium
                ),
//            shape = MaterialTheme.shapes.medium
        )
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
    Column(modifier = Modifier/*.fillMaxSize()*/) {
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
    TopBar()
}