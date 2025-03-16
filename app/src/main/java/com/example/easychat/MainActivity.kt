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
import com.example.easychat.ui.screens.ChatScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Asegura que Firebase esté inicializado

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { navController.navigate("home") }, // Redirige a Home si el login es exitoso
                        onForgotPassword = { /* Aquí se manejará recuperación de contraseña */ },
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
                            FirebaseAuth.getInstance().signOut() // Cierra sesión en Firebase
                            navController.popBackStack("login", inclusive = true)
                        },
                        onProfile = { /* Aquí se manejará la navegación al perfil */ },
                        onChatSelected = { contactId, contactName ->
                            navController.navigate("chat/$contactId/$contactName")
                        }
                    )
                }

                composable("chat/{contactId}/{contactName}") { backStackEntry ->
                    val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
                    val contactName = backStackEntry.arguments?.getString("contactName") ?: ""
                    ChatScreen(contactId, contactName) {
                        navController.popBackStack() // Regresa al home cuando se salga del chat
                    }
                }
            }
        }
    }
}
