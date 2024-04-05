package com.metoly.chatapp.viewmodel
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.metoly.chatapp.Model.CHATS
import com.metoly.chatapp.Model.Event
import com.metoly.chatapp.Model.USER_NODE
import com.metoly.chatapp.Model.ChatData
import com.metoly.chatapp.Model.userData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UserAuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {
    private var inProcessChats = mutableStateOf(false)
    var inProgress = mutableStateOf(false)
    private var eventMutableState = mutableStateOf<Event<String>?>(null)
    var signIn = mutableStateOf(false)
    var userData = mutableStateOf<userData?>(null)
    private val chats = mutableStateOf<List<ChatData>>(listOf())

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    private fun getUpdateChats() {
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


    fun signUp(name: String, nickName: String, email: String, password: String) {

        if (name.isEmpty() or email.isEmpty() or nickName.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please Fill All The Fields !!")
            return
        }

        inProgress.value = true
        db.collection(USER_NODE).whereEqualTo("nickName", nickName).get().addOnSuccessListener { it ->
            if (it.isEmpty) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        signIn.value = true
                        createOrUpdateProfile(name, nickName, imgUrl = null)
                    } else {
                        handleException(it.exception, customMessage = "Sign Up Failed")
                    }
                }
            } else {
                handleException(customMessage = "Nickname Already Exist")
                inProgress.value = false
            }
        }
    }


    private fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.d("LiveChatApp", " Live Chat Exception ", exception)
        exception?.printStackTrace()

        val errorMessage = exception?.localizedMessage ?: ""
        val message = customMessage.ifEmpty { errorMessage }
        eventMutableState.value = Event(message)
        inProgress.value = false
    }


    fun logIn(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please Fill All The Fields")
            return
        } else {
            inProgress.value = true
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    signIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                } else {
                    handleException(exception = it.exception, customMessage = "Login Failed")
                }
            }
        }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) { uploadedUri ->
            val imageUrl = uploadedUri.toString()
            createOrUpdateProfile(imgUrl = imageUrl)
            println(imageUrl)
        }
    }

    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)
            inProgress.value = false
        }
            .addOnFailureListener {
                handleException(it)
            }
    }

    fun createOrUpdateProfile(
        name: String? = null,
        nickName: String? = null,
        imgUrl: String? = null
    ) {
        val uid = auth.currentUser?.uid

        uid?.let { userId ->
            inProgress.value = true

            db.collection(USER_NODE).document(userId).get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = mutableMapOf<String, Any?>()

                    name?.let { userData["name"] = it }
                    nickName?.let { userData["nickName"] = it }
                    imgUrl?.let { userData["ImgURL"] = it }

                    db.collection(USER_NODE).document(userId).update(userData)
                        .addOnSuccessListener {
                            inProgress.value = false
                            updateChatsWithUserData(userId, name, nickName, imgUrl)
                        }
                        .addOnFailureListener { exception ->
                            inProgress.value = false
                            handleException(exception, "Cannot Update User")
                        }
                } else {
                    val userData = userData(
                        UID= userId,
                        Name = name,
                        NickName = nickName,
                        ImgURL = imgUrl
                    )

                    db.collection(USER_NODE).document(userId).set(userData)
                        .addOnSuccessListener {
                            inProgress.value = false
                        }
                        .addOnFailureListener { exception ->
                            inProgress.value = false
                            handleException(exception, "Cannot Create User")
                        }
                }
            }
                .addOnFailureListener { exception ->
                    inProgress.value = false
                    handleException(exception, "Cannot Retrieve User")
                }
        }
    }

    private fun updateChatsWithUserData(
        userId: String,
        name: String?,
        nickName: String?,
        imgUrl: String?
    ) {
        val updates = mutableMapOf<String, Any?>()

        if (name != null) updates["user1.name"] = name
        if (nickName != null) updates["user1.nickName"] = nickName
        if (imgUrl != null) updates["user1.imageUrl"] = imgUrl

        db.collection(CHATS).whereEqualTo("user1.userId", userId).get()
            .addOnSuccessListener { user1Chats ->
                user1Chats.forEach { chatDocument ->
                    val chatData = chatDocument.toObject<ChatData>()
                    chatData.chatId?.let {
                        db.collection(CHATS).document(it).update(updates)
                            .addOnFailureListener { exception ->
                                handleException(exception, "Cannot update chats")
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Cannot update chats")
            }

        db.collection(CHATS).whereEqualTo("user2.userId", userId).get()
            .addOnSuccessListener { user2Chats ->
                user2Chats.forEach { chatDocument ->
                    val chatData = chatDocument.toObject<ChatData>()
                    chatData.chatId?.let {
                        db.collection(CHATS).document(it).update(updates)
                            .addOnFailureListener { exception ->
                                handleException(exception, "Cannot update chats")
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Cannot update chats")
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

                getUpdateChats()
            }
        }
    }

    fun logOut() {
        auth.signOut()
        signIn.value = false
        userData.value = null
        eventMutableState.value = Event("Logged Out")
    }
}