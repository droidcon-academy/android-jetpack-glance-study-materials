package com.droidcon.weatherglancewidget.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.droidcon.weatherglancewidget.data.PlaceItem
import com.droidcon.weatherglancewidget.ui.theme.WeatherGlanceWidgetTheme
import kotlinx.coroutines.launch


class ConfigurationActivity : ComponentActivity() {

    private val places = listOf(
        PlaceItem(name = "Lisbon", latitude = 38.736946, longitude = -9.142685),
        PlaceItem(name = "Sopot", latitude = 54.444092, longitude = 18.570328),
        PlaceItem(name = "Amsterdam", latitude = 52.377956, longitude = 4.897070),
        PlaceItem(name = "Palo Alto", latitude = 37.468319, longitude = -122.143936),
        PlaceItem(name = "Oslo", latitude = 59.911491, longitude = 10.757933),
        PlaceItem(name = "Reykjavík", latitude = 64.128288, longitude = -21.827774)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherGlanceWidgetTheme {
                Scaffold { paddingValues ->
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        content = {
                            items(items = places, itemContent = {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Button(onClick = { onItemClick(item = it) }) {
                                        Text(text = it.name)
                                    }
                                }
                            })
                        })
                }
            }
        }
    }

    private fun onItemClick(item: PlaceItem) {
        val context = this
        lifecycleScope.launch {
            val glanceId =
                GlanceAppWidgetManager(context).getGlanceIds(
                    WeatherWidget::class.java
                ).last()
            WeatherWidget().apply {
                updateAppWidgetState(context, glanceId) {
                    WidgetStateHelper.saveLocation(
                        it,
                        latitude = item.latitude,
                        longitude = item.longitude
                    )
                    WidgetStateHelper.saveAddress(it, address = item.name)
                }
                update(context, glanceId)
            }
            startWeatherWorker(latitude = item.latitude, longitude = item.longitude)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}