package com.droidcon.weatherglancewidget.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.glance.text.TextAlign
import androidx.glance.unit.ColorProvider

// Set of Material typography styles to start with
val Typography = Typography(
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)

object GlanceTextStyles {
    fun bodyLarge(textAlign: TextAlign = TextAlign.Center, colorProvider: ColorProvider) =
        androidx.glance.text.TextStyle(
            fontWeight = androidx.glance.text.FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = textAlign,
            color = colorProvider
        )

    fun body(textAlign: TextAlign = TextAlign.Center, colorProvider: ColorProvider) =
        androidx.glance.text.TextStyle(
            fontWeight = androidx.glance.text.FontWeight.Normal,
            fontSize = 14.sp,
            textAlign = textAlign,
            color = colorProvider
        )
}