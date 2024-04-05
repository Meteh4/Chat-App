package com.metoly.chatapp.viewmodel
import android.icu.util.Calendar
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.metoly.chatapp.Model.CHATS
import com.metoly.chatapp.Model.Event
import com.metoly.chatapp.Model.MESSAGE
import com.metoly.chatapp.Model.USER_NODE
import com.metoly.chatapp.Model.ChatData
import com.metoly.chatapp.Model.ChatUser
import com.metoly.chatapp.Model.MessageItem
import com.metoly.chatapp.Model.MessageList
import com.metoly.chatapp.Model.userData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {
    var inProcessChats = mutableStateOf(false)
    private var inProgress = mutableStateOf(false)
    private var eventMutbleState = mutableStateOf<Event<String>?>(null)
    private var signIn = mutableStateOf(false)
    var userData = mutableStateOf<userData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }
    private fun getChats() {
        inProcessChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.UID),
                Filter.equalTo("user2.userId", userData.value?.UID),
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }
            if (value != null) {
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }
                inProcessChats.value = false
            }
        }
    }

    val chatMessages = mutableStateOf<MessageList>(MessageList())

    fun getMessages(chatId: String) {
        db.collection(CHATS)
            .document(chatId)
            .collection(MESSAGE)
            .orderBy("timestamp")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    val messageList = value.documents.mapNotNull {
                        it.toObject<MessageItem>()
                    }.map {
                        MessageItem(
                            messageId = it.messageId,
                            sendBy = it.sendBy,
                            message = it.message,
                            timestamp = it.timestamp
                        )
                    }
                    chatMessages.value = MessageList(messages = messageList)
                }
            }
    }

    fun onSendReply(chatId: String, message: String) {
        val time = Calendar.getInstance().time
        val messageData = MessageItem(sendBy = userData.value?.NickName, message = message, timestamp = time, messageId = userData.value?.UID)
        db.collection(CHATS).document(chatId).collection(MESSAGE).document().set(messageData)
    }

    private fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.d("LiveChatApp", " Live Chat Exception ", exception)
        exception?.printStackTrace()

        val errorMessage = exception?.localizedMessage ?: ""
        val message = customMessage.ifEmpty { errorMessage }
        eventMutbleState.value = Event(message)
        inProgress.value = false
    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->

            if (error != null) {
                handleException(error, "Cannot Retrieve User")
            }

            if (value != null) {
                val user = value.toObject<userData>()
                userData.value = user
                inProgress.value = false

                getChats()
            }
        }
    }

    fun onAddChat(nickName: String) {
        if (nickName.isEmpty()) {
            handleException(customMessage = "Nickname Can't Be Empty")
        } else {
            db.collection(CHATS).where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("user1.nickName", nickName),
                        Filter.equalTo("user2.nickName", userData.value?.NickName)
                    ),
                    Filter.and(
                        Filter.equalTo("user1.nickName", userData.value?.NickName),
                        Filter.equalTo("user2.nickName", nickName)
                    )
                )
            ).get().addOnSuccessListener { it ->
                if (it.isEmpty) {
                    db.collection(USER_NODE).whereEqualTo("nickName", nickName).get()
                        .addOnSuccessListener {
                            if (it.isEmpty) {
                                handleException(customMessage = "Nick Name Not Found")
                            } else {
                                val chatPatners = it.toObjects<userData>()[0]
                                val id = db.collection(CHATS).document().id
                                val chat = ChatData(
                                    chatId = id,
                                    ChatUser(
                                        userData.value?.UID,
                                        userData.value?.Name,
                                        userData.value?.ImgURL,
                                        userData.value?.NickName
                                    ),
                                    ChatUser(
                                        chatPatners.UID,
                                        chatPatners.Name,
                                        chatPatners.ImgURL,
                                        chatPatners.NickName
                                    )
                                )
                                db.collection(CHATS).document(id).set(chat)
                            }
                        }
                        .addOnFailureListener {
                            handleException(it)
                        }
                } else {
                    handleException(customMessage = "Chat Already Exist !!")
                }
            }
        }
    }
}