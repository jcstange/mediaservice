package com.example.mediaservice.koin

import com.example.mediaservice.MainViewModel
import com.example.mediaservice.network.Repository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModules = module {
  single { Repository() }
  viewModel { MainViewModel(get()) }
}