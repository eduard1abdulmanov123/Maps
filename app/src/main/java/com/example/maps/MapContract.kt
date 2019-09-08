package com.example.maps

import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient

interface MapContract {

    interface View{

        fun showEmptyProgress(show:Boolean)

        fun showMessage(stringID:Int)

        fun moveCamera(latitude: Double, longitude: Double,zoom:Float)

        fun showAddress(address: String?)
    }

    interface Presenter{
        fun attach(view:View,geocoder: Geocoder)

        fun detach()

        fun fetchLastLocation(fusedLocationProviderClient: FusedLocationProviderClient)

        fun fetchAddress(latitude:Double,longitude:Double)
    }
}