package com.droidcon.weatherglancewidget.di

import com.droidcon.weatherglancewidget.data.WeatherRepository
import com.droidcon.weatherglancewidget.widget.WeatherWidgetWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val androidModule = module {
    single { WeatherRepository(get()) }
    worker { WeatherWidgetWorker(get(), androidContext(), it.get()) }
}