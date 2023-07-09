package com.example.gptcentral

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gptcentral.ui.theme.GPTCentralTheme
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class changepasswordscreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPTCentralTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChangePasswordScreen()
                }
            }
        }
    }
}

@Composable
private fun ChangePasswordScreen() {
    // State variables for current password, new password, and confirm password
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Perform necessary validation and handle password change
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    fun changePassword() {
        // Perform necessary validation, e.g., check if the new password and confirm password match
        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(context, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword){
            Toast.makeText(context, "New password and confirm password do not match", Toast.LENGTH_SHORT).show()
            return
        }
        // Reauthenticate the user with their current password
        val credential = EmailAuthProvider.getCredential(currentUser?.email ?: "", currentPassword)
        currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Current password is correct, proceed with changing the password
                    currentUser.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                // Password changed successfully
                                Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(context,profileactivity::class.java)
                                context.startActivity(intent)
                            } else {
                                // Password change failed, handle the error
                                val errorMessage = updateTask.exception?.message
                                Toast.makeText(context, "Failed to change password: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // Current password is incorrect, show an error message
                    Toast.makeText(
                        context,
                        "Invalid current password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // UI elements for change password screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = { Text("Current Password") },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().shadow(elevation = 20.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
        TextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().shadow(elevation = 20.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().shadow(elevation = 20.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { changePassword() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Change Password")
        }
    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview5() {
    GPTCentralTheme {

    }
}