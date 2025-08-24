package com.example.recuerdago.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.recuerdago.network.NominatimClient

@Composable
fun LocationInfoCard(
    userLocation: Pair<Double, Double>?,
    selectedLocation: Pair<Double, Double>?,
    onLocationSearch: ((Double, Double) -> Unit)? = null,
    locationCustomName: String,
    onCustomNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
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
                            onLocationSearch?.invoke(lat, lon)
                            selectedAddress = result.display_name
                            isSearchMode = false
                            keyboardController?.hide()
                        }
                    }
                } catch (e: Exception) {
                    // Manejar error de búsqueda
                    selectedAddress = "No se encontró la ubicación"
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
                    } catch (e: Exception) {
                        selectedAddress = "Ubicación: $lat, $lon"
                        searchText = "Ubicación: $lat, $lon"
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ubicación del usuario (GPS - punto azul)
            LocationRow(
                icon = Icons.Default.MyLocation,
                iconColor = Color.Blue,
                title = "Tu ubicación actual",
                address = userAddress ?: "Obteniendo ubicación...",
                isLoading = userAddress == null
            )

            Divider(color = Color.Gray.copy(alpha = 0.3f))

            // Ubicación seleccionada (punto rojo) - Con búsqueda y nombre personalizado
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
                    if (isSearchMode) {
                        searchText = selectedAddress ?: ""
                    }
                },
                onSearch = searchLocation,
                onCustomNameChange = onCustomNameChange,
                onEditNameToggle = { isEditingName = !isEditingName }
            )
        }
    }
}

@Composable
private fun LocationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    address: String,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = iconColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Obteniendo dirección...",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            } else {
                Text(
                    text = address,
                    fontSize = 12.sp,
                    color = Color.Black.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SearchableLocationRow(
    isSearchMode: Boolean,
    searchText: String,
    selectedAddress: String?,
    isLoading: Boolean,
    isSearching: Boolean,
    locationCustomName: String,
    isEditingName: Boolean,
    onSearchTextChange: (String) -> Unit,
    onSearchModeToggle: () -> Unit,
    onSearch: () -> Unit,
    onCustomNameChange: (String) -> Unit,
    onEditNameToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ubicación seleccionada",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.8f)
                )

                // Botón para alternar modo búsqueda
                IconButton(
                    onClick = onSearchModeToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isSearchMode) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (isSearchMode) "Cancelar búsqueda" else "Buscar ubicación",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Campo para nombre personalizado de la ubicación (OBLIGATORIO)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Nombre de la ubicación:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                    // Asterisco rojo indicando campo obligatorio
                    Text(
                        text = " *",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }

                IconButton(
                    onClick = onEditNameToggle,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = if (isEditingName) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = if (isEditingName) "Guardar nombre" else "Editar nombre",
                        tint = Color.Red,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (isEditingName) {
                OutlinedTextField(
                    value = locationCustomName,
                    onValueChange = onCustomNameChange,
                    placeholder = {
                        Text(
                            text = "Ej: Casa, Trabajo, Universidad...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onEditNameToggle() }
                    ),
                    isError = locationCustomName.isBlank(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (locationCustomName.isBlank()) Color.Red else Color.Blue,
                        unfocusedBorderColor = if (locationCustomName.isBlank()) Color.Red.copy(alpha = 0.5f) else Color.Gray
                    )
                )
                if (locationCustomName.isBlank()) {
                    Text(
                        text = "Este campo es obligatorio",
                        fontSize = 10.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            } else {
                Text(
                    text = if (locationCustomName.isNotBlank()) locationCustomName else "Campo obligatorio - Toca para agregar nombre",
                    fontSize = 12.sp,
                    color = if (locationCustomName.isNotBlank()) Color.Black.copy(alpha = 0.8f) else Color.Red,
                    fontWeight = if (locationCustomName.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.clickable { onEditNameToggle() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isSearchMode) {
                // Campo de texto para buscar
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    placeholder = {
                        Text(
                            text = "Ej: Universidad Central, Quito",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    },
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.Red
                            )
                        } else {
                            IconButton(
                                onClick = onSearch,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearch() }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 12.sp,
                        color = Color.Black
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedBorderColor = Color.Red,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            } else {
                // Mostrar dirección (modo normal)
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Obteniendo dirección...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    Text(
                        text = selectedAddress ?: "Mueve el mapa para seleccionar",
                        fontSize = 12.sp,
                        color = Color.Black.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { onSearchModeToggle() }
                    )
                }
            }
        }
    }
}