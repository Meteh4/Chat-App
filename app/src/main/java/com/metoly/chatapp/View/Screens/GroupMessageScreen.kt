import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.metoly.chatapp.Model.ChatUser
import com.metoly.chatapp.Model.MessageItem
import com.metoly.chatapp.ui.theme.CustomDivider
import com.metoly.chatapp.ui.theme.CustomRow
import com.metoly.chatapp.viewmodel.GroupChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GroupMessageScreen(navController: NavController, vm: GroupChatViewModel, chatId: String) {
    var reply by rememberSaveable {
        mutableStateOf("")
    }
    val onSendReply = {
        vm.onSendGroup(chatId, reply)
        reply = ""
    }
    val myUser = vm.userData.value
    val currentChat = vm.groupChats.value.first { it.chatId == chatId }
    val currentUserUID = myUser?.UID ?: ""
    val chatUser = currentChat.users.find { it.userId != currentUserUID } ?: ChatUser()

    LaunchedEffect(key1 = Unit) {
        vm.getMessagesGroup(chatId)
    }
    BackHandler {

    }

    Scaffold (
        bottomBar = {
            GroupReplyBox(
                reply = reply,
                onReplyChange = { reply = it },
                onSendReply = onSendReply)},
        topBar = {
            GroupChatHeader(name = chatUser.name ?: "", imageUrl = chatUser.imageUrl ?: "") {
                navController.popBackStack()
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            GroupMessageList(messages = vm.messages.value.messages, userID = myUser?.UID ?: "")
        }
    }
}

@Composable
    fun GroupMessageList(messages: List<MessageItem>, userID: String) {
        LazyColumn {
            items(messages) { message ->
                val formattedTimestamp = message.timestamp?.let { formatTimestamp(it) }
                val isCurrentUserMessage = message.messageId.toString() == userID
                val backgroundColor =
                    if (isCurrentUserMessage) MaterialTheme.colorScheme.secondary else Color.Transparent
                val textColor =
                    if (isCurrentUserMessage) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                val messagePaddingLeft = if (isCurrentUserMessage) 64.dp else 0.dp
                val messagePaddingRight = if (isCurrentUserMessage) 0.dp else 64.dp
                val dividerBackground = if (isCurrentUserMessage) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.secondary
                val boxBorders = if (isCurrentUserMessage) RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 2.dp, bottomStart = 16.dp) else RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 2.dp)

                Box(
                    modifier = Modifier
                        .clip(boxBorders)
                        .fillMaxWidth()
                        .padding(start = messagePaddingLeft, end = messagePaddingRight)
                        .padding(6.dp)
                        .border(
                            1.dp,
                            color = MaterialTheme.colorScheme.secondary,
                            shape = boxBorders
                        )
                        .background(backgroundColor, shape = boxBorders)
                ) {
                    Column {
                        Text(
                            text = "${message.sendBy}:",
                            fontSize = 10.sp,
                            color = textColor,
                            modifier = Modifier.padding(start = 8.dp, top = 6.dp, bottom = 4.dp)
                        )
                        Divider(
                            color = dividerBackground
                        )
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "${message.message}",
                            color = textColor
                        )
                        Text(
                            text = "$formattedTimestamp",
                            fontSize = 10.sp,
                            color = textColor,
                            modifier = Modifier.align(Alignment.End).padding(4.dp)
                        )
                    }

                }
            }
        }
    }

    fun formatTimestamp(timestamp: Date): String {
        val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
        return sdf.format(timestamp)
    }

    @Composable
    fun GroupChatHeader(name: String, imageUrl: String, onBackClick: () -> Unit) {
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
    fun GroupReplyBox(reply: String, onReplyChange: (String) -> Unit, onSendReply: () -> Unit) {
        Column(
            modifier = Modifier
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
                OutlinedTextField(value = reply, onValueChange = onReplyChange, maxLines = 3)
                Button(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    onClick = onSendReply,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }