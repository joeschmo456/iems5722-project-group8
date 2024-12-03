package com.example.iems5722project_group8

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

var account: String = "test"
var nickname: String = ""
var password: String = ""


suspend fun getProfiles() {
    withContext(Dispatchers.IO) {
        val url = URL("http://10.0.2.2:8086/get_profiles?account=${account}")
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

@Composable
fun PersonalProfile() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.BottomStart
            ) {
                getProfilesCoro()
                PersonalProfileHeader()
            }
            Spacer(modifier = Modifier.height(10.dp))
            PersonalProfileDetail()
            Box(
                modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.BottomCenter
            ) {
                BottomSettingIcons()
            }
        }
    }

}

@Composable
fun PersonalProfileHeader() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(10.dp)
    ) {
        val (portraitImageRef, usernameTextRef, desTextRef) = remember { createRefs() }
        Image(painter = painterResource(id = R.drawable.icon),
            contentDescription = "portrait",
            modifier = Modifier
                .constrainAs(portraitImageRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
                .clip(CircleShape))
        Text(text = nickname,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Left,
            color = Color.White,
            modifier = Modifier.constrainAs(usernameTextRef) {
                top.linkTo(portraitImageRef.top, 5.dp)
                start.linkTo(portraitImageRef.end, 10.dp)
                width = Dimension.preferredWrapContent
            })
    }
}

@Composable
fun PersonalProfileDetail() {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        Row {
            Text(
                text = "Personal Profile",
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.padding(vertical = 12.dp))

        NavigationDrawerItem(label = {
            Text("Account", style = MaterialTheme.typography.titleMedium)
        }, selected = true, badge = {
            account.let {
                Text(it)
            }
        }, icon = {
            Icon(Icons.Rounded.Face, null)
        }, onClick = {})
        Spacer(Modifier.height(8.dp))
        NavigationDrawerItem(label = {
            Text("Nickname", style = MaterialTheme.typography.titleMedium)
        }, selected = true, badge = {
            nickname.let {
                Text(it)
            }
        }, icon = {
            Icon(Icons.Rounded.DateRange, null)
        }, onClick = {})

    }
}

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun BottomSettingIcons() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val context = LocalContext.current
        Column(
            modifier = Modifier.clickable(
                onClick = {
                    val intent = Intent(context, EditActivity::class.java)
                    context.startActivity(intent)

                }, indication = null, interactionSource = MutableInteractionSource()
            ), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.Settings, contentDescription = null)
            Text(text = "Edit", fontSize = 15.sp)
        }

        Column(
            modifier = Modifier.clickable(
                // click to log out, update status
                onClick = {
                    val newStatus: Map<String, Any> = mapOf(
                        "account" to account, "loginStatus" to 0
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        logout(newStatus)
                    }
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)

                }, indication = null, interactionSource = MutableInteractionSource()
            ), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.Lock, contentDescription = null)
            Text(text = "Log out", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

suspend fun logout(newStatus: Map<String, Any>): String {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("http://10.0.2.2:8086/logout")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonOutput = JSONObject(newStatus).toString()

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