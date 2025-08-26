package com.example.recuerdago.screens.rutas

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.recuerdago.network.NominatimClient

@Composable
fun LocationInfoCard(
    userLocation: Pair<Double, Double>?,
    selectedLocation: Pair<Double, Double>?,
    locationCustomName: String,
    onCustomNameChange: (String) -> Unit,
    onLocationSearch: (Double, Double) -> Unit,
    onAddressChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    cardBackgroundColor: Color = if (isSystemInDarkTheme()) Color(0xFF2D2D44) else Color.White,
    textColor: Color = if (isSystemInDarkTheme()) Color.White else Color.Black,
    primaryColor: Color = if (isSystemInDarkTheme()) Color(0xFF64B5F6) else Color.Blue,
    errorColor: Color = Color.Red
) {
    var userAddress by remember { mutableStateOf<String?>(null) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var isLoadingSelected by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    // Estado para controlar edición de nombre
    var isEditingName by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Función para buscar ubicación por texto
    val searchLocation = {
        if (searchText.isNotBlank()) {
            isSearching = true
            scope.launch {
                try {
                    val response = NominatimClient.apiService.searchLocation(
                        query = searchText,
                        format = "json",
                        limit = 1,
                        addressdetails = 1
                    )

                    if (response.isNotEmpty()) {
                        val result = response[0]
                        val lat = result.lat?.toDoubleOrNull()
                        val lon = result.lon?.toDoubleOrNull()

                        if (lat != null && lon != null) {
                            // Mover el mapa a la ubicación encontrada
                            onLocationSearch.invoke(lat, lon)
                            selectedAddress = result.display_name
                            // ← AGREGAR ESTA LÍNEA PARA SINCRONIZAR
                            onAddressChange(result.display_name ?: "")
                            isSearchMode = false
                            keyboardController?.hide()
                        }
                    }
                } catch (e: Exception) {
                    // Manejar error de búsqueda
                    selectedAddress = "No se encontró la ubicación"
                    onAddressChange("No se encontró la ubicación") // ← AGREGAR
                } finally {
                    isSearching = false
                }
            }
        }
    }

    // Efecto para obtener dirección del usuario (solo una vez)
    LaunchedEffect(userLocation) {
        userLocation?.let { (lat, lon) ->
            scope.launch {
                try {
                    val response = NominatimClient.apiService.reverseGeocode(
                        lat = lat,
                        lon = lon
                    )
                    userAddress = response.display_name
                } catch (e: Exception) {
                    userAddress = "Ubicación GPS: $lat, $lon"
                }
            }
        }
    }

    // Efecto para obtener dirección seleccionada (se actualiza cuando cambia)
    LaunchedEffect(selectedLocation) {
        if (!isSearchMode) { // Solo actualizar si no estamos en modo búsqueda
            selectedLocation?.let { (lat, lon) ->
                isLoadingSelected = true
                scope.launch {
                    try {
                        val response = NominatimClient.apiService.reverseGeocode(
                            lat = lat,
                            lon = lon
                        )
                        selectedAddress = response.display_name
                        searchText = response.display_name ?: ""
                        // ← AGREGAR ESTA LÍNEA CRÍTICA
                        onAddressChange(response.display_name ?: "Ubicación: $lat, $lon")
                    } catch (e: Exception) {
                        val fallbackAddress = "Ubicación: $lat, $lon"
                        selectedAddress = fallbackAddress
                        searchText = fallbackAddress
                        // ← AGREGAR ESTA LÍNEA TAMBIÉN
                        onAddressChange(fallbackAddress)
                    } finally {
                        isLoadingSelected = false
                    }
                }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LocationRow(
                icon = Icons.Default.MyLocation,
                iconColor = primaryColor,
                title = "Tu ubicación actual",
                address = userAddress ?: "Obteniendo ubicación...",
                isLoading = userAddress == null,
                textColor = textColor
            )

            Divider(color = textColor.copy(alpha = 0.3f))

            SearchableLocationRow(
                isSearchMode = isSearchMode,
                searchText = searchText,
                selectedAddress = selectedAddress,
                isLoading = isLoadingSelected,
                isSearching = isSearching,
                locationCustomName = locationCustomName,
                isEditingName = isEditingName,
                onSearchTextChange = { searchText = it },
                onSearchModeToggle = {
                    isSearchMode = !isSearchMode
                    if (isSearchMode) searchText = selectedAddress ?: ""
                },
                onSearch = searchLocation,
                onCustomNameChange = onCustomNameChange,
                onEditNameToggle = { isEditingName = !isEditingName },
                textColor = textColor,
                errorColor = errorColor
            )
        }
    }
}