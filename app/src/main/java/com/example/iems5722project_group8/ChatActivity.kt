package com.example.iems5722project_group8

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.unit.dp
import com.example.iems5722project_group8.ui.theme.IEMS5722Project_Group8Theme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Refresh

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.MaterialTheme

import androidx.compose.runtime.Composable

import androidx.compose.runtime.mutableStateListOf

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatActivity : ComponentActivity() {

    data class Message(
        val message: String,
        val name: String,
        val message_time: String,
        val user_id: Int
    )

    val messages = mutableStateListOf<Message>()
    private var chatroomId: Int = -1
    private lateinit var chatroomName: String
    private var userName: String = "ZHANG Xiangbo"
    private var userId: Int = 1155226712

    @SuppressLint("RememberReturnType")
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        chatroomId = intent.getIntExtra("chatroomId", -1)
        chatroomName = intent.getStringExtra("chatroomName").toString()

        super.onCreate(savedInstanceState)
        setContent {
            IEMS5722Project_Group8Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            colors = topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text("$chatroomName")
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    Intent(applicationContext, MainActivity::class.java).also {
                                        startActivity(it)
                                    }
                                }
                                )
                                {
                                    Icon(Icons.Filled.ArrowBack, null)
                                }
                            },

                            actions = {
                                IconButton(onClick = {
                                    Intent(applicationContext, ChatActivity::class.java).also {
                                        it.putExtra("chatroomId", chatroomId)
                                        it.putExtra("chatroomName", chatroomName)
                                        startActivity(it)
                                    }

                                }
                                )
                                {
                                    Icon(Icons.Filled.Refresh, null)
                                }
                            }

                        )
                    },
                    bottomBar = {
                        BottomAppBar(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ) {
                            var text by remember { mutableStateOf("") }
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            )
                            {
                                TextField(
                                    value = text,

                                    onValueChange = {
                                        text = it
                                    },
                                    label = { Text("Please enter your message") },
                                    modifier = Modifier
                                        .width(300.dp)
                                )
                                Button(
                                    onClick = {
                                        val currentTime = LocalDateTime.now()
                                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

                                        val newMessageMap: Map<String, Any> = mapOf(
                                            "message" to text,
                                            "name" to userName,
                                            "message_time" to currentTime,
                                            "user_id" to userId,
                                        )

                                        if (text.isNotBlank()) {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                val response = sendMessages(newMessageMap)
                                                if (response == "OK") {
                                                    messages.add(0,
                                                        Message(
                                                            message = newMessageMap["message"] as String,
                                                            name = newMessageMap["name"] as String,
                                                            message_time = newMessageMap["message_time"] as String,
                                                            user_id = newMessageMap["user_id"] as Int,
                                                        ))
                                                    text = ""
                                                }
                                            }

                                        }
                                    },
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text("Send")

                                }

                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(innerPadding)
                                .align(Alignment.BottomStart),
                            verticalArrangement = Arrangement.Bottom,

                            horizontalAlignment = Alignment.Start

                        ) {

                            getMessagesCoro()

                            LazyColumn(
                                reverseLayout = true,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                items(messages) { message ->
                                    MessageCard((message),userName)
                                }
                            }


                        }
                    }


                }
            }
        }
    }


    private fun getMessagesCoro() {
        CoroutineScope(Dispatchers.Main).launch {
            getMessages()
        }
    }

    private suspend fun getMessages() {
        withContext(Dispatchers.IO) {
            val url = URL("https://iems5722-chatroomserverapp-1155226712.azurewebsites.net/get_messages?chatroom_id=$chatroomId")
            Log.d("ChatActivity", "Server URL: $url")
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                val response =
                    BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                Log.d("ChatActivity", "Server Response: $response")
                val json = JSONObject(response)
                val data = json.getJSONObject("data").getJSONArray("messages")
                messages.clear()
                for (i in 0 until data.length()) {
                    val messageObject = data.getJSONObject(i)
                    val message = messageObject.getString("message")
                    val name = messageObject.getString("name")
                    val message_time = messageObject.getString("message_time")
                    val user_id = messageObject.getInt("user_id")
                    messages.add(Message(message, name, message_time, user_id))
                }
            } finally {
                connection.disconnect()
            }
        }
    }


    private suspend fun sendMessages(newMessage: Map<String, Any>): String {
        return withContext(Dispatchers.IO) {
            try {
                val newMessageWithChatroomId: Map<String, Any> = newMessage.toMutableMap().apply {
                    put("chatroom_id", chatroomId)
                }
                val url = URL("https://iems5722-chatroomserverapp-1155226712.azurewebsites.net/send_message/")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonOutput = JSONObject(newMessageWithChatroomId).toString()
                connection.outputStream.use { os ->
                    val input = jsonOutput.toByteArray()
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(responseBody)
                    return@withContext jsonResponse.getString("status")
                } else {
                    return@withContext "ERROR"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext "ERROR"
            }
        }
    }


}

@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun MessageCard(msg: ChatActivity.Message, name: String) {
    val alignmentMethod =
        if (msg.name == name) Alignment.CenterEnd else Alignment.CenterStart
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = alignmentMethod

    ) {
        Row(
            modifier = Modifier
                .padding(all = 8.dp)
        ) {

            Column(
                horizontalAlignment = if (msg.name == name) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = msg.name,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(shape = MaterialTheme.shapes.medium, shadowElevation = 1.dp) {
                    Text(
                        text = msg.message,
                        modifier = Modifier.padding(all = 4.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val dateTime = LocalDateTime.parse(msg.message_time, formatter)

                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val newMessageTime = dateTime.format(timeFormatter)
                Text(
                    text = newMessageTime,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall
                )

            }
        }

    }
}
