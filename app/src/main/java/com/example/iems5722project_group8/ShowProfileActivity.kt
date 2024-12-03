//package com.example.iems5722project_group8
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.util.Log
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.rounded.*
//import androidx.compose.material.icons.rounded.Lock
//import androidx.compose.material.icons.rounded.Settings
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.NavigationDrawerItem
//import androidx.compose.material3.Text
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.paint
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.constraintlayout.compose.ConstraintLayout
//import androidx.constraintlayout.compose.Dimension
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.json.JSONObject
//import java.io.BufferedReader
//import java.io.InputStreamReader
//import java.net.HttpURLConnection
//import java.net.URL
//
//var account: String = "test"
//var nickname: String = ""
//var password: String = ""
//
//class ShowProfileActivity : ComponentActivity()  {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            PersonalProfile()
//
//        }
//    }
//
//    suspend fun getProfiles() {
//        withContext(Dispatchers.IO) {
//            val url =
//                URL("http://10.0.2.2:8086/get_profiles?account=${account}")
//            Log.d("ChatActivity", "Server URL: $url")
//            val connection = url.openConnection() as HttpURLConnection
//            try {
//                connection.requestMethod = "GET"
//                val response =
//                    BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
//                val json = JSONObject(response)
//                val data = json.getJSONObject("data").getJSONArray("profiles")
//                val profileObject = data.getJSONObject(0)
//                account = profileObject.getString("account")
//                nickname = profileObject.getString("nickname")
//                password = profileObject.getString("password")
//                Log.d("ChatActivity", "nickname in getProfiles: $nickname")
//
//            } finally {
//                connection.disconnect()
//            }
//        }
//    }
//
//    fun getProfilesCoro() {
//        CoroutineScope(Dispatchers.Main).launch {
//            getProfiles()
//            Log.d("ChatActivity", "nickname in getProfilesCoro: $nickname")
//        }
//    }
//
//    @Composable
//    fun PersonalProfile() {
//            getProfilesCoro()
//
//        Log.d("PersonalProfile", "nickname in PersonalProfile: ${nickname}")
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(MaterialTheme.colorScheme.background),
//        ) {
//            Box(
//                Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//                    .paint(
//                        painterResource(id = R.drawable.google_bg),
//                        contentScale = ContentScale.FillBounds
//                    ),
//                contentAlignment = Alignment.BottomStart
//            ) {
//                PersonalProfileHeader()
//            }
//            Spacer(modifier = Modifier.height(10.dp))
//            PersonalProfileDetail()
//            Box(
//                modifier = Modifier.fillMaxHeight(),
//                contentAlignment = Alignment.BottomCenter
//            ) {
//                BottomSettingIcons()
//            }
//        }
//
//    }
//
//    @Composable
//    fun PersonalProfileHeader() {
//        ConstraintLayout(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(100.dp)
//                .padding(10.dp)
//        ) {
//            val (portraitImageRef, usernameTextRef, desTextRef) = remember { createRefs() }
//            Image(
//                painter = painterResource(id = R.drawable.icon),
//                contentDescription = "portrait",
//                modifier = Modifier
//                    .constrainAs(portraitImageRef) {
//                        top.linkTo(parent.top)
//                        bottom.linkTo(parent.bottom)
//                        start.linkTo(parent.start)
//                    }
//                    .clip(CircleShape)
//            )
//            Text(
//                text = nickname,
//                fontSize = 20.sp,
//                fontWeight = FontWeight.ExtraBold,
//                textAlign = TextAlign.Left,
//                color = Color.White,
//                modifier = Modifier
//                    .constrainAs(usernameTextRef) {
//                        top.linkTo(portraitImageRef.top, 5.dp)
//                        start.linkTo(portraitImageRef.end, 10.dp)
//                        width = Dimension.preferredWrapContent
//                    }
//            )
//        }
//    }
//
//
//    @Composable
//    fun PersonalProfileDetail() {
//        getProfilesCoro()
//        Log.d("PersonalProfile", "Nickname Body : ${nickname}")
//        Column(
//            modifier = Modifier
//                .padding(horizontal = 12.dp)
//        ) {
//            Row {
//                Text(
//                    text = "Personal Profile",
//                    fontSize = 25.sp,
//                    fontWeight = FontWeight.ExtraBold,
//                    modifier = Modifier.weight(1f)
//                )
//            }
//            Spacer(Modifier.padding(vertical = 12.dp))
//            PersonalProfileItem.entries.forEach { item ->
//                NavigationDrawerItem(
//                    label = {
//                        Text(item.label, style = MaterialTheme.typography.titleMedium)
//                    },
//                    selected = true,
//                    badge = {
//                        item.badge?.let {
//                            Text(it)
//                        }
//                    },
//                    icon = {
//                        Icon(item.icon, null)
//                    },
//                    onClick = {
//                    }
//                )
//                Spacer(Modifier.height(8.dp))
//            }
//        }
//    }
//
//    enum class PersonalProfileItem(
//        val label: String,
//        val badge: String?,
//        val icon: ImageVector
//    ) {
//        ACCOUNT("Account", account, Icons.Rounded.Face),
//        NICKNAME("Nickname", nickname, Icons.Rounded.DateRange)
//    }
//
//    @SuppressLint("UnrememberedMutableInteractionSource")
//    @Composable
//    fun BottomSettingIcons() {
//        Row(
//            Modifier
//                .fillMaxWidth()
//                .padding(18.dp),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            val context = LocalContext.current
//            Column(
//                modifier = Modifier.clickable(
//                    onClick = {
//                        val intent = Intent(context, EditActivity::class.java)
//                        context.startActivity(intent)
//
//                    },
//                    indication = null,
//                    interactionSource = MutableInteractionSource()
//                ),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Icon(Icons.Rounded.Settings, contentDescription = null)
//                Text(text = "Edit", fontSize = 15.sp)
//            }
//
//            Column(
//                modifier = Modifier.clickable(
//                    onClick = {
//                        val intent = Intent(context, LoginActivity::class.java)
//                        context.startActivity(intent)
//
//                    },
//                    indication = null,
//                    interactionSource = MutableInteractionSource()
//                ),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Icon(Icons.Rounded.Lock, contentDescription = null)
//                Text(text = "Log out", fontSize = 15.sp, fontWeight = FontWeight.Bold)
//            }
//        }
//    }
//}