package com.example.iems5722project_group8

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

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
            Text("登录", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(20.dp))

            // 用户账号输入框
            BasicTextField(
                value = account,
                onValueChange = { account = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, Color.Gray),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 密码输入框
            BasicTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, Color.Gray),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(20.dp))

            // 登录按钮
            Button(
                onClick = { login(account, password, onLoginResult = { resultMessage -> message = resultMessage }) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("登录")
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
                        onLoginResult("登录成功: $responseMessage")
                    } else {
                        onLoginResult("登录失败: $responseMessage")
                    }
                }

                urlConnection.disconnect()

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    onLoginResult("网络请求失败")
                }
            }
        }.start()
    }
}
