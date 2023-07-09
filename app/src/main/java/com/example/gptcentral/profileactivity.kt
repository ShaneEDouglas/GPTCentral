package com.example.gptcentral

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.DrawerValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.bumptech.glide.Glide
import com.example.gptcentral.ui.theme.GPTCentralTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import java.util.*


class profileactivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPTCentralTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen()
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun ProfileScreen() {
    val context = LocalContext.current
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf<String?>(null) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()


    // Handle image upload to Firebase
    fun handleImageUploadToFirebase(uri: Uri) {
        val storage = Firebase.storage
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("images/$filename")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    uploadedImageUrl = it.toString()
                    // Update the profile image URL in Firebase Database or perform any necessary operations

                    //Save the url into the firebase database (user profile)
                    FirebaseDatabase.getInstance().getReference("users/"+ FirebaseAuth.getInstance().uid + "/profilePicture").setValue(uploadedImageUrl)
                }
            }.addOnFailureListener {
               Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }


    //Retrive the profile image, username, and email
    FirebaseDatabase.getInstance().getReference("users/"+ FirebaseAuth.getInstance().uid )
        .addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {


                uploadedImageUrl = snapshot.child("profilePicture").getValue(String::class.java)
                username = snapshot.child("username").getValue(String::class.java)?: ""
                 email = snapshot.child("email").getValue(String::class.java)?: ""


            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Failed to get the Url", "Url Error: $error")
            }

        })



    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadedImageUrl = it.toString()
            handleImageUploadToFirebase(it)
        }
    }

    // Open image picker
    fun openImagePicker() {
        imageLauncher.launch("image/*")
    }



    fun navigateToChangePasswordScreen() {
        val intent = Intent(context, changepasswordscreen::class.java)
        context.startActivity(intent)
    }

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
                    Text(
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
                    }),
                icon = {
                    Image(painterResource(id = R.drawable.profile), contentDescription = "Profile") }
            ) {
                androidx.compose.material.Text("Profile")
            }
            ListItem(
                modifier = Modifier.clickable(
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            val intent = Intent(context, imagegenactivity::class.java)
                            context.startActivity(intent)
                        }
                    }),
                icon = {
                    Image(painterResource(id = R.drawable.baseline_image_24), contentDescription = "Profile") }
            ) {
                androidx.compose.material.Text("Image Generator")
            }
            ListItem(
                modifier = Modifier.clickable(
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            val intent = Intent(context, gptactivty::class.java)
                            context.startActivity(intent)
                        }
                    }),
                icon = {
                    Image(painterResource(id = R.drawable.chat), contentDescription = "Profile") }
            ) {
                androidx.compose.material.Text("Chat Bot")
            }

            ListItem(
                modifier = Modifier.clickable(
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            handlelogout(context)
                        }
                    }),
                icon = {
                    Image(painterResource(id = R.drawable.logout), contentDescription = "Profile") }
            ) {
                androidx.compose.material.Text("Logout")
            }
        }
                        },

        content = {

//ui
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = { androidx.compose.material.Text("GPTCentral Chat") },
            actions = {

                androidx.compose.material.IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    androidx.compose.material.Icon(
                        Icons.Filled.Menu,
                        contentDescription = "More Options"
                    )
                }
            }
        )

        // Profile picture
        Card(
            modifier = Modifier
                .padding(20.dp)
                .size(200.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Image(
                modifier = Modifier

                    .size(200.dp)
                    .fillMaxSize()
                    .clickable { openImagePicker() },
                painter = if (uploadedImageUrl != null) rememberAsyncImagePainter(uploadedImageUrl) else painterResource(id = R.drawable.regpropic),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
        val backgroundcolor = colorResource(id = androidx.appcompat.R.color.material_blue_grey_800)
        Card(

            modifier = Modifier
                .padding(20.dp)
                .width(300.dp)
                .height(170.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            )

        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Username: $username",
                fontSize = 20.sp,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                modifier = Modifier.padding(16.dp),
                text = "Email: $email",
                fontSize = 20.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            onClick = { navigateToChangePasswordScreen() },
        ) {
            Text(text = "Change password")
        }

        Spacer(modifier = Modifier.height(5.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            onClick = { handlelogout(context) },
        ) {
            Text(text = "Log out")
        }
    }
})
}









@Preview(showBackground = true)
@Composable
fun DefaultPreview4() {
    GPTCentralTheme {
ProfileScreen()
    }
}