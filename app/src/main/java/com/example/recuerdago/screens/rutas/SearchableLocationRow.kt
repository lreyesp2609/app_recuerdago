package com.example.recuerdago.screens.rutas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchableLocationRow(
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
                    onValueChange = { newValue ->
                        if (newValue.length <= 100) { // Limitar a 100 caracteres
                            onCustomNameChange(newValue)
                        }
                    },
                    placeholder = {
                        Text(
                            text = "Ej: Casa, Trabajo, Universidad...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    },
                    trailingIcon = {
                        // Botón X para limpiar el campo
                        if (locationCustomName.isNotEmpty()) {
                            IconButton(
                                onClick = { onCustomNameChange("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar nombre",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    supportingText = {
                        Text(
                            text = "${locationCustomName.length}/100 caracteres",
                            fontSize = 10.sp,
                            color = if (locationCustomName.length >= 90) Color.Red else Color.Gray,
                            modifier = Modifier.fillMaxWidth()
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
                        unfocusedBorderColor = if (locationCustomName.isBlank()) Color.Red.copy(alpha = 0.5f) else Color.Gray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
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