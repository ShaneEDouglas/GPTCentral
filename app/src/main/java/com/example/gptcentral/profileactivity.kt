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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf<String?>(null) }


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
                username = snapshot.child("username").getValue(String::class.java)
                 email = snapshot.child("email").getValue(String::class.java)


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


    // Function to navigate to the change password screen
    fun navigateToChangePasswordScreen() {
        val intent = Intent(context, changepasswordscreen::class.java)
        context.startActivity(intent)
    }

//ui
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
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
            })

        // Profile picture
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
                painter = if (uploadedImageUrl != null) rememberAsyncImagePainter(uploadedImageUrl) else painterResource(id = R.drawable.dummypic),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(1.dp))

        Card(
            modifier = Modifier
                .padding(20.dp)
                .width(300.dp)
                .height(170.dp),
            elevation = CardDefaults.cardElevation(10.dp)
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
            onClick = { /*TODO*/ },
        ) {
            Text(text = "Log out")
        }
    }



}









@Preview(showBackground = true)
@Composable
fun DefaultPreview4() {
    GPTCentralTheme {
ProfileScreen()
    }
}