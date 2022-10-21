package com.example.firebaseauth.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CountryNamesAndCallingCodesScreen(onNavigateToAuthHomeScreen: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Back",
            modifier = Modifier.clickable(onClick = { onNavigateToAuthHomeScreen() })
        )
    }
}