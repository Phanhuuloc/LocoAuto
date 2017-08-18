package com.sungwoo.aps.mobile.activities

import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Handler
import android.widget.Button
import android.widget.TextView

import com.google.android.gms.maps.GoogleMap

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.LocationListener
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.view.View
import butterknife.bindView
import com.sungwoo.aps.mobile.R
import com.sungwoo.aps.mobile.common.utils.getBitmapDescriptor
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.sungwoo.aps.mobile.activities.LocoAutoBaseActivity
import io.cloudboost.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.NullPointerException
import java.util.ArrayList

/**
 * Created by localadmin on 8/16/2017.
 */

class PickupLocationActivity : LocoAutoBaseActivity(), OnMapReadyCallback, LocationListener {

    val requestStatusView: TextView by bindView(R.id.requestStatusView)
    val requestButton: Button by bindView(R.id.requestButton)

    private var mMap: GoogleMap? = null

    lateinit var locationManager: LocationManager
    var provider: String = ""
    // For saving the user's location
    internal var usersCurrentLocation = Location(provider)
    // A custom marker
    lateinit var locIcon1: BitmapDescriptor
    lateinit var locIcon3: BitmapDescriptor

    // If user's request is still wanted
    internal var requestActive = false
    internal var driverLocation = Location(provider)

    // Handler that will update the rider and driver's location in DownloadTask (downloadTaskType = 'saveLocation')
    internal var handler = Handler()
    internal var anotherHandler = Handler()
    internal var reqHandler = Handler()
    lateinit var reqRunnable: Runnable

    lateinit var sharedPreferences: SharedPreferences

    internal var downloadTaskType = ""

    inner class DownloadTask : AsyncTask<String?, Void?, Void?>() {

        internal var resultGood = false
        internal var resultType = ""
        internal var resultType2 = ""

        internal var driverDistance: Double = 0.toDouble()
        internal var driverBearing: Float = 0.toFloat()

        override fun doInBackground(vararg params: String?): Void? {
            if (downloadTaskType == "request") {
                try {
                    val riderLocation = CloudGeoPoint(usersCurrentLocation.latitude, usersCurrentLocation.longitude)
                    val reqActualName = arrayOfNulls<String>(1)

                    // Saves the requester's actual name
                    val actualNameObject = CloudQuery("User")
                    actualNameObject.equalTo("username", sharedPreferences.getString("currentUser", ""))
                    actualNameObject.find(CloudObjectArrayCallback { x, t ->
                        if (x != null) {
                            for (`object` in x) {
                                reqActualName[0] = `object`.getString("actualName")
                            }
                        } else {
                            t.printStackTrace()
                        }
                    })

                    val requestsObject = CloudObject("Requests")
                    requestsObject.set("reqUsername", sharedPreferences.getString("currentUser", ""))
                    requestsObject.set("reqLocation", riderLocation)
                    requestsObject.set("reqActualName", reqActualName[0])
                    requestsObject.save { x, t ->
                        Log.i("Rider's Location", "Saved")
                        Log.i("Rider Requester", "Saved")
                        resultGood = true
                        requestActive = true
                    }

                } catch (e: CloudException) {
                    e.printStackTrace()
                }


            } else if (downloadTaskType == "cancelRequest") {
                try {
                    // See who gave the request
                    val userQuery = CloudQuery("Requests")
                    Log.d("Cancel Request", "Executed")
                    userQuery.equalTo("reqUsername", sharedPreferences.getString("currentUser", ""))
                    userQuery.find(CloudObjectArrayCallback { x, t ->
                        if (x != null) {
                            for (`object` in x) {
                                `object`.delete { x, t ->
                                    if (x != null) {
                                        Log.i("Request", "Deleted")
                                        resultGood = true
                                    } else {
                                        t.printStackTrace()
                                    }
                                }
                            }
                        } else {
                            t.printStackTrace()
                        }
                    })
                } catch (e: CloudException) {
                    e.printStackTrace()
                }


            } else if (downloadTaskType == "saveLocation") {
                try {
                    val riderLocation = CloudGeoPoint(usersCurrentLocation.latitude, usersCurrentLocation.longitude)
                    val driverUsername = arrayOf("")
                    // Updates the user's location
                    val userQuery = CloudQuery("Requests")
                    userQuery.equalTo("reqUsername", sharedPreferences.getString("currentUser", ""))
                    userQuery.find(CloudObjectArrayCallback { x, t ->
                        if (x != null) {
                            for (`object` in x) {
                                driverUsername[0] = `object`.getString("driverUsername")
                                `object`.set("reqLocation", riderLocation)
                                `object`.save { x, t ->
                                    Log.i("Rider's Location", "Saved")
                                    resultGood = true
                                }
                            }
                        } else {
                            t.printStackTrace()
                        }
                    })

                    // Updates the driver's location
                    val driverQuery = CloudQuery("User")
                    driverQuery.equalTo("username", driverUsername[0])
                    driverQuery.find(CloudObjectArrayCallback { x, t ->
                        if (x != null) {
                            if (x.size > 0) {
                                for (`object` in x) {
                                    try {
                                        driverBearing = java.lang.Float.parseFloat(`object`.getString("bearing"))
                                        val geopoint = JSONObject(`object`.get("location").toString())
                                        // retrieve the coordinates
                                        val coordinates = geopoint.getString("coordinates").replace("[", "").replace("]", "")
                                        val latLng = coordinates.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                                        val stringLat = latLng[1].replace(",", "")
                                        val stringLng = latLng[0].replace(",", "")
                                        val latitude = java.lang.Double.parseDouble(stringLat)
                                        val longitude = java.lang.Double.parseDouble(stringLng)
                                        driverLocation.latitude = latitude
                                        driverLocation.longitude = longitude
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }

                                }
                            }
                        } else {
                            t.printStackTrace()
                        }
                    })

                    if (driverLocation.latitude != 0.0 && driverLocation.longitude != 0.0) {
                        Log.i("Driver Location", driverLocation.latitude.toString() + "," + driverLocation.longitude)
                        val distance = usersCurrentLocation.distanceTo(driverLocation).toDouble()
                        driverDistance = (Math.round(distance * 0.000621371 * 100) / 100).toDouble()
                        resultGood = true
                        resultType = "driverDistance"
                        if (driverDistance == 0.0) {
                            resultType = "driverDistance0"
                            // Make a notification when the driver has arrived
                            val nBuilder = NotificationCompat.Builder(applicationContext)
                                    .setSmallIcon(R.mipmap.ic_start_location)
                                    .setContentTitle("Your Driver is Here!")
                                    .setContentText("Your driver has arrived at your location.")
                                    .setVibrate(longArrayOf(0, 1000, 500, 1000, 500))
                                    .setAutoCancel(true)
                            // Intent that executes when you click on the notification
                            val resultIntent = Intent(applicationContext, PickupLocationActivity::class.java)
                            val pendingIntent = PendingIntent.getActivity(
                                    applicationContext,
                                    0,
                                    resultIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            nBuilder.setContentIntent(pendingIntent)
                            // Issues the notification
                            val mNotificationId = 1 // Sets an ID for the notification

                            val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            // Builds the notification and issues it.
                            mNotifyMgr.notify(mNotificationId, nBuilder.build())

                            anotherHandler.postDelayed({
                                // Resets the location so the vibration stops
                                driverLocation = Location(provider)
                                val cancelRequestTask = DownloadTask()
                                downloadTaskType = "cancelRequest"
                                cancelRequestTask.execute("")
                            }, 3000)
                        }
                    }
                } catch (e: CloudException) {
                    e.printStackTrace()
                }

                handler.postDelayed({
                    if (requestActive) {
                        val updateLocations = DownloadTask()
                        downloadTaskType = "saveLocation"
                        updateLocations.execute("")
                    } else {
                        Log.i("Update Runnable", "Stopped")
                        handler.removeCallbacksAndMessages(null)
                    }
                }, 5000)
            } else if (downloadTaskType == "getCurrentRequests") {
                val myRequests = CloudQuery("Requests")
                myRequests.equalTo("reqUsername", sharedPreferences.getString("currentUser", ""))
                try {
                    myRequests.find(CloudObjectArrayCallback { x, t ->
                        if (x != null) {
                            if (x.size > 0) {
                                Log.i("My Requests Amount", x.size.toString())
                                for (`object` in x) {
                                    try {
                                        val possibleTerm = `object`.get("driverUsername").toString()
                                        if (possibleTerm == null || possibleTerm == "") {

                                        } else {
                                            val driverUsername = `object`.getString("driverUsername")
                                            Log.i("My Requests", "Driver Accepted: " + driverUsername)
                                            resultGood = true
                                            resultType2 = "yes"
                                            reqHandler.removeCallbacksAndMessages(reqRunnable)
                                        }
                                    } catch (n: NullPointerException) {
                                        Log.i("My Requests", "Driver Hasn't Accepted")
                                        resultGood = true
                                        resultType2 = "no"

                                        reqRunnable = Runnable {
                                            val seeRequestsTask = DownloadTask()
                                            downloadTaskType = "getCurrentRequests"
                                            seeRequestsTask.execute("")
                                        }
                                        reqHandler.postDelayed(reqRunnable, 5000)
                                    }

                                }
                            } else {
                                Log.i("My Requests", "None")
                                requestActive = false
                            }
                        } else {
                            t.printStackTrace()
                        }
                    })
                } catch (e: CloudException) {
                    e.printStackTrace()
                }

            }

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            if (resultGood) {
                if (downloadTaskType == "request") {
                    if (resultGood) {
                        requestStatusView.text = "Finding a driver..."
                        requestButton.visibility = View.VISIBLE
                        requestButton.text = "Cancel Request"
                        requestActive = true
                        val seeRequestsTask = DownloadTask()
                        downloadTaskType = "getCurrentRequests"
                        seeRequestsTask.execute("")
                    } else {
                        Log.i("Request Ride", "Not Successful")
                    }
                } else if (downloadTaskType == "cancelRequest") {
                    requestStatusView.visibility = View.INVISIBLE
                    requestButton.visibility = View.VISIBLE
                    requestButton.text = "Request Ride"

                } else if (downloadTaskType == "saveLocation") {
                    if (resultGood) {
                        var driverMarker = mMap!!.addMarker(MarkerOptions().position(LatLng(0.0, 0.0)))
                        if (resultType == "driverDistance") {
                            requestStatusView.text = "Your driver is " + driverDistance.toString() + " miles away."
                            requestStatusView.visibility = View.VISIBLE

                            // Ensures the two markers are in view
                            mMap!!.clear()
                            val builder = LatLngBounds.Builder()
                            val markers = ArrayList<Marker>()
                            markers.add(mMap!!.addMarker(MarkerOptions().position(LatLng(usersCurrentLocation.latitude, usersCurrentLocation.longitude))
                                    .title("Your Location")
                                    .icon(locIcon1)))
                            driverMarker = mMap!!.addMarker(MarkerOptions().position(LatLng(driverLocation.latitude, driverLocation.longitude))
                                    .title("Rider's Location")
                                    .icon(locIcon3)
                                    .rotation(driverBearing)
                                    .anchor(0.5f, 0.5f))
                            markers.add(driverMarker)

                            for (marker in markers) {
                                builder.include(marker.position)
                            }
                            val bounds = builder.build()
                            val padding = 100 // offset from edges of the map in pixels
                            val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                            mMap!!.animateCamera(cu)
                        } else if (resultType == "driverDistance0") {
                            requestActive = false
                            requestStatusView.text = "Your driver has arrived!"
                            requestStatusView.visibility = View.VISIBLE
                            requestButton.text = "Request Ride"
                            requestButton.visibility = View.VISIBLE
                            driverMarker.remove()
                        }
                    }
                } else if (downloadTaskType == "getCurrentRequests") {
                    if (resultGood) {
                        if (resultType2 == "yes") {
                            requestStatusView.text = "A driver is on their way!"
                            requestButton.visibility = View.INVISIBLE
                            val startUpdatingTask = DownloadTask()
                            downloadTaskType = "saveLocation"
                            startUpdatingTask.execute("")
                        } else {
                            requestStatusView.text = "Finding a driver..."
                            requestButton.visibility = View.VISIBLE
                        }
                        requestButton.text = "Cancel Request"
                        requestStatusView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    fun requestRide(view: View) {
        if (requestActive) {
            requestActive = false
            downloadTaskType = "cancelRequest"
            Log.i("Rider Map", "Ride Cancelled")
            val cancelRequestTask = DownloadTask()
            cancelRequestTask.execute("")
        } else {
            requestStatusView.visibility = View.VISIBLE
            downloadTaskType = "request"
            Log.i("Rider Map", "Ride Requested")
            val requestRideTask = DownloadTask()
            requestRideTask.execute("")
        }
    }

    fun startClient(view: View) {
        ClientActivity.start(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_location)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        sharedPreferences = this.getSharedPreferences(packageName, Context.MODE_PRIVATE)
        Log.i("My Username", sharedPreferences.getString("currentUser", ""))

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        provider = locationManager.getBestProvider(Criteria(),false)
        locIcon1 = getBitmapDescriptor(this,R.drawable.ic_my_location_24dp)
        locIcon3 = getBitmapDescriptor(this,R.drawable.ic_location_on_24dp)

        // All location services
        locationManager.requestLocationUpdates(provider, 400, 1f, this)

        try {
            val location = locationManager.getLastKnownLocation(provider)
            //if (location != null) {
            if (requestActive) {
                usersCurrentLocation = location
                val saveLocationTask = DownloadTask()
                downloadTaskType = "saveLocation"
                saveLocationTask.execute("")
            } else {
                // Checks to see if there are any current requests made by the current user
                usersCurrentLocation = location
                val getCurrentRequestsTask = DownloadTask()
                downloadTaskType = "getCurrentRequests"
                getCurrentRequestsTask.execute("")
            }
            //}
            usersCurrentLocation = location
            Log.i("Users Location", location.latitude.toString() + " " + location.longitude)
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 10f))
            mMap!!.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("Your Location").icon(locIcon1))
        } catch (n: NullPointerException) {

        }

    }

    override fun onResume() {
        super.onResume()
        locationManager.requestLocationUpdates(provider, 400, 1f, this)
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap!!.clear()
        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(usersCurrentLocation.latitude, usersCurrentLocation.longitude), 10f))
        mMap!!.addMarker(MarkerOptions()
                .position(LatLng(usersCurrentLocation.latitude, usersCurrentLocation.longitude))
                .title("Your Location")
                .icon(locIcon1))
    }

    override fun onLocationChanged(location: Location) {
        Log.i("Users Updated Location", location.latitude.toString() + " " + location.longitude)
        mMap!!.clear()
        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 10f))
        mMap!!.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("Your Location").icon(locIcon1))

        // Updates user's location in the database
        if (requestActive) {
            usersCurrentLocation = location
            val saveLocationTask = DownloadTask()
            downloadTaskType = "saveLocation"
            saveLocationTask.execute("")
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    override fun onBackPressed() {
        // Prevents the user from clicking the back button and returning to the signup page.
    }

    companion object Factory{
        fun start(context: Context) {
            val intent = Intent(context, PickupLocationActivity::class.java)
            context.startActivity(intent)
        }
    }
}