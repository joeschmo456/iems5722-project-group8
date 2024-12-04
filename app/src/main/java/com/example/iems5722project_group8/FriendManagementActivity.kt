package com.example.iems5722project_group8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iems5722project_group8.ui.theme.IEMS5722Project_Group8Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class FriendManagementActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FriendManagementScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FriendManagementScreen() {
        IEMS5722Project_Group8Theme {
            var nickname by remember { mutableStateOf("") }
            var friends by remember { mutableStateOf(listOf<Friend>()) }
            var message by remember { mutableStateOf("") } // 用于存储显示的消息

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text("Friend Management") },
                        colors = TopAppBarDefaults.mediumTopAppBarColors(),
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Input field for nickname
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Friend Nickname") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Buttons for Add, Delete, and Show Friends
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val result = addFriend(nickname)
                                withContext(Dispatchers.Main) {
                                    message = result  // 更新提示信息
                                }
                            }
                        }) {
                            Text("Add")
                        }

                        Button(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val result = deleteFriend(nickname)
                                withContext(Dispatchers.Main) {
                                    message = result  // 更新提示信息
                                }
                            }
                        }) {
                            Text("Delete")
                        }

                        Button(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val fetchedFriends = fetchFriends(nickname)
                                withContext(Dispatchers.Main) {
                                    friends = fetchedFriends
                                    message = if (friends.isNotEmpty()) {
                                        "Successfully displayed friends"  // 更新提示信息
                                    } else {
                                        "No friends found"
                                    }
                                }
                            }
                        }) {
                            Text("Show")
                        }
                    }

                    // 显示操作结果消息
                    if (message.isNotEmpty()) {
                        Text(text = message, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    // Display friends in a LazyColumn
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(friends) { friend ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Nickname: ${friend.nickname}", fontSize = 18.sp)
                                    Text("Account: ${friend.account}", fontSize = 16.sp)
                                    Text(
                                        "Online: ${if (friend.hasLogin) "Yes" else "No"}",
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    data class Friend(
        val account: String,
        val nickname: String,
        val hasLogin: Boolean
    )

    private suspend fun addFriend(nickname: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("http://10.0.2.2:8000/add_friends")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonInput = JSONObject().apply {
                    put("nickname", nickname)
                }

                connection.outputStream.use { os ->
                    val input = jsonInput.toString().toByteArray()
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return@withContext "Friend added successfully"
                } else {
                    return@withContext "Failed to add friend"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext "Error: ${e.message}"
            }
        }
    }

    private suspend fun deleteFriend(nickname: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("http://10.0.2.2:8000/delete_friends")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonInput = JSONObject().apply {
                    put("nickname", nickname)
                }

                connection.outputStream.use { os ->
                    val input = jsonInput.toString().toByteArray()
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return@withContext "Friend deleted successfully"
                } else {
                    return@withContext "Failed to delete friend"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext "Error: ${e.message}"
            }
        }
    }

//    private suspend fun fetchFriends(nickname: String): List<Friend> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val url = URL("http://10.0.2.2:8000/show_friends")
//                val connection = url.openConnection() as HttpURLConnection
//                connection.requestMethod = "POST"
//                connection.setRequestProperty("Content-Type", "application/json")
//                connection.doOutput = true
//
//                val jsonInput = JSONObject().apply {
//                    put("nickname", nickname)
//                }
//
//                connection.outputStream.use { os ->
//                    val input = jsonInput.toString().toByteArray()
//                    os.write(input, 0, input.size)
//                }
//
//                val responseCode = connection.responseCode
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    val response = BufferedReader(connection.inputStream.reader()).use { it.readText() }
//                    val jsonArray = JSONArray(JSONObject(response).getString("data"))
//
//                    return@withContext (0 until jsonArray.length()).map { i ->
//                        val jsonObject = jsonArray.getJSONObject(i)
//                        Friend(
//                            account = jsonObject.getString("account"),
//                            nickname = jsonObject.getString("nickname"),
//                            hasLogin = jsonObject.getBoolean("has_login")
//                        )
//                    }
//                } else {
//                    return@withContext emptyList()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                return@withContext emptyList()
//            }
//        }
//    }
    private suspend fun fetchFriends(nickname: String): List<Friend> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("http://10.0.2.2:8000/show_friends")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonInput = JSONObject().apply {
                    put("nickname", nickname)
                }

                connection.outputStream.use { os ->
                    val input = jsonInput.toString().toByteArray()
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(connection.inputStream.reader()).use { it.readText() }
                    val jsonObject = JSONObject(response)

                    // 直接从返回的 JSON 对象中获取数据
                    if (jsonObject.has("account")) {
                        // 返回单个 friend 对象
                        return@withContext listOf(
                            Friend(
                                account = jsonObject.getString("account"),
                                nickname = jsonObject.getString("nickname"),
                                hasLogin = jsonObject.getInt("has_login") == 1
                            )
                        )
                    }
                }
                return@withContext emptyList()  // 返回空列表，表示没有找到好友
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext emptyList()  // 返回空列表，表示发生错误
            }
        }
    }

}