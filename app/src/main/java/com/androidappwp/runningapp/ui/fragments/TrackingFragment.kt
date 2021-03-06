package com.androidappwp.runningapp.ui.fragments

import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.androidappwp.runningapp.R
import com.androidappwp.runningapp.db.Run
import com.androidappwp.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.androidappwp.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.androidappwp.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.androidappwp.runningapp.other.Constants.MAP_ZOOM
import com.androidappwp.runningapp.other.Constants.POLY_LINE_COLOR
import com.androidappwp.runningapp.other.Constants.POLY_LINE_WIDTH
import com.androidappwp.runningapp.other.TrackingUtility
import com.androidappwp.runningapp.services.Polyline
import com.androidappwp.runningapp.services.TrackingService
import com.androidappwp.runningapp.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import javax.inject.Inject
import kotlin.math.round


@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {


    private val viewModel: MainViewModel by viewModels()
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var map: GoogleMap ?= null
    private var curTimeInMillis = 0L
    private var menu:Menu? = null

    @set:Inject
    var weight = 80f


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView.onCreate(savedInstanceState)

        btnToggleRun.setOnClickListener {
            //sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
            toggleRun()
        }

        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

        mapView.getMapAsync{
            map = it
            addAllPolyLines()
        }

        subscribeToObservers()


    }

    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking((it))
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyLine()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            tvTimer.text = formattedTime
        })
    }

    private fun toggleRun(){
        if(isTracking){
            menu?.getItem(0)?.isVisible =true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(curTimeInMillis > 0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }

        return super.onOptionsItemSelected(item)

    }

    private fun showCancelTrackingDialog(){
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel th Run?")
            .setMessage("Are you Sure to Cancel the Current Run and Delete all its Data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){ _,_ ->
                stopRun()
            }
            .setNegativeButton("No"){ dialogInterface, _ ->
                dialogInterface.cancel()

            }

            .create()
        dialog.show()
    }


    private fun stopRun(){
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking:Boolean){
        this.isTracking = isTracking
        if(!isTracking){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        }else{
            btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible =true
            btnFinishRun.visibility = View.GONE
        }

    }


    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM

                )
            )
        }
    }

    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        for(polyine in pathPoints){
            for (pos in polyine){
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()

            )
        )
    }

    private fun endRunAndSaveToDb(){
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for(polyline in pathPoints){
                distanceInMeters += TrackingUtility.calculatePolyLineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60 ) * 10)/10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(bmp,dateTimeStamp,avgSpeed, distanceInMeters, curTimeInMillis,caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run Saved Successfully",
                Snackbar.LENGTH_LONG
            ).show()

            stopRun()

        }
    }

    private fun addAllPolyLines(){
        for(polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .color(POLY_LINE_COLOR)
                .width(POLY_LINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyLine(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastLatLng = pathPoints.last()[pathPoints.last().size-2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLY_LINE_COLOR)
                .width(POLY_LINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    // Life Cycle Functions

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }


    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        mapView?.onDestroy()
//    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)

    }
}