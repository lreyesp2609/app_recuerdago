package com.example.recuerdago.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

@Composable
fun GetCurrentLocation(
    onLocationResult: (latitude: Double, longitude: Double) -> Unit,
    onError: (String) -> Unit,
    onPermissionGranted: () -> Unit = {} // Añade este callback
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var shouldRetryLocation by remember { mutableStateOf(false) } // Nuevo estado para reintentar

    // --- Lanzador para permisos ---
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasPermission = fineLocationGranted || coarseLocationGranted

        if (hasPermission) {
            onPermissionGranted() // Notificar que los permisos fueron concedidos
        } else {
            onError("Permiso de ubicación denegado")
        }
    }

    // --- Lanzador para activar GPS ---
    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // GPS activado: Reiniciar el proceso de ubicación
            shouldRetryLocation = true
        } else {
            onError("GPS no activado")
        }
    }

    // Verificar permisos al inicio
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            hasPermission = true
        }
    }

    // Función para iniciar actualizaciones de ubicación
    fun startLocationUpdates(
        client: FusedLocationProviderClient,
        request: LocationRequest
    ) {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    onLocationResult(location.latitude, location.longitude)
                } else {
                    onError("No se pudo obtener la ubicación")
                }
                client.removeLocationUpdates(this)
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    // Efecto para manejar la obtención de ubicación (se ejecuta cuando hay permisos o se debe reintentar)
    LaunchedEffect(hasPermission, shouldRetryLocation) {
        if (!hasPermission) return@LaunchedEffect

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        ).setMinUpdateIntervalMillis(500).build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            startLocationUpdates(fusedLocationClient, locationRequest)
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                gpsLauncher.launch(intentSenderRequest)
            } else {
                onError("GPS no disponible")
            }
        }
        // Resetear el flag de reintento después de procesarlo
        shouldRetryLocation = false
    }
}