package com.example.iems5722project_group8

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.unit.dp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class EditActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            editScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    // screen of this function
    fun editScreen() {
        var editNickname by remember { mutableStateOf("") }
        var editPassword by remember { mutableStateOf("") }
        editNickname = nickname
        editPassword = password

        // call get profiles function
        getProfilesCoro()

        MaterialTheme {
            Scaffold(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
                topBar = {
                    TopAppBar(colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ), title = {
                        Text("Personal Profile")
                    }, navigationIcon = {
                        IconButton(onClick = {
                            /*val intent = Intent(applicationContext, MainActivity::class.java)
                            applicationContext.startActivity(intent)*/
                            Intent(applicationContext, MainActivity::class.java).also {
                                startActivity(it)
                            }

                        }) {
                            Icon(Icons.Filled.ArrowBack, "back to main")
                        }
                    })
                },
                bottomBar = {
                    BottomAppBar(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    val newProfileMap: Map<String, Any> = mapOf(
                                        "account" to account,
                                        "nickname" to editNickname,
                                        "password" to editPassword
                                    )
                                    CoroutineScope(Dispatchers.IO).launch {
                                        editProfiles(newProfileMap)
                                    }
                                }, modifier = Modifier.padding(8.dp)
                            ) {
                                Text("Save")

                            }
                        }
                    }
                }) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        alignment = Alignment.Center,
                        painter = painterResource(id = R.drawable.icon),
                        contentDescription = "portrait",
                        modifier = Modifier.clip(CircleShape)
                    )
                    OutlinedTextField(value = account,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "icon"
                            )
                        },
                        onValueChange = { account = it },
                        label = { Text(text = "Account") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    OutlinedTextField(value = editNickname,

                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email, contentDescription = "icon"
                            )
                        },
                        onValueChange = { editNickname = it },
                        label = { Text("Nickname") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    OutlinedTextField(value = editPassword,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Create, contentDescription = "password"
                            )
                        },
                        onValueChange = { editPassword = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            nickname = editNickname
            password = editPassword
        }

    }

    suspend fun getProfiles() {
        withContext(Dispatchers.IO) {
            val url = URL("http://10.0.2.2:8086/get_profiles?account=$account")
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                val response =
                    BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val json = JSONObject(response)
                val data = json.getJSONObject("data").getJSONArray("profiles")
                val profileObject = data.getJSONObject(0)
                account = profileObject.getString("account")
                nickname = profileObject.getString("nickname")
                password = profileObject.getString("password")
            } finally {
                connection.disconnect()
            }
        }
    }

    fun getProfilesCoro() {
        CoroutineScope(Dispatchers.Main).launch {
            getProfiles()
        }
    }

    suspend fun editProfiles(newProfileMap: Map<String, Any>): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("http://10.0.2.2:8086/edit_profiles")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonOutput = JSONObject(newProfileMap).toString()

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