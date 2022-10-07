package com.example.firebaseauth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.firebaseauth.R
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis

@Composable
fun LandingScreen(isDoneProvider: () -> Boolean, onDone: () -> Unit) {
    val updateOnDone by rememberUpdatedState(newValue = onDone)
    val isDone by rememberUpdatedState(newValue = isDoneProvider())
    var shouldShowBlankScreen by remember {
        mutableStateOf(true)
    }

    if (shouldShowBlankScreen)
        Box(Modifier)
    else
        LogoAndSlogan()

    LaunchedEffect(key1 = Unit) {
        val t = measureTimeMillis() {
            delay(100)
            shouldShowBlankScreen = false
            while (true) {
                if (isDone) break
                delay(1)
            }
        }
        if (t < 2000L) delay(2000L - t)
        updateOnDone()
    }
}

@Composable
fun LogoAndSlogan(){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            null,
            modifier = Modifier.size(180.dp, 120.dp)
        )

//        Spacer(modifier = Modifier.height(12.dp))

//        Text("Có phải App này được tạo ra vì bạn không? Hãy khám phá!")
    }
}

@Preview(showBackground = true)
@Composable
fun LogoAndSloganPreview(){
    FirebaseAuthTheme {
        LogoAndSlogan()
    }
}