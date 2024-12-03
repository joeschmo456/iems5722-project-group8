package com.example.iems5722project_group8

import android.Manifest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

import com.example.iems5722project_group8.ui.theme.IEMS5722Project_Group8Theme
import com.google.firebase.messaging.Constants.TAG
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : ComponentActivity() {
    private val chatrooms = mutableStateListOf<Chatroom>()
    private val userId = 1155226712

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        createNotificationChannel()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d(TAG, "FCM registration token: $token")

            val newTokenMap: Map<String, Any> = mapOf(
                "user_id" to userId,
                "token" to token
            )

            // call the push function of token
            CoroutineScope(Dispatchers.IO).launch {
                pushTokens(newTokenMap)
            }
        }

        FirebaseMessaging.getInstance().subscribeToTopic("all").addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.e("TAG", "onCreate: subscribeToTopic")
            } else {
                Log.e("TAG", "onCreate: subscribeToTopic failed")
            }
        }

        enableEdgeToEdge()
        setContent {
            mainPage(chatrooms = chatrooms, applicationContext = this)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    // mainpage function
    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun mainPage(chatrooms: List<Chatroom>, applicationContext: Context) {
        IEMS5722Project_Group8Theme {
            // add drawer
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet { /* Drawer content */
                    PersonalProfile()
                    }
                },
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(), topBar = {
                        TopAppBar(colors = topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ), title = {
                            Text("IEMS5722")
                        }, navigationIcon = {
                            IconButton(onClick = {}) {
                                Icon(Icons.Filled.Menu, null)
                            }
                        })
                    }) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        getChatroomCoro()
                        LazyColumn {
                            items(chatrooms.reversed()) { chatroom ->
                                OutlinedButton(
                                    onClick = {
                                        Intent(applicationContext, ChatActivity::class.java).also {
                                            it.putExtra("chatroomId", chatroom.id)
                                            it.putExtra("chatroomName", chatroom.name)
                                            startActivity(it)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(2.dp),
                                    shape = RectangleShape

                                ) {
                                    Text(
                                        text = chatroom.name, fontSize = 20.sp
                                    )
                                }
                            }
                        }


                    }
                }
            }

        }
    }

    data class Chatroom(
        val id: Int, val name: String
    )

    private suspend fun getChatroom() {
        withContext(Dispatchers.IO) {
            val url = URL("https://iems5722-chatroomserverapp-1155226712.azurewebsites.net/get_chatrooms")
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                val response =
                    BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                // parse json
                val json = JSONObject(response)
                val data = json.getJSONArray("data")
                chatrooms.clear()
                for (i in 0 until data.length()) {
                    val room = data.getJSONObject(i)
                    chatrooms.add(Chatroom(room.getInt("id"), room.getString("name")))
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun getChatroomCoro() {
        CoroutineScope(Dispatchers.Main).launch {
            getChatroom()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
                )
            }
            val channel = NotificationChannel(
                "MyNotification", "MyNotification", NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java) as NotificationManager
            manager.createNotificationChannel(channel)


        }

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val CHANNEL_ID = "my_channel_01"
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // push tokens and user_id to server
    private suspend fun pushTokens(newToken: Map<String, Any>): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://iems5722-chatroomserverapp-1155226712.azurewebsites.net/submit_push_token/")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                Log.d(
                    "MainActivityLog",
                    "Sending token: userId = ${newToken["user_id"]}, token = ${newToken["token"]}"
                )
                val jsonOutput = JSONObject(newToken).toString()
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


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.title, it.body)
        }
    }

    private fun sendNotification(title: String?, body: String?) {
        val CHANNEL_ID = "my_channel_01"
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.icon)
            .setContentTitle(title).setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setContentIntent(pendingIntent)
            .setAutoCancel(true)
        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        val notificationId = 0
        notificationManager.notify(notificationId, builder.build())

    }

}
