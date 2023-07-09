package com.example.gptcentral

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue


class Chatviewmodel: ViewModel() {

    val messageList by mutableStateOf(mutableListOf<Message>())

    // add message to list
    fun addMessage(message: Message) {
        messageList.add(message)
    }

    fun createchatrooms() {
        FirebaseDatabase.getInstance().getReference("users" + FirebaseAuth.getInstance().uid).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatroomId = FirebaseDatabase.getInstance().getReference("chatrooms").push().key
                if (chatroomId != null) {
                    snapshot.ref.child("chatrooms").setValue(chatroomId) // chatroom id is stored under current user's node

                    attachmessagelistener(chatroomId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Chatroom failed", "Failed to create unique chatrooms: $error")
            }

        })

    }

    fun attachmessagelistener(chatroomid:String){

        FirebaseDatabase.getInstance().getReference("messages/$chatroomid").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                snapshot.children.forEach {
                    val message = snapshot.getValue(Message::class.java)
                    if (message != null ){
                        messageList.add(snapshot.getValue(Message::class.java)!!)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Message Listenr error", "Could not put messages into firebase: $error" )
            }


        })
    }
}