package com.example.easychat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(contactId: String, contactName: String, onBack: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(contactId) {
        db.collection("chats")
            .whereEqualTo("senderId", currentUser)
            .whereEqualTo("receiverId", contactId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)
                    }.sortedBy { it.timestamp }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contactName, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF007AFF))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message, isOwnMessage = message.senderId == currentUser)
                }
            }

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("Escribe un mensaje...") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )

            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        val newMessage = Message(
                            senderId = currentUser,
                            receiverId = contactId,
                            message = messageText,
                            timestamp = Timestamp.now()
                        )
                        coroutineScope.launch {
                            db.collection("chats").add(newMessage)
                        }
                        messageText = ""
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp)
            ) {
                Text("Enviar")
            }
        }
    }
}

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

@Composable
fun MessageBubble(message: Message, isOwnMessage: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier.padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = if (isOwnMessage) Color(0xFF007AFF) else Color.LightGray)
        ) {
            Text(
                text = message.message,
                color = if (isOwnMessage) Color.White else Color.Black,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
