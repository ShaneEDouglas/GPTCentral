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
import androidx.compose.foundation.layout.*



import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                // A surface container using the 'background' color from the theme
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


@Composable
fun Mainscreen() {
    val messagelist = remember { mutableStateListOf<Message>() }
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("GPTCentral Chat") },
            actions = {
                IconButton(onClick = { handlelogout(context) }) {
                    Text("Logout")
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Filled.Menu, contentDescription = "More Options")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        expanded = false
                        val intent = Intent(context, profileactivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Text("Profile")
                    }
                    DropdownMenuItem(onClick = {
                        expanded = false
                        val intent = Intent(context, imagegenactivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Text("Image Generator")
                    }
                    androidx.compose.material.DropdownMenuItem(onClick = {
                        expanded = false
                        val intent = Intent(context, gptactivty::class.java)
                        context.startActivity(intent)
                    }) {
                        androidx.compose.material.Text("Chat Bot")
                    }
                }
            }
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
                    .padding(16.dp) // adjust as needed
            )
        }
        Divider(thickness = 2.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                 // optional, just to distinguish the area
        ) {
            inputsection(
                messagelist = messagelist
            )
        }
    }
}


//Design what each message will look like

@Composable
fun MessageCard(message:Message) { val backgroundcolor = if(message.isuser) colorResource(id = R.color.chatgreen) else Color.Blue
        Card(
            modifier = Modifier
                .padding(20.dp),
            shape = RoundedCornerShape(15.dp),
            backgroundColor = backgroundcolor,
            elevation = 10.dp
        ) {
            Column() {

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 20.sp,
                    text = message.message?:"",
                    modifier = Modifier.padding(8.dp),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
}

@Composable
fun chatsection(messagelist: MutableList<Message>,modifier: Modifier = Modifier) {
    val liststate = rememberLazyListState()

    LazyColumn(
        state = liststate,
        verticalArrangement = Arrangement.Center
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

    // Scroll to bottom every time messagelist changes
    LaunchedEffect(messagelist.size) {
        if (messagelist.isNotEmpty()) {
            liststate.animateScrollToItem(index = messagelist.size - 1)
        }
    }
}
@Composable
fun inputsection(messagelist: MutableList<Message>,modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    val gptResponse = remember { mutableStateOf("") }


    LaunchedEffect(gptResponse.value) {  // Observe changes in gptResponse
        if(gptResponse.value.isNotBlank()) {
            messagelist.add(Message(gptResponse.value, false)) // Add bot's response to the message list
            gptResponse.value = ""  // Reset gptResponse
        }
    }

    Row( modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            modifier = Modifier,
            shape = RoundedCornerShape(4.dp),
            value = text,
            onValueChange = { newText ->
                text = newText
            },
            label = {Text("Enter a Message...")}
        )

        IconButton(
            onClick = {
                //call the api
                gptapidata(text, gptResponse,messagelist)
                messagelist.add(Message(text, true))
                text = ""
            }
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send Message" )
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
    val apikey = "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"

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
    //can't use on create

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

                // Similar to pet data, you could create a list to hold multiple responses
                val gptResponses = mutableListOf<String>()

                for(i in 0 until jsonarray.length()){
                    val botresponse = jsonarray.getJSONObject(i).getString("text")
                    gptResponses.add(botresponse)
                }

                // Use the first response for the message list, or handle as needed
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