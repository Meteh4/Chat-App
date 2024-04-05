import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metoly.chatapp.DestinationScreen
import com.metoly.chatapp.Model.ChatUser
import com.metoly.chatapp.ui.theme.CommonProgressBar
import com.metoly.chatapp.ui.theme.CustomRow
import com.metoly.chatapp.ui.theme.TitleText
import com.metoly.chatapp.ui.theme.navigateTo
import com.metoly.chatapp.viewmodel.GroupChatViewModel


@Composable
fun GroupChatScreen(navController: NavController, vm: GroupChatViewModel) {
    val inProgress = vm.inProcessChats
    LaunchedEffect(Unit) {
        vm.getAllUsers()
        vm.getGroupChats()
    }
    if (inProgress.value) {
        CommonProgressBar()
    } else {

        val groupChats = vm.groupChats.value
        val showDialog = remember {
            mutableStateOf(false)
        }
        val allUsersState by vm.allUsers.collectAsState()
        val onFabClick: () -> Unit = { showDialog.value = true }
        val onDismiss: () -> Unit = { showDialog.value = false }
        val onAddGroupChat: (String, List<ChatUser>) -> Unit = { groupName, users ->
            val currentUser = vm.userData.value
            currentUser?.let { user ->
                val adminId = user.UID ?: ""
                vm.addGroupChat(groupName, users, adminId)
            }
            showDialog.value = false
        }
        Scaffold(
            topBar = {
                TitleText(
                    text = "Group Chats"
                )
            },
            bottomBar = {
                BottomNavBar(
                    selectedItem = BottomNavBarItems.STATUS,
                    navController = navController
                )
            },
            floatingActionButton = {
                FABC(
                    showDialog = showDialog.value,
                    onFabClick = onFabClick,
                    onDismiss = onDismiss,
                    onAddChat = onAddGroupChat,
                    availableUsers = allUsersState
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    if (groupChats.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.material.Text(text = "No Group Chats Available !!")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(groupChats) { groupChat ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .border(1.dp, color = MaterialTheme.colors.primary, shape = RoundedCornerShape(8.dp))
                                ) {
                                    CustomRow(
                                        imgUrl = groupChat.groupImageUrl,
                                        name = groupChat.groupName
                                    ) {
                                        navigateTo(
                                            navController,
                                            DestinationScreen.GroupMessage.createRoute(chatId = groupChat.chatId ?: "")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun FABC(
    showDialog: Boolean,
    onFabClick: () -> Unit,
    onDismiss: () -> Unit,
    onAddChat: (String, List<ChatUser>) -> Unit,
    availableUsers: List<ChatUser>
) {
    val groupNameState = remember { mutableStateOf(TextFieldValue()) }
    val usersState = remember { mutableStateListOf<ChatUser>() }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss.invoke()
                groupNameState.value = TextFieldValue()
                usersState.clear()
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddChat(groupNameState.value.text, usersState)
                    }
                ) {
                    androidx.compose.material.Text(text = "Add Group Chat")
                }
            },
            title = { androidx.compose.material.Text(text = "Add Group Chat") },
            text = {
                Column {
                    OutlinedTextField(
                        value = groupNameState.value,
                        onValueChange = { groupNameState.value = it },
                        label = { androidx.compose.material.Text(text = "Group Name") }
                    )
                    availableUsers.forEach { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = usersState.contains(user),
                                onCheckedChange = {
                                    if (it) {
                                        usersState.add(user)
                                    } else {
                                        usersState.remove(user)
                                    }
                                }
                            )
                            androidx.compose.material.Text(
                                text = user.nickName ?: "",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        )
        FloatingActionButton(
            onClick = { onFabClick() },
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = Color.White)
        }
    } else {
        FloatingActionButton(
            onClick = { onFabClick() },
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = Color.White)
        }
    }
}
