package com.example.gptcentral

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.gptcentral.ui.theme.GPTCentralTheme
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private var issignedup: Boolean = true



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPTCentralTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    loginfields()
                }
            }
        }
    }
}



@Composable
fun loginfields(){

    val context = LocalContext.current
// helps keep the user logged  in after signing on
    if (FirebaseAuth.getInstance().currentUser != null) {
        val intent = Intent(context, gptactivty::class.java)
        startActivity(context,intent,null)
    }



    var isvisible by remember { mutableStateOf(false) }
    //Get username,email and password from the input fields

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Image(
            painter = painterResource(id = R.drawable.central),
            contentDescription = "App logo",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(300.dp)
                .width(300.dp)
                .padding(12.dp)

            )


        if (isvisible) {
            TextField (
                value = username,
                onValueChange = { username = it },
                label = {Text("Username")},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                ,

                )
        }


        TextField (
            value = email,
            onValueChange = { email = it },
            label = {Text("Email")},
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )

        TextField(
            value = password ,
            onValueChange = { password = it },
            label = {Text("Password")},
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)

        )
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp, top = 4.dp,),
            horizontalArrangement = Arrangement.End
        ){
            Button(
                modifier = Modifier
                    .width(150.dp)
                    .height(50.dp)
                ,
                onClick = {
                    if (issignedup){
                        handlesignup(username,email, password, context)
                    } else {
                        handlelogin(email, password, context)
                    }


                }) {
                Text(
                    text = if (isvisible) "Sign up" else "Log in",
                    modifier = Modifier
                        .padding(8.dp),
                    fontSize = 15.sp
                )
            }
        }


        Text(

            modifier = Modifier
                .padding(30.dp, top = 50.dp)
                .clickable { isvisible = !isvisible },
            text = if (issignedup) {
                if (isvisible) "Have an account? Log in here" else "Don't have an account? Sign up here"
            } else {
                "User is not signed up"
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,

            )




    }
}


fun handlesignup(username:String, email: String, password: String, context: Context) {




    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task: Task<AuthResult> ->
            if (task.isSuccessful) {

                //First saves user to the firebase database
                val userID = FirebaseAuth.getInstance().currentUser?.uid
               val database = FirebaseDatabase.getInstance().reference

                if (userID != null) {
                    val user = User(userID, username,email,"")

                    database.child("users").child(userID).setValue(user).addOnCompleteListener {
                        if(task.isSuccessful){
                            Log.d("User Saved", "User is saved to firebasedatabase")
                        } else {
                            val errormessage = task.exception?.message.toString()
                            Log.d("User Failed to save", "Error: $errormessage")
                        }
                    }
                }

                //Goes to the chatactivity
                //create a new intent to go to the gpt chatting activity
                val intent = Intent(context, gptactivty::class.java)
                //starts the activity
                startActivity(context,intent,null)

                Toast.makeText(context, "Signup Complete", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(context, "Signup failed", Toast.LENGTH_SHORT)
                    .show()
                Toast.makeText(
                    context,
                    task.exception?.localizedMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
}


fun handlelogin(email: String, password: String, context: Context) {

    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener { task: Task<AuthResult> ->

        if (task.isSuccessful) {

            val intent = Intent (context,gptactivty::class.java)
            startActivity(context,intent,null)

            Toast.makeText(context, "Login Complete", Toast.LENGTH_SHORT)
                .show()


        } else {
            Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT)
                .show()
            Toast.makeText(
                context,
                task.exception?.localizedMessage,
                Toast.LENGTH_LONG
            ).show()
        }

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GPTCentralTheme {
        loginfields()
    }
}