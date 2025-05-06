package com.example.bingetracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.bingetracker.R

val CourierPrime = FontFamily(
    Font(R.font.courierprime_regular, FontWeight.Normal),
    Font(R.font.courierprime_bold, FontWeight.Bold),
    Font(R.font.courierprime_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.courierprime_bolditalic, FontWeight.Bold, FontStyle.Italic)
    // Add more weights/styles as needed
)

// Set of Material typography styles to start with
//    bodyLarge = TextStyle(
//        fontFamily = CourierPrime,
//        fontWeight = FontWeight.Normal,
//        fontSize = 16.sp,
//        lineHeight = 24.sp,
//        letterSpacing = 0.5.sp
//    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = CourierPrime),
    displayMedium = TextStyle(fontFamily = CourierPrime),
    displaySmall = TextStyle(fontFamily = CourierPrime),
    headlineLarge = TextStyle(fontFamily = CourierPrime),
    headlineMedium = TextStyle(fontFamily = CourierPrime),
    headlineSmall = TextStyle(fontFamily = CourierPrime),
    titleLarge = TextStyle(fontFamily = CourierPrime),
    titleMedium = TextStyle(fontFamily = CourierPrime),
    titleSmall = TextStyle(fontFamily = CourierPrime),
    bodyLarge = TextStyle(
        fontFamily = CourierPrime,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(fontFamily = CourierPrime),
    bodySmall = TextStyle(fontFamily = CourierPrime),
    labelLarge = TextStyle(fontFamily = CourierPrime),
    labelMedium = TextStyle(fontFamily = CourierPrime),
    labelSmall = TextStyle(fontFamily = CourierPrime)
)

