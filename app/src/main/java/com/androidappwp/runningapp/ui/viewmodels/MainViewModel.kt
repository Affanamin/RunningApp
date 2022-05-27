package com.androidappwp.runningapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.androidappwp.runningapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


// The Job of our Main view Model is to collect data from our repository and provide it for all those fragments that we will be
// needing in this view model

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel(){

// We do not want to add viewmodel factory class here because this will all be handled by dagger hilt behind the scenes. We just use
// @HiltViewModel

}
