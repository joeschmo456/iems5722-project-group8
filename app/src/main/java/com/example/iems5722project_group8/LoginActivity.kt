package com.example.iems5722project_group8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.content.Intent
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }

    @Composable
    fun LoginScreen() {
        var account by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("LOGIN",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 用户账号输入框
            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, Color.Gray),
                singleLine = true,
                label = { Text("Account") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 密码输入框
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, Color.Gray),
                singleLine = true,
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // 登录按钮
            Button(
                onClick = { login(account, password, onLoginResult = { resultMessage ->
                    message = resultMessage
                    if (resultMessage.startsWith("success")) {
                        navigateToMainActivity()
                    }
                })
              },
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("LOGIN")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(message)
        }
    }

    private fun login(account: String, password: String, onLoginResult: (String) -> Unit) {
        // 网络请求在后台线程中执行
        Thread {
            try {
                val url = URL("http://10.0.2.2:8086/login")  // FastAPI 后端地址
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }

                // 构建 JSON 请求体
                val jsonBody = """
                    {
                        "account": "$account",
                        "password": "$password"
                    }
                """.trimIndent()

                // 发送请求体
                val outputStream: OutputStream = urlConnection.outputStream
                outputStream.write(jsonBody.toByteArray())

                // 获取响应
                val responseCode = urlConnection.responseCode
                val responseMessage = urlConnection.inputStream.bufferedReader().readText()

                // 处理响应
                runOnUiThread {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        onLoginResult("success: $responseMessage")

                    } else {
                        onLoginResult("failed: $responseMessage")
                    }
                }

                urlConnection.disconnect()

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    onLoginResult("network failed")
                }
            }
        }.start()
    }
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 结束当前 Activity，防止返回登录界面
    }
}
