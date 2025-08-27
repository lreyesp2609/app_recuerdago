import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import com.example.recuerdago.data.models.DirectionsRequest
import com.example.recuerdago.data.models.DirectionsResponse
import com.example.recuerdago.network.RetrofitInstance

import androidx.compose.runtime.State

class MapViewModel : ViewModel() {
    private val _route = mutableStateOf<DirectionsResponse?>(null)
    val route: State<DirectionsResponse?> = _route

    private var currentMode = "walking"

    fun setMode(mode: String) {
        currentMode = mode
    }

    fun fetchRoute(start: Pair<Double, Double>, end: Pair<Double, Double>) {
        viewModelScope.launch {
            try {
                val profile = when (currentMode) {
                    "walking" -> "foot-walking"
                    "cycling" -> "cycling-regular"
                    "driving" -> "driving-car"
                    else -> "foot-walking"
                }

                val request = DirectionsRequest(
                    coordinates = listOf(
                        listOf(start.second, start.first), // lon, lat
                        listOf(end.second, end.first)
                    )
                )

                val response = RetrofitInstance.api.getRoute(profile, request)
                _route.value = response

            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching route", e)
                _route.value = null
            }
        }
    }

    fun clearRoute() {
        _route.value = null
    }
}
