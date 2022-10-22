package com.example.firebaseauth.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.firebaseauth.data.CountryNamesAndCallingCodesModel

@Composable
fun CountryNamesAndCallingCodesScreen(onNavigateToAuthHomeScreen: () -> Unit) {
    Column {
        Text(
            text = "Back",
            modifier = Modifier.clickable(onClick = { onNavigateToAuthHomeScreen() })
        )
        /*val pager = remember {
            Pager(PagingConfig(pageSize = 100, enablePlaceholders = true)){}
        }*/
    }
}