package com.example.maps

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.maps.model.LatLng
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity(),MapContract.View {

    companion object{
        const val REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100
    }

    lateinit var presenter: MapContract.Presenter
    lateinit var map:GoogleMap
    private var locationPermissionGranted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter = MapPresenter()
        presenter.attach(this, Geocoder(this))
        showEmptyProgress(true)
        getLocationPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            REQUEST_ID_ACCESS_COURSE_FINE_LOCATION -> {
                locationPermissionGranted = true
                if(grantResults.isNotEmpty()){
                   for(grant in grantResults){
                       if(grant!=PackageManager.PERMISSION_GRANTED){
                           showMessage(R.string.not_permission)
                           locationPermissionGranted = false
                           break
                       }
                   }
                }
                initMap()
            }
            else-> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun showEmptyProgress(show:Boolean){
        if(show){
            container_map.visibility = View.GONE
            empty_progress_bar.visibility = View.VISIBLE
        }else{
            container_map.visibility = View.VISIBLE
            empty_progress_bar.visibility = View.GONE
        }
    }

    override fun showMessage(stringID: Int) {
        Snackbar.make(root,stringID,Snackbar.LENGTH_SHORT).show()
    }

    override fun moveCamera(latitude: Double, longitude: Double, zoom: Float) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude,longitude),zoom))
    }

    override fun showAddress(address: String?) {
        val addr = address ?: baseContext.getString(R.string.address_not_found)
        address_text_view.text = addr
    }

    private fun getLocationPermission(){
        val accessCoarsePermission = ContextCompat.checkSelfPermission(this,ACCESS_COARSE_LOCATION)
        val accessFinePermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
        if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED && accessFinePermission != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_ID_ACCESS_COURSE_FINE_LOCATION)
            return
        }
        locationPermissionGranted = true
        initMap()
    }

    @SuppressLint("MissingPermission")
    private fun initMap(){
        (fragment_maps as SupportMapFragment).getMapAsync {
            showEmptyProgress(false)
            map = it
            if(locationPermissionGranted){
                with(map){
                    uiSettings.isMyLocationButtonEnabled = true
                    isMyLocationEnabled = true
                    map.setOnCameraIdleListener {
                        container_address.visibility = View.VISIBLE
                        val tatLng = map.cameraPosition.target
                        presenter.fetchAddress(tatLng.latitude,tatLng.longitude)
                    }
                    map.setOnCameraMoveListener {
                        container_address.visibility = View.GONE
                    }
                }
                presenter.fetchLastLocation(LocationServices.getFusedLocationProviderClient(this))
            }
        }
    }
}
