package com.example.addharold

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Log.ASSERT
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import kotlinx.android.synthetic.main.activity_main.*

enum class ProviderType {
    BASIC
}

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    private val db = FirebaseFirestore.getInstance()
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        createMarker()
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        enableMyLocation()

        map.addPolygon(
            PolygonOptions().add(
                LatLng(-12.0, 12.0),
                LatLng(-200.0,200.0),
                LatLng(-100.0, 0.0)
            )
        )
        val polylineOptions = PolylineOptions()
        map.setOnMapLongClickListener {
            map.addMarker(MarkerOptions().position(it))


            polylineOptions.add(it)
            val polyline = map.addPolyline(polylineOptions)
        }

    }

    private fun createMarker() {
        val favoritePlace = LatLng(39.42283, -0.41542)
        val marker = MarkerOptions().position(favoritePlace).title("Holi")
        map.addMarker(marker)

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(favoritePlace, 18f),
            4000,
            null
        )
    }

    private fun isPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun enableMyLocation(){
        if (!::map.isInitialized) return
        if (isPermissionsGranted()){
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                map.isMyLocationEnabled = true
            }else{
                Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        createMapFragment()
        //setup
        val bundle = intent.extras

        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        setup(email ?: "", provider ?: "")

        logOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            // para volver a la pantalla anterior
            onBackPressed()
        }

        saveButton.setOnClickListener {
            // PAISES
            db.collection("paises").document("ES").set(
                hashMapOf(
                    "nombre" to "ESPAÑA",
                    "poblacion" to 47350000,
                )
            )
            db.collection("paises").document("FR").set(
                hashMapOf(
                    "nombre" to "FRANCIA",
                    "poblacion" to 67390000,
                )
            )
            db.collection("paises").document("IT").set(
                hashMapOf(
                    "nombre" to "ITALIA",
                    "poblacion" to 59550000,
                )
            )
            db.collection("paises").document("DE").set(
                hashMapOf(
                    "nombre" to "ALEMANIA",
                    "poblacion" to 83240000,
                )
            )
            db.collection("paises").document("UK").set(
                hashMapOf(
                    "nombre" to "INGLATERRA",
                    "poblacion" to 55980000,
                )
            )
            // CIUDADES
            db.collection("ciudades").document("mad").set(
                hashMapOf(
                    "nombre" to "Madrid",
                    "poblacion" to 3223000,
                    "cod_pais" to "ES",
                )
            )
            db.collection("ciudades").document("val").set(
                hashMapOf(
                    "nombre" to "Valencia",
                    "poblacion" to 791413,
                    "cod_pais" to "ES",
                )
            )
            db.collection("ciudades").document("bar").set(
                hashMapOf(
                    "nombre" to "Barcelona",
                    "poblacion" to 1620000,
                    "cod_pais" to "ES",
                )
            )
            db.collection("ciudades").document("par").set(
                hashMapOf(
                    "nombre" to "París",
                    "poblacion" to 2161000,
                    "cod_pais" to "FR",
                )
            )
            db.collection("ciudades").document("tou").set(
                hashMapOf(
                    "nombre" to "Toulouse",
                    "poblacion" to 471941,
                    "cod_pais" to "FR",
                )
            )
            db.collection("ciudades").document("ly").set(
                hashMapOf(
                    "nombre" to "Lyon",
                    "poblacion" to 513275,
                    "cod_pais" to "FR",
                )
            )
            db.collection("ciudades").document("rom").set(
                hashMapOf(
                    "nombre" to "Roma",
                    "poblacion" to 2873000,
                    "cod_pais" to "IT",
                )
            )
            db.collection("ciudades").document("mil").set(
                hashMapOf(
                    "nombre" to "Milán",
                    "poblacion" to 1352000,
                    "cod_pais" to "IT",
                )
            )
            db.collection("ciudades").document("nap").set(
                hashMapOf(
                    "nombre" to "Nápoles",
                    "poblacion" to 3085000,
                    "cod_pais" to "IT",
                )
            )
            db.collection("ciudades").document("mun").set(
                hashMapOf(
                    "nombre" to "Munich",
                    "poblacion" to 1472000,
                    "cod_pais" to "DE",
                )
            )
            db.collection("ciudades").document("ber").set(
                hashMapOf(
                    "nombre" to "Berlin",
                    "poblacion" to 3645000,
                    "cod_pais" to "DE",
                )
            )
            db.collection("ciudades").document("stu").set(
                hashMapOf(
                    "nombre" to "Stuttgart",
                    "poblacion" to 634830,
                    "cod_pais" to "DE",
                )
            )
            db.collection("ciudades").document("lon").set(
                hashMapOf(
                    "nombre" to "Londres",
                    "poblacion" to 8982000,
                    "cod_pais" to "UK",
                )
            )
            db.collection("ciudades").document("man").set(
                hashMapOf(
                    "nombre" to "Manchester",
                    "poblacion" to 553230,
                    "cod_pais" to "UK",
                )
            )
            db.collection("ciudades").document("liv").set(
                hashMapOf(
                    "nombre" to "Liverpool",
                    "poblacion" to 496784,
                    "cod_pais" to "UK",
                )
            )
            // MONUMENTOS
            db.collection("monumentos").document("pr").set(
                hashMapOf(
                    "nombre" to "Palacio Real",
                    "cod_poblacion" to "mad",
                )
            )
            db.collection("monumentos").document("cac").set(
                hashMapOf(
                    "nombre" to "Ciudad de las Artes y las Ciencias",
                    "cod_poblacion" to "val",
                )
            )
            db.collection("monumentos").document("pg").set(
                hashMapOf(
                    "nombre" to "Park Güell",
                    "cod_poblacion" to "bar",
                )
            )
            db.collection("monumentos").document("tei").set(
                hashMapOf(
                    "nombre" to "Torre Eiffel",
                    "cod_poblacion" to "par",
                )
            )
            db.collection("monumentos").document("jarjap").set(
                hashMapOf(
                    "nombre" to "Jardin Japonais Pierre Baudis",
                    "cod_poblacion" to "tou",
                )
            )
            db.collection("monumentos").document("galr").set(
                hashMapOf(
                    "nombre" to "Gallo-Roman Museum of Lyon-Fourvière",
                    "cod_poblacion" to "ly",
                )
            )
            db.collection("monumentos").document("col").set(
                hashMapOf(
                    "nombre" to "Coliseo Romano",
                    "cod_poblacion" to "rom",
                )
            )
            db.collection("monumentos").document("duo").set(
                hashMapOf(
                    "nombre" to "Duomo de Milán",
                    "cod_poblacion" to "mil",
                )
            )
            db.collection("monumentos").document("ves").set(
                hashMapOf(
                    "nombre" to "Monte Vesubio",
                    "cod_poblacion" to "nap",
                )
            )
            db.collection("monumentos").document("mar").set(
                hashMapOf(
                    "nombre" to "Marienplatz",
                    "cod_poblacion" to "mun",
                )
            )
            db.collection("monumentos").document("muc").set(
                hashMapOf(
                    "nombre" to "Museo de la cerveza",
                    "cod_poblacion" to "mun",
                )
            )
            db.collection("monumentos").document("pbr").set(
                hashMapOf(
                    "nombre" to "Puerta de Brandeburgo",
                    "cod_poblacion" to "ber",
                )
            )
            db.collection("monumentos").document("lps").set(
                hashMapOf(
                    "nombre" to "Librería pública de Stuttgart",
                    "cod_poblacion" to "stu",
                )
            )
            db.collection("monumentos").document("lbr").set(
                hashMapOf(
                    "nombre" to "London Bridge",
                    "cod_poblacion" to "lon",
                )
            )
            Toast.makeText(
                this, "Información cargada con éxito", Toast.LENGTH_SHORT
            ).show()
        }
        getButton.setOnClickListener {

            val getDatos = Intent(this, ConsultaActivity::class.java)
            startActivity(getDatos)

           // db.collection("paises").document("ES").get().addOnSuccessListener {
                //Consulta
          //  }
        }
    }



    private fun setup(email: String, provider: String) {
        title = "Inicio"
        emailTextView.text = email
        providerTextView.text = provider

        logOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }
    }

    private fun createMapFragment(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Boton pulsado", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estas en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }


}