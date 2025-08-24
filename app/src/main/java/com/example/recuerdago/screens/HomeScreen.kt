package com.example.recuerdago.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.recuerdago.screens.rutas.RutasScreen
import com.example.recuerdago.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val userState = authViewModel.user
    val isLoggedIn = authViewModel.isLoggedIn
    val isDarkTheme = isSystemInDarkTheme()

    // Estados de animación
    var isVisible by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }


    // Animaciones
    val logoScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    val logoRotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else 360f,
        animationSpec = tween(1000), label = ""
    )

    // Colores según el tema
    val backgroundGradient = if (isDarkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A1A2E),
                Color(0xFF16213E)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF8F9FA),
                Color(0xFFE3F2FD)
            )
        )
    }

    val cardColors = if (isDarkTheme) {
        CardDefaults.cardColors(containerColor = Color(0xFF2D2D44))
    } else {
        CardDefaults.cardColors(containerColor = Color.White)
    }

    val primaryColor = if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF1976D2)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val accentColor = Color(0xFFFF6B6B)

    LaunchedEffect(userState, isLoggedIn) {
        if (!isLoggedIn || (userState != null && !userState.activo)) {
            authViewModel.logout()
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
        delay(800)
        showContent = true
    }

    val isLoading = authViewModel.isLoading
    val errorMessage = authViewModel.errorMessage

    val userId = try {
        val idField = userState!!::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.get(userState).toString()
    } catch (e: Exception) {
        "N/A"
    }

    @Composable
    fun HomeTabContent(
        navController: NavController,
        userState: Any?,
        isLoading: Boolean,
        errorMessage: String?,
        authViewModel: AuthViewModel,
        primaryColor: Color,
        textColor: Color,
        cardColors: CardColors,
        showContent: Boolean, // Añadido para las animaciones
        accentColor: Color,
        isDarkTheme: Boolean
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 80.dp), // Espacio para el header
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = showContent,
                enter = slideInVertically(
                    initialOffsetY = { -it }
                ) + fadeIn()
            ) {
                when {
                    isLoading -> {
                        LoadingContent(primaryColor)
                    }
                    userState != null -> {
                        UserWelcomeContent(userState, textColor, primaryColor, cardColors)
                    }
                    errorMessage != null -> {
                        ErrorContent(errorMessage, authViewModel, primaryColor, textColor)
                    }
                    else -> {
                        NoUserContent(authViewModel, primaryColor, textColor)
                    }
                }
            }

            AnimatedVisibility(
                visible = showContent && userState != null,
                enter = slideInVertically(
                    initialOffsetY = { it }
                ) + fadeIn()
            ) {
                val currentUserId = try {
                    userState!!::class.java.getDeclaredField("id").apply { isAccessible = true }.get(userState).toString()
                } catch (e: Exception) { "N/A" }

                ModulesSection(
                    navController = navController,
                    userId = currentUserId,
                    primaryColor = primaryColor,
                    accentColor = accentColor,
                    cardColors = cardColors,
                    isDarkTheme = isDarkTheme,
                    onTabSelected = { selectedTab = it } // Pasar la función para cambiar tab
                )
            }
        }
    }

    @Composable
    fun RutasTab(navController: NavController, userId: String) {
        // Contenedor responsivo para RutasScreen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 80.dp), // Mismo padding que HomeTabContent
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RutasScreen(
                userId = userId,
                isDarkTheme = isDarkTheme,
                primaryColor = primaryColor,
                textColor = textColor,
            )
        }
    }

    @Composable
    fun RecordatoriosTab() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Recordatorios")
        }
    }

    @Composable
    fun GruposTab() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Grupos Colaborativos")
        }
    }

    @Composable
    fun ConfiguracionTab() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Configuración")
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = if (isDarkTheme) Color(0xFF2D2D44) else Color.White,
                contentColor = primaryColor
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Inicio",
                            tint = if (selectedTab == 0) primaryColor else Color.Gray
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "Rutas alternas",
                            tint = if (selectedTab == 1) primaryColor else Color.Gray
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Recordatorios",
                            tint = if (selectedTab == 2) primaryColor else Color.Gray
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = "Grupos colaborativos",
                            tint = if (selectedTab == 3) primaryColor else Color.Gray
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = if (selectedTab == 4) primaryColor else Color.Gray
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues)
        ) {
            // Header con logo y nombre (siempre visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .scale(logoScale)
                        .rotate(logoRotation),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        tint = primaryColor,
                        modifier = Modifier.size(50.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.AccessAlarm,
                        contentDescription = "Alarma",
                        tint = accentColor,
                        modifier = Modifier
                            .size(20.dp)
                            .offset(x = 15.dp, y = (-15).dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it }
                    ) + fadeIn(
                        animationSpec = tween(800, delayMillis = 400)
                    )
                ) {
                    Text(
                        text = "RecuerdaGo",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            // Contenido de las pestañas
            when (selectedTab) {
                0 -> HomeTabContent(
                    navController = navController,
                    userState = userState,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    authViewModel = authViewModel,
                    primaryColor = primaryColor,
                    textColor = textColor,
                    cardColors = cardColors,
                    showContent = showContent,
                    accentColor = accentColor,
                    isDarkTheme = isDarkTheme
                )
                1 -> RutasTab(navController, userId)
                2 -> RecordatoriosTab()
                3 -> GruposTab()
                4 -> ConfiguracionTab()
            }
        }
    }
}

@Composable
fun LoadingContent(primaryColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(color = primaryColor, strokeWidth = 3.dp)
        Text("Cargando usuario...", color = primaryColor, fontSize = 16.sp)
    }
}

@Composable
fun UserWelcomeContent(
    userState: Any,
    textColor: Color,
    primaryColor: Color,
    cardColors: CardColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "¡Bienvenido!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            // Usando reflection segura para acceder a las propiedades
            val userName = try {
                val nombreField = userState::class.java.getDeclaredField("nombre")
                val apellidoField = userState::class.java.getDeclaredField("apellido")
                nombreField.isAccessible = true
                apellidoField.isAccessible = true
                val nombre = nombreField.get(userState) as? String ?: ""
                val apellido = apellidoField.get(userState) as? String ?: ""
                "$nombre $apellido"
            } catch (e: Exception) {
                "Usuario"
            }

            Text(
                text = userName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = primaryColor.copy(alpha = 0.3f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val userId = try {
                    val idField = userState::class.java.getDeclaredField("id")
                    idField.isAccessible = true
                    idField.get(userState).toString()
                } catch (e: Exception) {
                    "N/A"
                }

                InfoChip(
                    icon = Icons.Default.Badge,
                    text = "ID: $userId",
                    primaryColor = primaryColor
                )

                val isActive = try {
                    val activoField = userState::class.java.getDeclaredField("activo")
                    activoField.isAccessible = true
                    activoField.get(userState) as? Boolean ?: false
                } catch (e: Exception) {
                    false
                }

                InfoChip(
                    icon = Icons.Default.CheckCircle,
                    text = if (isActive) "Activo" else "Inactivo",
                    primaryColor = if (isActive) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }

            val userRol = try {
                val rolField = userState::class.java.getDeclaredField("rol")
                rolField.isAccessible = true
                rolField.get(userState) as? String ?: "N/A"
            } catch (e: Exception) {
                "N/A"
            }

            InfoChip(
                icon = Icons.Default.Person,
                text = "Rol: $userRol",
                primaryColor = primaryColor
            )
        }
    }
}

@Composable
fun InfoChip(icon: ImageVector, text: String, primaryColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = primaryColor
        )
    }
}

@Composable
fun ErrorContent(
    errorMessage: String,
    authViewModel: AuthViewModel,
    primaryColor: Color,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Error: $errorMessage",
            fontSize = 16.sp,
            color = Color(0xFFFF6B6B),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = { authViewModel.retryGetCurrentUser() },
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Reintentar", color = Color.White)
        }
    }
}

@Composable
fun NoUserContent(authViewModel: AuthViewModel, primaryColor: Color, textColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "No se pudo cargar el usuario",
            fontSize = 18.sp,
            color = textColor
        )
        Button(
            onClick = { authViewModel.getCurrentUser() },
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Cargar usuario", color = Color.White)
        }
    }
}

@Composable
fun ModulesSection(
    navController: NavController,
    userId: String,
    primaryColor: Color,
    accentColor: Color,
    cardColors: CardColors,
    isDarkTheme: Boolean,
    onTabSelected: (Int) -> Unit // Nuevo parámetro para cambiar tabs
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Módulos Principales",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ModuleCard(
            icon = Icons.Default.DirectionsCar,
            title = "Rutas Alternas",
            description = "Encuentra las mejores rutas con IA y análisis de patrones de movilidad",
            iconColor = Color(0xFF4CAF50),
            cardColors = cardColors,
            isDarkTheme = isDarkTheme,
            onClick = { onTabSelected(1) } // Usar la función pasada como parámetro
        )

        ModuleCard(
            icon = Icons.Default.NotificationImportant,
            title = "Recordatorios",
            description = "Alertas inteligentes basadas en tu ubicación geográfica",
            iconColor = accentColor,
            cardColors = cardColors,
            isDarkTheme = isDarkTheme
        )

        ModuleCard(
            icon = Icons.Default.Groups,
            title = "Grupos Colaborativos",
            description = "Monitoreo compartido de recorridos con tu equipo",
            iconColor = Color(0xFF9C27B0),
            cardColors = cardColors,
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
fun ModuleCard(
    icon: ImageVector,
    title: String,
    description: String,
    iconColor: Color,
    cardColors: CardColors,
    isDarkTheme: Boolean,
    onClick: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onClick?.invoke()
            }
            .clip(RoundedCornerShape(16.dp)),
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(
                        iconColor.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = if (isDarkTheme) Color.Gray else Color.DarkGray,
                    lineHeight = 16.sp,
                    maxLines = 2
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Ir",
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}