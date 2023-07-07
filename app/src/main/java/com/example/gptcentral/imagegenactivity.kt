package com.example.gptcentral

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.gptcentral.ui.theme.GPTCentralTheme
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

var downloadedImageFile: File? = null


class imagegenactivity : ComponentActivity() {
    var imageUrl: MutableState<String?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPTCentralTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Box(modifier = Modifier
                        .fillMaxSize()
                    ) {
                        Mainimgscreen(imageurl = imageUrl)


                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun Mainimgscreen(imageurl:MutableState<String?>) {

    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }




    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        TopAppBar(
            title = { androidx.compose.material.Text("GPTCentral Chat") },
            actions = {
                androidx.compose.material.IconButton(onClick = { handlelogout(context) }) {
                    androidx.compose.material.Text("Logout")
                }
                androidx.compose.material.IconButton(onClick = { expanded = !expanded }) {
                    androidx.compose.material.Icon(
                        Icons.Filled.Menu,
                        contentDescription = "More Options"
                    )
                }
                androidx.compose.material.DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    androidx.compose.material.DropdownMenuItem(onClick = {
                        expanded = false
                        val intent = Intent(context, profileactivity::class.java)
                        context.startActivity(intent)
                    }) {
                        androidx.compose.material.Text("Profile")
                    }
                    androidx.compose.material.DropdownMenuItem(onClick = {
                        expanded = false
                        val intent = Intent(context, imagegenactivity::class.java)
                        context.startActivity(intent)
                    }) {
                        androidx.compose.material.Text("Image Generator")
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

        Text(
            textAlign = TextAlign.Center,
            text = "GPT Image Generator",
            fontSize = 28.sp,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { newtext -> text = newtext },
            label = { Text("Enter your Prompt") },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(

                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            onClick = {
                isLoading = true
                getgptimg(text, context) { url ->
                    imageurl.value = url
                    isLoading = false
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Generate Image",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(10.dp)
            ) {
                if (imageurl.value != null) {
                    GlideImage(
                        model = imageurl.value,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                } else {
                    // Fallback if there is no image yet
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
    ) {
        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            onClick = { imageurl.value?.let { downloadimage(it, context) } }
        ) {
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Download")
        }
    }
}




var generatedImage by mutableStateOf<Bitmap?>(null)
val imgClient = OkHttpClient()

fun getgptimg(Prompt: String, context: Context, callback: (String) -> Unit)  {
    val url = "https://api.openai.com/v1/images/generations"
    val apiKey = "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"

    val jsonRequest = JSONObject()
        .put("prompt",Prompt)
        .put("n", 1)
        .put("size","512x512")

    val jsonMediatype = "application/json; charset=utf-8".toMediaType()

    val requestbody = jsonRequest.toString().toRequestBody(jsonMediatype)

    val request = Request.Builder()
        .url(url)
        .addHeader("Content-Type","applications/json")
        .addHeader("Authorization", "Bearer $apiKey")
        .post(requestbody)
        .build()

    imgClient.newCall(request).enqueue(object: Callback{
        override fun onFailure(call: Call, e: IOException) {
            Log.d("Image Failed","Your image failed to load: $e")
        }

        override fun onResponse(call: Call, response: Response) {

            Log.d("Image Successful", "here is your response $response")
            val responsebody = response.body?.string()
            val jsonresponse = JSONObject(responsebody)

            if (jsonresponse.has("data")) {
                val jsonarray = jsonresponse.getJSONArray("data")
                val firstobject = jsonarray.getJSONObject(0)
                val imageurl = firstobject.getString("url")

                callback(imageurl)
            } else {
                Log.d("Image Generation Error", "No 'data' object in the response")
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Use a different prompt", Toast.LENGTH_SHORT).show()
                }

                val bytes = response.body?.byteStream()?.readBytes()

                if (bytes != null) {
                    val filename = "${System.currentTimeMillis()}.jpg"
                    val file = File(context.cacheDir,filename)

                    //get uri for file and save to firebase
                    val uri = Uri.fromFile(file)
                    saveimagetofirebase(context,uri)
                }
            }
        }
    })
}

//Downloading the image and saving it to the phone gallery
private fun saveimagetogallery (bitmap: Bitmap,context:Context){
    val filename = "${System.currentTimeMillis()}.jpg"
    val fos: OutputStream?
    val imageuri: Uri?

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        // Inserting the values to the MediaStore
        val resolver = context.contentResolver
        imageuri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        fos = imageuri?.let { resolver.openOutputStream(it) }
    } else {
        val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(imagesDir, filename)
        fos = FileOutputStream(imageFile)
        imageuri = Uri.fromFile(imageFile)
    }

    fos?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }

    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, "Image Saved Successfully", Toast.LENGTH_SHORT).show()

    }
}

private fun saveimagetofirebase(context: Context, uri:Uri) {

    val storage = Firebase.storage
    val filename = "GeneratedImages/${System.currentTimeMillis()}.jpg"
    val imgref = storage.reference.child(filename)



    val uploadtask = imgref.putFile(uri)
    uploadtask.addOnFailureListener {

        Toast.makeText(context, "Image Filed to upload", Toast.LENGTH_SHORT).show()
    }.addOnSuccessListener {

        Toast.makeText(context, "Upload successful", Toast.LENGTH_SHORT).show()
    }
}


private fun downloadimage(imageurl: String, context:Context) {
    val request = Request.Builder()
        .url(imageurl)
        .build()
    //make another network call to get the url and turn it into a file to download
    imgClient.newCall(request).enqueue(object: Callback {
        override fun onFailure(call: Call, e: IOException) {
           Log.d("Image download failed", "Failed to downlaod image ${e.printStackTrace()}")

            Toast.makeText(context,"Failed to download Image",Toast.LENGTH_SHORT).show()
        }

        override fun onResponse(call: Call, response: Response) {
            val input = response.body?.byteStream()
            val bitmap = BitmapFactory.decodeStream(input)

            if (bitmap != null){
                saveimagetogallery(bitmap,context)

            } else {
                Toast.makeText(context,"There is no image yet, Type in your prompt",Toast.LENGTH_SHORT).show()
            }

        }


    })
}




@Preview(showBackground = true)
@Composable
fun DefaultPreview3() {
    GPTCentralTheme {

    }
}