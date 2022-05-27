package com.androidappwp.runningapp.repositories

import com.androidappwp.runningapp.db.Run
import com.androidappwp.runningapp.db.RunDao
import javax.inject.Inject

// Repository class
// The job of repository is just to collect data from out of our data sources, SO In this case we only have our room database
// as a data source but if we would need access to an API - then we would also get the data from API of our Repository

class MainRepository @Inject constructor(

    val runDao: RunDao
) {

    suspend fun insertRun(run:Run) = runDao.insertRun(run)

    suspend fun deleteRun(run:Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurnt()


    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurnt()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()

}