package com.metoly.chatapp.View.Screens
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metoly.chatapp.Model.MessageItem
import com.metoly.chatapp.ui.theme.CustomDivider
import com.metoly.chatapp.ui.theme.CustomRow
import com.metoly.chatapp.viewmodel.ChatViewModel

@Composable
fun PrivateMessageScreen(navController: NavController, vm: ChatViewModel, chatId: String) {
    var reply by rememberSaveable {
        mutableStateOf("")
    }
    val onSendReply = {
        vm.onSendReply(chatId, reply)
        reply = ""
    }

    val myUser = vm.userData.value
    val currentChat = vm.chats.value.first { it.chatId == chatId }
    val chatUser =
        if (myUser?.UID == currentChat.user1.userId) currentChat.user2 else currentChat.user1

    LaunchedEffect(key1 = Unit) {
        vm.getMessages(chatId)
    }
    BackHandler {

    }

    Scaffold (
        bottomBar = {
            ReplyBox(
                reply = reply,
                onReplyChange = { reply = it },
                onSendReply = onSendReply)},
        topBar = {
            ChatHeader(name = chatUser.name ?: "", imageUrl = chatUser.imageUrl ?: "") {
                navController.popBackStack()
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            MessageList(messages = vm.chatMessages.value.messages, userID = myUser?.UID ?: "")
        }
    }
}

@Composable
fun MessageList(messages: List<MessageItem>, userID: String) {
    LazyColumn {
        items(messages) { message ->
            val isCurrentUserMessage = message.messageId.toString() == userID
            val backgroundColor = if (isCurrentUserMessage) MaterialTheme.colorScheme.primary else Color.Transparent
            val textColor = if (isCurrentUserMessage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
                    .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "${message.sendBy}: ${message.message}",
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun ChatHeader(name: String, imageUrl: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = null,
            modifier = Modifier
                .clickable { onBackClick.invoke() }
                .padding(8.dp)
        )

        CustomRow(imgUrl = imageUrl, name = name) {

        }
    }
}

@Composable
fun ReplyBox(reply: String, onReplyChange: (String) -> Unit, onSendReply: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)
    ) {
        CustomDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(8.dp)
                .wrapContentSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextField(value = reply, onValueChange = onReplyChange, maxLines = 3)
            Button(onClick = onSendReply, Modifier.padding(start = 4.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}