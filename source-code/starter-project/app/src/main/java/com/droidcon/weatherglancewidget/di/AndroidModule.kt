package com.droidcon.weatherglancewidget.di

import com.droidcon.weatherglancewidget.data.WeatherRepository
import org.koin.dsl.module

val androidModule = module {
    single { WeatherRepository(get()) }
}