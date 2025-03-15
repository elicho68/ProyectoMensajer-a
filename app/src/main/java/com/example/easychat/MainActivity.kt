package com.example.easychat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.easychat.ui.screens.LoginScreen
import com.example.easychat.ui.screens.RegisterScreen
import com.example.easychat.ui.screens.HomeScreen
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Asegura que Firebase se inicializa
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { navController.navigate("home") }, // Redirige a Home si el login es exitoso
                        onForgotPassword = { /* Aquí irá la navegación a recuperar contraseña */ },
                        onRegister = { navController.navigate("register") } // Redirige al registro
                    )
                }
                composable("register") {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.popBackStack()
                            navController.navigate("login")
                        },
                        onLogin = { navController.popBackStack() }
                    )
                }
                composable("home") {
                    HomeScreen(
                        onLogout = {
                            navController.popBackStack("login", inclusive = true)
                        }
                    )
                }
            }
        }
    }
}
