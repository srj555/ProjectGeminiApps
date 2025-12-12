package com.projectduoplay.shared

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit = {}
) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Fade in (600ms)
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = LinearEasing)
        )
        // Hold (1000ms)
        delay(1000)
        // Fade out (400ms)
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        )
        // Total: 2 seconds
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "DUO PLAY",
            fontSize = 48.sp,
            fontWeight = FontWeight.Light,
            color = Color.White,
            letterSpacing = 8.sp,
            modifier = Modifier.alpha(alpha.value)
        )
    }
}
