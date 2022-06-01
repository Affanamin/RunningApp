package com.androidappwp.runningapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.androidappwp.runningapp.R
import com.androidappwp.runningapp.other.TrackingUtility
import com.androidappwp.runningapp.ui.viewmodels.MainViewModel
import com.androidappwp.runningapp.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import java.lang.Math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()

    }

    private fun subscribeToObservers(){
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text = totalTimeRun
            }
        })

        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it.let {
                val km = it / 1000f
                val totalistance = round(km * 10f)/10f
                val totalDistanceString = "${totalistance}km"
                tvTotalDistance.text = totalDistanceString
            }
        })

        //Avg Speed
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/hr"
                //tvAverageSpeed.text = avgSpeedString
                tvAverageSpeed.text = "50km/hr"

            }
        })

        //Calories Burned

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it.let {
                val caloriesBurnedString = "${it}kcal"
                //tvTotalCalories.text = caloriesBurnedString
                tvTotalCalories.text = "250kcal"
            }
        })



    }

}