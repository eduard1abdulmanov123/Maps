package com.example.maps

import android.annotation.SuppressLint
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient

class MapPresenter:MapContract.Presenter {

    companion object{
        const val DEFAULT_ZOOM = 17f
    }

    private var view:MapContract.View? = null
    private var geocoder:Geocoder? = null

    override fun attach(view: MapContract.View,geocoder: Geocoder) {
        this.view = view
        this.geocoder = geocoder
    }

    override fun detach() {
        view = null
        geocoder = null
    }

    @SuppressLint("MissingPermission")
    override fun fetchLastLocation(fusedLocationProviderClient: FusedLocationProviderClient) {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            //Делаем проверку, потому что на эмуляторе не определяется местоположение(требуется настройка)
            if(it!=null){
                view?.moveCamera(it.latitude,it.longitude, DEFAULT_ZOOM)
            }else{
                view?.showMessage(R.string.current_location_not_found)
            }
        }
    }

    override fun fetchAddress(latitude: Double, longitude: Double) {
        val addresses = geocoder?.getFromLocation(latitude,longitude,1)
        if(!addresses.isNullOrEmpty()) {
            view?.showAddress(addresses[0].getAddressLine(0))
            return
        }
        view?.showAddress(null)
    }
}