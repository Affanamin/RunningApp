package com.androidappwp.runningapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RunningDatabase: RoomDatabase() {

    //abstract fun getRunDao(): RunDao

    // The Reason why this class is so very lite in comparison with news app , is because that we are using dragger Dependency Injections in
    // this project while we haven't used this in newsapp.. Dragger can take of the other code down here
}