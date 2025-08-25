package com.example.recuerdago

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recuerdago.ui.theme.RecuerdagoTheme
import com.example.recuerdago.viewmodel.AuthViewModel
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recuerdago.screens.HomeScreen
import com.example.recuerdago.screens.rutas.RutasScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authViewModel: AuthViewModel = ViewModelProvider(
            this,
            AuthViewModel.AuthViewModelFactory(this)
        )[AuthViewModel::class.java]

        // Configurar barras del sistema para toda la app
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Configurar colores de las barras (siempre negro)
        window.navigationBarColor = Color.Black.toArgb()
        window.statusBarColor = Color.Black.toArgb()

        // Configurar iconos blancos
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContent {
            RecuerdagoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(navController = navController, authViewModel = authViewModel)
                        }
                        composable("home") {
                            HomeScreen(authViewModel = authViewModel, navController = navController)
                        }
                        composable("rutas") {
                            val token = authViewModel.accessToken ?: ""
                            RutasScreen(token = token)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Observa el usuario
    LaunchedEffect(authViewModel.user) {
        if (authViewModel.user != null) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Observa errores
    LaunchedEffect(authViewModel.errorMessage) {
        authViewModel.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // Logo
        Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Ubicación",
                tint = Color(0xFF64B5F6), // azul claro
                modifier = Modifier.fillMaxSize()
            )
            Icon(
                imageVector = Icons.Default.AccessAlarm,
                contentDescription = "Alarma",
                tint = Color.Red,
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = (-10).dp, y = 10.dp)
            )
        }

        Text(
            text = "RecuerdaGo",
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !authViewModel.isLoading
        )

        // Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !authViewModel.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    authViewModel.login(email, password) { }
                } else {
                    Toast.makeText(
                        context,
                        "Por favor completa todos los campos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2),
                contentColor = Color.White
            ),
            enabled = !authViewModel.isLoading
        ) {
            if (authViewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciando...", fontSize = 18.sp, color = Color.White)
            } else {
                Text("Iniciar sesión", fontSize = 18.sp, color = Color.White)
            }
        }

        Text(
            text = "¿No tienes cuenta? Regístrate",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(100.dp))
        Spacer(modifier = Modifier.weight(0.5f))
    }
}
