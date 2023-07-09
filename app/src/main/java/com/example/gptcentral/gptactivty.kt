package com.example.gptcentral

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*



import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import com.example.gptcentral.ui.theme.GPTCentralTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

lateinit var gptResponse: MutableState<String>

class gptactivty : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPTCentralTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                        Mainscreen()
                }
            }
        }
    }
}




@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Mainscreen() {
    val messagelist = remember { mutableStateListOf<Message>() }
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
                Column(

                ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colorResource(id = R.color.purple_500))

                        ) {
                            androidx.compose.material3.Text(
                                text = "Main Menu",
                                modifier = Modifier.padding(16.dp),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    ListItem(
                        modifier = Modifier.clickable(
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    val intent = Intent(context, profileactivity::class.java)
                                    context.startActivity(intent)
                                }
                            }
                        ),
                        icon = {
                            Image(painterResource(id = R.drawable.profile), contentDescription = "Profile") }
                    ) {
                        Text("Profile")
                    }
                    ListItem(
                        modifier = Modifier.clickable(
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    val intent = Intent(context, imagegenactivity::class.java)
                                    context.startActivity(intent)
                                }
                            }
                        ),
                        icon = {
                            Image(painterResource(id = R.drawable.baseline_image_24), contentDescription = "Profile") }
                    ) {
                        Text("Image Generator")
                    }
                    ListItem(
                        modifier = Modifier.clickable(
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    val intent = Intent(context, gptactivty::class.java)
                                    context.startActivity(intent)
                                }
                            }
                        ),
                        icon = {
                            Image(painterResource(id = R.drawable.chat), contentDescription = "Profile") }
                    ) {
                        Text("Chat Bot")
                    }
                    ListItem(
                        modifier = Modifier.clickable(
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    handlelogout(context)
                                }
                            }
                        ),
                        icon = {
                            Image(painterResource(id = R.drawable.logout), contentDescription = "Profile") }

                    ) {
                        Text("Logout")
                    }
                }
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                TopAppBar(
                    title = { Text("GPTCentral Chat") },
                    actions = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },

                        ) {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = "More Options",
                                tint = Color.White
                            )
                        }
                    },

                    elevation = 6.dp,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    chatsection(
                        messagelist = messagelist,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
                Divider(thickness = 2.dp,)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    inputsection(
                        messagelist = messagelist,
                        context
                    )
                }
            }
        }
    )
}

@Composable
fun chatsection(messagelist: MutableList<Message>, modifier: Modifier = Modifier) {
    val liststate = rememberLazyListState()

    LazyColumn(
        state = liststate,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        items(messagelist) { message ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = if (message.isuser) Arrangement.End else Arrangement.Start
            ) {
                MessageCard(message = message)
            }
        }
    }

    LaunchedEffect(messagelist.size) {
        if (messagelist.isNotEmpty()) {
            liststate.animateScrollToItem(index = messagelist.size - 1)
        }
    }
}

@Composable
fun inputsection(messagelist: MutableList<Message>,context: Context) {
    var text by remember { mutableStateOf("") }
    val gptResponse = remember { mutableStateOf("") }
    val isBotTyping = remember { mutableStateOf(false) }

    LaunchedEffect(gptResponse.value) {
        if (gptResponse.value.isNotBlank()) {
            messagelist.add(Message(gptResponse.value, false))
            gptResponse.value = ""
            isBotTyping.value = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        TextField(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
                .fillMaxWidth()
                ,
            shape = RoundedCornerShape(16.dp),
            value = text,
            onValueChange = { newText ->
                text = newText
            },
            placeholder = {
                Text(
                    text = "Type a message...",
                    style = TextStyle(color = colorResource(id = R.color.black))
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (text.isBlank()) {
                            Toast.makeText(context, "Type a message", Toast.LENGTH_SHORT).show()
                        } else {
                            gptapidata(text, gptResponse, messagelist)
                            messagelist.add(Message(text, true))
                            text = ""
                            isBotTyping.value = true
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                    )
                }
            },
        )
        if (isBotTyping.value) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp),
                color = MaterialTheme.colors.primary
            )
        }
    }
}

@Composable
fun MessageCard(message: Message) {
    val backgroundColor = if (message.isuser) colorResource(id = R.color.chatgreen) else colorResource(id = androidx.appcompat.R.color.material_blue_grey_800)
    Card(
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = backgroundColor,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(

                text = message.message ?: "",
                modifier = Modifier.padding(8.dp),
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 16.sp,
                    color = if (message.isuser) Color.White else Color.White
                )
            )
        }
    }
}




fun handlelogout(context: Context){

    FirebaseAuth.getInstance().signOut()
    val intent = Intent(context,MainActivity::class.java)
    startActivity(context, intent,null)

}


val client = OkHttpClient()

fun gptapidata(prompt: String, gptResponse: MutableState<String>, messagelist: MutableList<Message>) {

    //set up the OkHttp api call
    val url = "https://api.openai.com/v1/completions"
    val apikey = "YOUR_API_KEY_HERE"

    //get the json type
    val jsonMediatype = "application/json; charset=utf-8".toMediaType()

    //message parameter in json object for the openai api
    val message = JSONObject().apply {
        put("role","user")
        put("content",prompt)
    }

    val json = JSONObject().apply {
        put("model","text-davinci-003")
        put("prompt",prompt)
        put("max_tokens",4000)
        put("temperature",0)
    }

    //make the requestbody


    val requestbody = json.toString().toRequestBody(jsonMediatype)

    //requestobject

    val request = Request.Builder()
        .url(url)
        .addHeader("Content-Type","application/json")
        .addHeader("Authorization", "Bearer $apikey")
        .post(requestbody)
        .build()


    client.newCall(request).enqueue(object:Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.d("Call failed","Failed to get a response: ${e.message}")
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            try {
                Log.d("Call was successful", "this is your response: $responseBody")
                val jsonarray = JSONObject(responseBody).getJSONArray("choices")


                val gptResponses = mutableListOf<String>()

                for(i in 0 until jsonarray.length()){
                    val botresponse = jsonarray.getJSONObject(i).getString("text")
                    gptResponses.add(botresponse)
                }


                gptResponse.value = gptResponses[0]

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    })
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    GPTCentralTheme {
        Mainscreen()
    }
}