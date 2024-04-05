package com.metoly.chatapp.viewmodel
import android.icu.util.Calendar
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.metoly.chatapp.Model.MESSAGE
import com.metoly.chatapp.Model.USER_NODE
import com.metoly.chatapp.Model.ChatUser
import com.metoly.chatapp.Model.GROUP_CHATS
import com.metoly.chatapp.Model.GroupChatData
import com.metoly.chatapp.Model.MessageItem
import com.metoly.chatapp.Model.MessageList
import com.metoly.chatapp.Model.userData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class GroupChatViewModel @Inject constructor(
    auth: FirebaseAuth,
    private val db: FirebaseFirestore,
) : ViewModel() {
    var inProcessChats = mutableStateOf(false)
    var userData = mutableStateOf<userData?>(null)
    private var signIn = mutableStateOf(false)
    private var inProgress = mutableStateOf(false)
    val groupChats = mutableStateOf<List<GroupChatData>>(listOf())
    val allUsers = MutableStateFlow<List<ChatUser>>(emptyList())

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
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

            }
        }
    }

    fun addGroupChat(groupName: String, users: List<ChatUser>, adminId: String) {
        val id = db.collection(GROUP_CHATS).document().id

        val updatedUsers = users.toMutableList()
        val currentUser = userData.value
        if (currentUser != null && currentUser.UID == adminId) {
            val adminUser = ChatUser(
                userId = currentUser.UID,
                name = currentUser.Name ?: "",
                imageUrl = currentUser.ImgURL ?: "",
                nickName = currentUser.NickName ?: ""
            )
            updatedUsers.add(adminUser)
        }

        val groupChat = GroupChatData(
            chatId = id,
            groupName = groupName,
            users = updatedUsers,
            adminId = adminId
        )
        db.collection(GROUP_CHATS).document(id).set(groupChat)
    }

    fun getAllUsers() {
        inProcessChats.value = true
        db.collection(USER_NODE).get().addOnSuccessListener { querySnapshot ->
            val users = mutableListOf<ChatUser>()
            for (document in querySnapshot.documents) {
                val userId = document.getString("uid")
                val name = document.getString("name")
                val imageUrl = document.getString("imgURL")
                val nickName = document.getString("nickName")

                val user = userData(
                    UID = userId,
                    Name = name,
                    ImgURL = imageUrl,
                    NickName = nickName
                )
                users.add(ChatUser(user.UID, user.Name, user.ImgURL, user.NickName))
            }
            allUsers.value = users
            inProcessChats.value = false
            Log.d("LiveChatApp", "Users: $users")
        }.addOnFailureListener { exception ->
            handleException(exception, "Cannot Retrieve Users")
        }
    }

    fun getGroupChats() {
        inProcessChats.value = true
        val currentUserUID = userData.value?.UID
        if (currentUserUID != null) {
            db.collection(GROUP_CHATS).get().addOnSuccessListener { groupChatsSnapshot ->
                val groupChats = mutableListOf<GroupChatData>()
                for (document in groupChatsSnapshot.documents) {
                    val groupChat = document.toObject<GroupChatData>()
                    if (groupChat != null) {
                        for (user in groupChat.users) {
                            if (user.userId == currentUserUID) {
                                groupChats.add(groupChat)
                                break
                            }
                        }
                    }
                }
                this.groupChats.value = groupChats
                inProcessChats.value = false
            }.addOnFailureListener { exception ->
                handleException(exception, "Cannot Retrieve Group Chats")
            }
        } else {
            handleException(customMessage = "User data not available")
        }
    }

    val messages = mutableStateOf<MessageList>(MessageList())

    fun getMessagesGroup(groupId: String) {
        db.collection(GROUP_CHATS)
            .document(groupId)
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
                    messages.value = MessageList(messages = messageList)
                }
            }
    }

    fun onSendGroup(groupId: String, message: String) {
        val time = Calendar.getInstance().time
        val messageData = MessageItem(sendBy = userData.value?.NickName, message = message, timestamp = time, messageId = userData.value?.UID)
        db.collection(GROUP_CHATS).document(groupId).collection(MESSAGE).document().set(messageData)
    }

    private fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.d("LiveChatApp", " Live Chat Exception ", exception)
        exception?.printStackTrace()
        val errorMessage = exception?.localizedMessage ?: ""
        val message = customMessage.ifEmpty { errorMessage }
        println(message)
    }
}