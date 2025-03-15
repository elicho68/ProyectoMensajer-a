package com.example.easychat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.easychat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var contactEmail by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val firestore = FirebaseFirestore.getInstance()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EasyChat", color = Color.White) },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Cerrar sesión", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFF007AFF)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { addContact(contactEmail) { msg -> message = msg } },
                containerColor = Color(0xFF007AFF),
                contentColor = Color.White
            ) {
                Icon(painterResource(id = R.drawable.ic_add), contentDescription = "Agregar contacto")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Agregar Contacto", fontSize = 20.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = contactEmail,
                onValueChange = { contactEmail = it },
                label = { Text("Correo del contacto") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { addContact(contactEmail) { msg -> message = msg } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar")
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (message.isNotEmpty()) {
                Text(message, color = Color.Red, fontSize = 14.sp)
            }
        }
    }
}

fun addContact(email: String, onResult: (String) -> Unit) {
    if (email.isBlank()) {
        onResult("El correo no puede estar vacío")
        return
    }
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("users").whereEqualTo("email", email).get()
        .addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                val user = documents.documents[0]
                val name = user.getString("firstName") ?: ""
                val lastName = user.getString("lastName") ?: ""
                onResult("Usuario agregado: $name $lastName")
            } else {
                onResult("El usuario no existe")
            }
        }
        .addOnFailureListener {
            onResult("Error al buscar el usuario")
        }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(onLogout = {})
}
