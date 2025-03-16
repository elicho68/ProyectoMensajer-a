package com.example.easychat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onProfile: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var contacts by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var groups by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("chats") }
    var showAddContactDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == "chats") {
            loadContacts { contacts = it }
        } else {
            loadGroups { groups = it }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EasyChat", color = Color.White) },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menú")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Perfil") },
                            onClick = {
                                expanded = false
                                onProfile()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Agregar contacto") },
                            onClick = {
                                expanded = false
                                showAddContactDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                expanded = false
                                FirebaseAuth.getInstance().signOut()
                                onLogout()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFF007AFF)
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(selectedTab, onTabSelected = { selectedTab = it })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar contacto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedTab == "chats") {
                Text("Tus contactos", fontSize = 20.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(contacts) { contact ->
                        ContactItem(contact["firstName"] ?: "", contact["lastName"] ?: "")
                    }
                }
            } else {
                Text("Tus grupos", fontSize = 20.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                if (groups.isEmpty()) {
                    Text("Aún no tienes grupos. Agrega más contactos para crear uno.", color = Color.Gray)
                } else {
                    LazyColumn {
                        items(groups) { group ->
                            ContactItem(group["groupName"] ?: "", "")
                        }
                    }
                }
            }
        }
    }
}

// Función para cargar contactos desde Firestore
fun loadContacts(onResult: (List<Map<String, String>>) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    db.collection("users").document(currentUser).collection("contacts").get()
        .addOnSuccessListener { documents ->
            val contacts = documents.mapNotNull { doc ->
                mapOf(
                    "firstName" to (doc.getString("firstName") ?: ""),
                    "lastName" to (doc.getString("lastName") ?: "")
                )
            }
            onResult(contacts)
        }
        .addOnFailureListener {
            onResult(emptyList())
        }
}


// Función para cargar grupos desde Firestore
fun loadGroups(onResult: (List<Map<String, String>>) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser?.email ?: return

    FirebaseFirestore.getInstance().collection("groups")
        .whereArrayContains("members", currentUser)
        .get()
        .addOnSuccessListener { documents ->
            val groups = documents.map { doc ->
                mapOf("groupName" to (doc.getString("name") ?: "Grupo sin nombre"))
            }
            onResult(groups)
        }
        .addOnFailureListener {
            onResult(emptyList())
        }
}

@Composable
fun BottomNavigationBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar(containerColor = Color(0xFF007AFF)) {
        NavigationBarItem(
            selected = selectedTab == "chats",
            onClick = { onTabSelected("chats") },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Chats") },
            label = { Text("Chats") }
        )
        NavigationBarItem(
            selected = selectedTab == "grupos",
            onClick = { onTabSelected("grupos") },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Grupos") },
            label = { Text("Grupos") }
        )
    }
}

@Composable
fun ContactItem(firstName: String, lastName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$firstName $lastName", fontSize = 18.sp, color = Color.Black)
        }
    }
}
