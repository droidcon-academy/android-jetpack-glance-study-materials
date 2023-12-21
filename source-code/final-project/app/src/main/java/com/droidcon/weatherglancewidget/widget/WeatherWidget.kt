package com.droidcon.weatherglancewidget.widget

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.droidcon.weatherglancewidget.MainActivity
import com.droidcon.weatherglancewidget.R
import com.droidcon.weatherglancewidget.ui.theme.GlanceColorScheme
import com.droidcon.weatherglancewidget.ui.theme.GlanceTextStyles
import java.util.concurrent.TimeUnit

class WeatherWidget : GlanceAppWidget() {
    companion object {
        const val UNIQUE_WORK_TAG = "WeatherWidgetWork"
    }

    override val sizeMode: SizeMode
        get() = SizeMode.Responsive(setOf(DpSize(110.dp, 50.dp), DpSize(160.dp, 50.dp)))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme(
                colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    GlanceTheme.colors
                } else {
                    GlanceColorScheme.colors
                }
            ) {
                WidgetContent()
            }
        }
    }

    @Composable
    fun WidgetContent() {
        val state = WidgetStateHelper.getState(currentState())
        Box(
            modifier = GlanceModifier.fillMaxSize()
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        GlanceModifier.background(GlanceTheme.colors.tertiaryContainer).cornerRadius(6.dp)
                    } else {
                        GlanceModifier.background(ImageProvider(resId = R.drawable.shape_widget_small))
                    }
                )
                .appWidgetBackground(),
            contentAlignment = Alignment.Center
        ) {
            when (state.data.temperature) {
                Int.MIN_VALUE -> InitialView()
                else -> WidgetBody(state = state)
            }
        }
    }

    @Composable
    fun WidgetBody(state: WidgetState) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(
                    onClick =
                    actionStartActivity(
                        activity = MainActivity::class.java,
                        parameters = actionParametersOf(
                            ActionParameters.Key<Double>(WidgetConst.LOCATION_WIDGET_LATITUDE) to state.data.latitude,
                            ActionParameters.Key<Double>(WidgetConst.LOCATION_WIDGET_LONGITUDE) to state.data.longitude,
                            ActionParameters.Key<String>(WidgetConst.WIDGET_NAME_KEY) to (state.data.address
                                ?: "")
                        )
                    )
                )
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(start = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = GlanceModifier.size(12.dp),
                    provider = ImageProvider(resId = R.drawable.outline_location_on_24),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onTertiaryContainer),
                    contentDescription = "Location icon"
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    text = state.data.address ?: "null",
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Start,
                        color = GlanceTheme.colors.onTertiaryContainer
                    ),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                if (state.loading.not()) {
                    Image(
                        modifier = GlanceModifier.size(32.dp).padding(6.dp)
                            .clickable(onClick = actionRunCallback<WidgetRefreshAction>()),
                        provider = ImageProvider(resId = R.drawable.ic_refresh),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onTertiaryContainer),
                        contentDescription = "Refresh icon"
                    )
                } else {
                    Box(
                        modifier = GlanceModifier.padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(GlanceModifier.size(20.dp))
                    }
                }
            }
            Spacer(GlanceModifier.defaultWeight())
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Image(
                    provider = ImageProvider(R.drawable.outline_thermostat_24),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onTertiaryContainer),
                    contentDescription = "temperature icon"
                )
                Spacer(GlanceModifier.width(2.dp))
                Text(
                    text = state.data.temperature.toString() + " °C",
                    style = GlanceTextStyles.bodyLarge(colorProvider = GlanceTheme.colors.onTertiaryContainer),
                    maxLines = 1
                )
                Spacer(GlanceModifier.defaultWeight())
                if (LocalSize.current.width > 120.dp) {
                    Image(
                        provider = ImageProvider(R.drawable.outline_air_24),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onTertiaryContainer),
                        contentDescription = "wind icon"
                    )
                    Spacer(GlanceModifier.width(4.dp))
                    Text(
                        text = state.data.windSpeed.toString(),
                        style = GlanceTextStyles.body(colorProvider = GlanceTheme.colors.onTertiaryContainer),
                        maxLines = 1
                    )
                    Text(
                        text = "m/s",
                        style = GlanceTextStyles.body(colorProvider = GlanceTheme.colors.onTertiaryContainer),
                        maxLines = 1
                    )
                }
            }
        }
    }

    @Composable
    fun InitialView() {
        Column(
            modifier = GlanceModifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator()
            Spacer(GlanceModifier.size(4.dp))
            Text(text = "Loading data ...", style = GlanceTextStyles.body(colorProvider = GlanceTheme.colors.onTertiaryContainer))
        }
    }
}

class WidgetRefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val prefs = WeatherWidget().getAppWidgetState<Preferences>(context, glanceId)
        val location =
            WidgetStateHelper.getState(prefs).let { it.data.latitude to it.data.longitude }
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<WeatherWidgetWorker>()
            .setInputData(
                WeatherWidgetWorker.buildData(
                    latitude = location.first,
                    longitude = location.second
                )
            )
            .build()
        WorkManager.getInstance(context)
            .enqueue(oneTimeWorkRequest)
    }
}

fun Context.startWeatherWorker(latitude: Double, longitude: Double) {
    val networkConstraint =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    val request = PeriodicWorkRequest
        .Builder(WeatherWidgetWorker::class.java, 15, TimeUnit.MINUTES)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5000L, TimeUnit.MILLISECONDS)
        .setInputData(
            WeatherWidgetWorker.buildData(latitude, longitude)
        )
        .setConstraints(networkConstraint)
        .build()
    val uniqueTag = WeatherWidget.UNIQUE_WORK_TAG + "_$latitude" + "_$longitude"
    WorkManager.getInstance(this)
        .enqueueUniquePeriodicWork(
            uniqueTag,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
}