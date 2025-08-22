package com.example.recuerdago.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.recuerdago.viewmodel.AuthViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val userState = authViewModel.user  // Copia local
    val isLoggedIn = authViewModel.isLoggedIn

    LaunchedEffect(userState, isLoggedIn) {
        if (!isLoggedIn || (userState != null && !userState.activo)) {
            authViewModel.logout()
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    val isLoading = authViewModel.isLoading
    val errorMessage = authViewModel.errorMessage

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Rutas alternas") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Recordatorios") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Grupos colaborativos") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ubicación",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.fillMaxSize()
                )
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Reloj",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(36.dp)
                        .offset(x = (-10).dp, y = 10.dp)
                )
            }

            Text(
                text = "RecuerdaGo",
                fontSize = 28.sp,
                color = Color.Black
            )

            when {
                isLoading -> {
                    CircularProgressIndicator()
                    Text("Cargando usuario...")
                }
                userState != null -> {  // Usar la copia local
                    Text(text = "Bienvenido, ${userState.nombre} ${userState.apellido}", fontSize = 20.sp)
                    Text(text = "ID: ${userState.id}")
                    Text(text = "Activo: ${userState.activo}")
                    Text(text = "Rol: ${userState.rol}")

                    Button(
                        onClick = { authViewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Cerrar sesión")
                    }
                }
                errorMessage != null -> {
                    Text(text = "Error: $errorMessage", fontSize = 16.sp, color = Color.Red)
                    Button(onClick = { authViewModel.retryGetCurrentUser() }) {
                        Text("Reintentar")
                    }
                }
                else -> {
                    Text(text = "No se pudo cargar el usuario", fontSize = 18.sp)
                    Button(onClick = { authViewModel.getCurrentUser() }) {
                        Text("Cargar usuario")
                    }
                }
            }
        }
    }
}
