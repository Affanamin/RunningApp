package com.androidappwp.runningapp.ui.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.androidappwp.runningapp.R
import com.androidappwp.runningapp.other.CustomMarkerView
import com.androidappwp.runningapp.other.TrackingUtility
import com.androidappwp.runningapp.ui.viewmodels.MainViewModel
import com.androidappwp.runningapp.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import java.lang.Math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()

    }

    private fun setupBarChart(){
       barChart.xAxis.apply {
           position = XAxis.XAxisPosition.BOTTOM
           setDrawLabels(false)
           axisLineColor = Color.WHITE
           textColor = Color.WHITE
           setDrawAxisLine(false)
       }
       barChart.axisLeft.apply {
           axisLineColor = Color.WHITE
           textColor = Color.WHITE
           setDrawGridLines(false)
       }
        barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        barChart.apply {
            description.text = "Avg Speed Over Time"
            legend.isEnabled = false

        }



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

        viewModel.runSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val allAvgSpeeds = it.indices.map{i -> BarEntry(i.toFloat(),it[i].avgSpeedInKMH)}
                val bardataset = BarDataSet(allAvgSpeeds,"Avg Speed Over Time").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }
                barChart.data = BarData(bardataset)
                barChart.marker = CustomMarkerView(it.reversed(),requireContext(),R.layout.marker_view)
                barChart.invalidate()
            }
        })



    }

}